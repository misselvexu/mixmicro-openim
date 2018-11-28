package com.acmedcare.framework.newim.server.core.connector;

import static com.acmedcare.framework.newim.server.ClusterLogger.clusterReplicaLog;

import com.acmedcare.framework.kits.executor.RetriableThreadExecutor;
import com.acmedcare.framework.kits.executor.RetriableThreadExecutor.ExecutorCallback;
import com.acmedcare.framework.kits.executor.RetriableThreadExecutor.RetriableAttribute;
import com.acmedcare.framework.kits.thread.DefaultThreadFactory;
import com.acmedcare.framework.kits.thread.ThreadKit;
import com.acmedcare.framework.newim.BizResult;
import com.acmedcare.framework.newim.protocol.Command.ClusterWithClusterCommand;
import com.acmedcare.framework.newim.protocol.Command.Retriable;
import com.acmedcare.framework.newim.protocol.RetriableRemotingCommand;
import com.acmedcare.framework.newim.protocol.request.ClusterForwardMessageHeader;
import com.acmedcare.framework.newim.server.config.IMProperties;
import com.acmedcare.framework.newim.server.core.ClusterReplicaSession;
import com.acmedcare.framework.newim.server.core.IMSession;
import com.acmedcare.framework.newim.server.event.AbstractEventHandler;
import com.acmedcare.framework.newim.server.event.Event;
import com.acmedcare.tiffany.framework.remoting.ChannelEventListener;
import com.acmedcare.tiffany.framework.remoting.OnewayCallback;
import com.acmedcare.tiffany.framework.remoting.netty.NettyClientConfig;
import com.acmedcare.tiffany.framework.remoting.netty.NettyRemotingSocketClient;
import com.acmedcare.tiffany.framework.remoting.protocol.RemotingCommand;
import com.acmedcare.tiffany.framework.remoting.protocol.RemotingSysRequestCode;
import com.alibaba.fastjson.JSON;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import io.netty.channel.Channel;
import io.netty.util.HashedWheelTimer;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.ThreadPoolExecutor.CallerRunsPolicy;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.ReentrantLock;

/**
 * Cluster Replica Connector
 *
 * @author <a href="mailto:iskp.me@gmail.com">Elve.Xu</a>
 * @version ${project.version} - 16/11/2018.
 */
public class ClusterReplicaConnector {

  private final IMProperties properties;
  private final IMSession imSession;
  private final NettyClientConfig nettyClientConfig;

  /** Replica Server List */
  private Set<String> replicaServerAddressList = Sets.newHashSet();

  private ReentrantLock upgradeReplicaServerAddressLocker = new ReentrantLock();

  private Map<String, NettyRemotingSocketClient> replicaServerInstancesMap =
      Maps.newConcurrentMap();

  private Map<String, ScheduledExecutorService> keepAliveExecutors = Maps.newConcurrentMap();

  private ClusterReplicaSession clusterReplicaSession = new ClusterReplicaSession();

  private ExecutorService forwardMessageExecutor =
      new ThreadPoolExecutor(
          8,
          16,
          5000,
          TimeUnit.MILLISECONDS,
          new LinkedBlockingQueue<>(64),
          new DefaultThreadFactory("forward-message-executor"),
          new CallerRunsPolicy());

  private HashedWheelTimer forwardRetryHashedWheelTimer =
      new HashedWheelTimer(60, TimeUnit.SECONDS);
  private AbstractEventHandler<List<String>> fetchNewClusterReplicaServerEventHandler;

  public ClusterReplicaConnector(IMProperties properties, IMSession imSession) {
    this.properties = properties;
    this.imSession = imSession;
    this.nettyClientConfig = new NettyClientConfig();
    this.nettyClientConfig.setEnableHeartbeat(false);
    this.nettyClientConfig.setClientChannelMaxIdleTimeSeconds(40);
  }

  public void start() {
    // register instance
    imSession.registerClusterReplicasConnector(this);

    this.fetchNewClusterReplicaServerEventHandler =
        new AbstractEventHandler<List<String>>() {
          @Override
          public void execute(Event<List<String>> event) {

            clusterReplicaLog.info("Received a refresh event :{}", event);
            upgradeReplicaServerAddressLocker.lock();
            try {
              List<String> addresses = event.data();
              if (addresses != null && addresses.size() > 0) {
                clusterReplicaLog.info(
                    "Find some replica servers,ready to connect them: {}",
                    JSON.toJSONString(addresses));

                for (String address : addresses) {
                  if (!replicaServerInstancesMap.containsKey(address)) {
                    //
                    clusterReplicaLog.info("Find new replica server,ready to connect {}", address);

                    String name = "[Connector:" + address + "]";
                    RetriableAttribute retriableAttribute =
                        new RetriableAttribute(1, 5000, TimeUnit.MILLISECONDS);
                    RetriableThreadExecutor<NettyRemotingSocketClient> executor =
                        new RetriableThreadExecutor<>(
                            name,
                            () -> startupReplicaRemotingClient(address),
                            retriableAttribute,
                            new ExecutorCallback<NettyRemotingSocketClient>() {
                              @Override
                              public void onCompleted(NettyRemotingSocketClient result) {
                                clusterReplicaLog.info("new replica:{} connect succeed.", address);
                              }

                              @Override
                              public void onFailed(String message) {
                                clusterReplicaLog.warn(
                                    "a new replica server :{} ,connect failed", address);
                              }
                            });

                    executor.execute();
                  }
                }
              } else {
                clusterReplicaLog.warn("Received refresh event data is empty.");
              }

            } finally {
              upgradeReplicaServerAddressLocker.unlock();
            }
          }
        };

    // register
    imSession.registerEventHandler(fetchNewClusterReplicaServerEventHandler);
  }

  private NettyRemotingSocketClient startupReplicaRemotingClient(final String address)
      throws Exception {
    if (!replicaServerInstancesMap.containsKey(address)) {
      // never init
      NettyRemotingSocketClient nettyRemotingSocketClient =
          new NettyRemotingSocketClient(
              nettyClientConfig,
              new ChannelEventListener() {
                @Override
                public void onChannelConnect(String remoteAddr, Channel channel) {
                  clusterReplicaLog.info(
                      "Cluster Replica Client Connector [{}] is connected", remoteAddr);
                }

                @Override
                public void onChannelClose(String remoteAddr, Channel channel) {
                  clusterReplicaLog.info(
                      "Cluster Replica Client Connector [{}] is closed", remoteAddr);
                }

                @Override
                public void onChannelException(String remoteAddr, Channel channel) {
                  clusterReplicaLog.info(
                      "Cluster Replica Client Connector [{}] is exception ,closing ..", remoteAddr);

                  try {
                    if (replicaServerInstancesMap.containsKey(remoteAddr)) {
                      clusterReplicaLog.info(
                          "shutdown & remove local connection client:{} cache.", remoteAddr);
                      replicaServerInstancesMap.get(remoteAddr).shutdown();
                    }
                  } catch (Exception ignore) {
                  }

                  try {
                    clusterReplicaLog.info(
                        "shutdown & remove local replica server address set :{} cache.",
                        remoteAddr);
                    replicaServerAddressList.remove(remoteAddr);
                  } catch (Exception ignore) {
                  }

                  try {
                    channel.close();
                  } catch (Exception ignore) {
                  }
                }

                @Override
                public void onChannelIdle(String remoteAddr, Channel channel) {
                  clusterReplicaLog.info(
                      "Cluster Replica Client Connector [{}] is idle", remoteAddr);
                }
              });

      // update replica address list
      nettyRemotingSocketClient.updateNameServerAddressList(Lists.newArrayList(address));

      // startup
      nettyRemotingSocketClient.start();

      //
      clusterReplicaLog.info("send handshake request to replica server :{} ", address);
      RemotingCommand handshakeRequest =
          RemotingCommand.createRequestCommand(ClusterWithClusterCommand.CLUSTER_HANDSHAKE, null);
      nettyRemotingSocketClient.invokeOneway(
          address,
          handshakeRequest,
          2000,
          new OnewayCallback() {
            @Override
            public void operationComplete(boolean b) {
              if (b) {
                clusterReplicaLog.info(
                    "handshake request to replica server :{} ,send succeed.", address);

                replicaServerInstancesMap.put(address, nettyRemotingSocketClient);

                ScheduledThreadPoolExecutor executor =
                    new ScheduledThreadPoolExecutor(
                        1, new DefaultThreadFactory("cluster-replica-connector-timer-" + address));

                clusterReplicaLog.info(
                    "startup schedule thread for server:{} to keep-alive ", address);

                executor.scheduleWithFixedDelay(
                    () -> {
                      try {
                        try {
                          RemotingCommand heartbeat =
                              RemotingCommand.createRequestCommand(
                                  RemotingSysRequestCode.HEARTBEAT, null);
                          nettyRemotingSocketClient.invokeOneway(
                              address,
                              heartbeat,
                              2000,
                              isSendOk -> {
                                if (!isSendOk) {
                                  clusterReplicaLog.warn(
                                      "Cluster-Replica client:{} heartbeat send failed ", address);
                                }
                              });
                        } catch (Exception e) {
                          clusterReplicaLog.warn(
                              "Cluster-Replica client:{} heartbeat send failed with exception",
                              address,
                              e);
                        }
                      } catch (Exception e) {
                        clusterReplicaLog.warn(
                            "Cluster-Replica client heartbeat process failed. ", e);
                      }
                    },
                    10,
                    properties.getClusterHeartbeat(),
                    TimeUnit.SECONDS);
                keepAliveExecutors.put(address, executor);
              }
            }
          });

      return nettyRemotingSocketClient;
    } else {
      return replicaServerInstancesMap.get(address);
    }
  }

  /**
   * 转发消息到集群其他客户端
   *
   * <p>
   */
  public void forwardMessage(
      ClusterForwardMessageHeader header, byte[] messages, Retriable retriable) {

    replicaServerInstancesMap.forEach(
        (address, nettyRemotingSocketClient) -> {
          clusterReplicaLog.info("准备分发消息到服务器:{}", address);
          doForwardMessage(nettyRemotingSocketClient, address, header, messages, retriable);
        });
  }

  private void doForwardMessage(
      NettyRemotingSocketClient nettyRemotingSocketClient,
      String server,
      ClusterForwardMessageHeader header,
      byte[] message,
      Retriable retriable) {

    try {
      RemotingCommand forwardRequest =
          RemotingCommand.createRequestCommand(
              ClusterWithClusterCommand.CLUSTER_FORWARD_MESSAGE, header);
      forwardRequest.setBody(message);
      forwardMessageExecutor.execute(
          () -> {
            try {
              nettyRemotingSocketClient.invokeAsync(
                  server,
                  forwardRequest,
                  2000,
                  responseFuture -> {
                    if (responseFuture.isSendRequestOK()) {
                      // send success
                      RemotingCommand response = responseFuture.getResponseCommand();
                      if (response != null) {
                        byte[] result = response.getBody();
                        BizResult bizResult = JSON.parseObject(result, BizResult.class);
                        if (bizResult != null && bizResult.getCode() == 0) {
                          // forward success
                          clusterReplicaLog.info(
                              "forward message:{} to server:{} succeed.",
                              new String(message),
                              server);
                        } else {
                          // failed
                          if (retriable != null && retriable.isRetry()) {
                            doRetry(
                                RetriableRemotingCommand.builder()
                                    .retriable(retriable)
                                    .client(nettyRemotingSocketClient)
                                    .targetServerAddress(server)
                                    .header(header)
                                    .body(message)
                                    .build());
                            clusterReplicaLog.info(
                                "forward message success, response biz code is failed. , add retry queue");
                          }
                        }
                      } else {
                        // failed
                        if (retriable != null && retriable.isRetry()) {
                          doRetry(
                              RetriableRemotingCommand.builder()
                                  .retriable(retriable)
                                  .client(nettyRemotingSocketClient)
                                  .targetServerAddress(server)
                                  .header(header)
                                  .body(message)
                                  .build());
                          clusterReplicaLog.info("forward message failed , add retry queue");
                        }
                      }
                    } else {
                      // failed
                      if (retriable != null && retriable.isRetry()) {
                        doRetry(
                            RetriableRemotingCommand.builder()
                                .retriable(retriable)
                                .client(nettyRemotingSocketClient)
                                .targetServerAddress(server)
                                .header(header)
                                .body(message)
                                .build());
                        clusterReplicaLog.info("forward message failed , add retry queue");
                      }
                    }
                  });
            } catch (Exception e) {
              clusterReplicaLog.error(
                  "server:{} forward message thread execute exception", server, e);
              if (retriable != null && retriable.isRetry()) {
                doRetry(
                    RetriableRemotingCommand.builder()
                        .retriable(retriable)
                        .client(nettyRemotingSocketClient)
                        .targetServerAddress(server)
                        .header(header)
                        .body(message)
                        .build());
                clusterReplicaLog.info("forward message failed , add retry queue");
              }
            }
          });
    } catch (Exception e) {
      clusterReplicaLog.error("Cluster forward message execute failed with exception", e);
    }
  }

  private void doRetry(RetriableRemotingCommand retriableRemotingCommand) {
    if (retriableRemotingCommand != null) {
      retriableRemotingCommand.getRetriable().retry();

      if (retriableRemotingCommand.getRetriable().getRetryTimes()
          > retriableRemotingCommand.getRetriable().getMaxCounts()) {
        clusterReplicaLog.info(
            "forward message can't retry any more. already retry times:{} > max Times: {}",
            retriableRemotingCommand.getRetriable().getRetryTimes(),
            retriableRemotingCommand.getRetriable().getMaxCounts());
        return;
      }

      // timer
      forwardRetryHashedWheelTimer.newTimeout(
          timeout ->
              doForwardMessage(
                  retriableRemotingCommand.getClient(),
                  retriableRemotingCommand.getTargetServerAddress(),
                  (ClusterForwardMessageHeader) retriableRemotingCommand.getHeader(),
                  retriableRemotingCommand.getBody(),
                  retriableRemotingCommand.getRetriable()),
          retriableRemotingCommand.getRetriable().getPeriod(),
          TimeUnit.SECONDS);
      clusterReplicaLog.info(
          "message:{} will forward on next round, after times: {} s later.",
          retriableRemotingCommand.getRetriable().getPeriod());
    }
  }

  public void shutdown() {
    if (forwardMessageExecutor != null) {
      ThreadKit.gracefulShutdown(forwardMessageExecutor, 10, 20, TimeUnit.SECONDS);
    }

    replicaServerInstancesMap.forEach(
        (s, nettyRemotingSocketClient) -> {
          try {
            nettyRemotingSocketClient.shutdown();
          } catch (Exception ignore) {
          }
        });

    if (forwardRetryHashedWheelTimer != null) {
      forwardRetryHashedWheelTimer.stop();
    }

    // unregister event handler
    imSession.unRegisterEventHandler();

    keepAliveExecutors.forEach(
        (s, scheduledExecutorService) -> {
          ThreadKit.gracefulShutdown(scheduledExecutorService, 10, 20, TimeUnit.SECONDS);
        });
  }
}
