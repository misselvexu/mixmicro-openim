package com.acmedcare.framework.newim.server.core.connector;

import static com.acmedcare.framework.newim.server.ClusterLogger.clusterReplicaLog;

import com.acmedcare.framework.kits.thread.DefaultThreadFactory;
import com.acmedcare.framework.kits.thread.ThreadKit;
import com.acmedcare.framework.newim.BizResult;
import com.acmedcare.framework.newim.protocol.Command.ClusterWithClusterCommand;
import com.acmedcare.framework.newim.protocol.Command.Retriable;
import com.acmedcare.framework.newim.protocol.RetriableRemotingCommand;
import com.acmedcare.framework.newim.server.config.IMProperties;
import com.acmedcare.framework.newim.server.core.ClusterReplicaSession;
import com.acmedcare.framework.newim.server.processor.header.ClusterForwardMessageHeader;
import com.acmedcare.tiffany.framework.remoting.ChannelEventListener;
import com.acmedcare.tiffany.framework.remoting.netty.NettyClientConfig;
import com.acmedcare.tiffany.framework.remoting.netty.NettyRemotingSocketClient;
import com.acmedcare.tiffany.framework.remoting.protocol.RemotingCommand;
import com.acmedcare.tiffany.framework.remoting.protocol.RemotingSysRequestCode;
import com.alibaba.fastjson.JSON;
import com.google.common.collect.Lists;
import io.netty.channel.Channel;
import io.netty.util.HashedWheelTimer;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.ThreadPoolExecutor.CallerRunsPolicy;
import java.util.concurrent.TimeUnit;

/**
 * Cluster Replica Connector
 *
 * @author <a href="mailto:iskp.me@gmail.com">Elve.Xu</a>
 * @version ${project.version} - 16/11/2018.
 */
public class ClusterReplicaConnector {

  private final IMProperties properties;
  private final NettyClientConfig nettyClientConfig;

  /** Replica Server List */
  private List<String> replicaServerList = Lists.newArrayList();

  private NettyRemotingSocketClient nettyRemotingSocketClient;
  private ClusterReplicaSession clusterReplicaSession = new ClusterReplicaSession();
  private ScheduledExecutorService keepAliveExecutor =
      new ScheduledThreadPoolExecutor(
          1, new DefaultThreadFactory("cluster-replica-connector-timer"));

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

  public ClusterReplicaConnector(IMProperties properties) {
    this.properties = properties;
    this.nettyClientConfig = new NettyClientConfig();
    this.nettyClientConfig.setEnableHeartbeat(false);
    this.nettyClientConfig.setClientChannelMaxIdleTimeSeconds(40);
  }

  /**
   * Startup Client Connector
   *
   * <p>
   */
  public void start() {
    if (nettyRemotingSocketClient == null) {
      nettyRemotingSocketClient =
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
    }

    // update replica address list
    nettyRemotingSocketClient.updateNameServerAddressList(replicaServerList);

    // startup
    nettyRemotingSocketClient.start();

    // startup schedule thread for keep-alive
    keepAliveExecutor.scheduleWithFixedDelay(
        () -> {
          try {
            for (String server : replicaServerList) {
              try {
                RemotingCommand heartbeat =
                    RemotingCommand.createRequestCommand(RemotingSysRequestCode.HEARTBEAT, null);
                nettyRemotingSocketClient.invokeOneway(
                    server,
                    heartbeat,
                    3000,
                    isSendOk -> {
                      if (!isSendOk) {
                        clusterReplicaLog.warn(
                            "Cluster-Replica client:{} heartbeat send failed ", server);
                      }
                    });
              } catch (Exception e) {
                clusterReplicaLog.warn(
                    "Cluster-Replica client:{} heartbeat send failed with exception", server, e);
              }
            }
          } catch (Exception e) {
            clusterReplicaLog.warn("Cluster-Replica client heartbeat process failed. ", e);
          }
        },
        10,
        properties.getClusterHeartbeat(),
        TimeUnit.SECONDS);
  }

  /**
   * 转发消息到集群其他客户端
   *
   * <p>
   */
  public void forwardMessage(
      ClusterForwardMessageHeader header, byte[] messages, Retriable retriable) {

    for (String server : replicaServerList) {
      doForwardMessage(nettyRemotingSocketClient, server, header, messages, retriable);
    }
  }

  private void doForwardMessage(
      NettyRemotingSocketClient nettyRemotingSocketClient,
      String server,
      ClusterForwardMessageHeader header,
      byte[] messages,
      Retriable retriable) {

    try {
      RemotingCommand forwardRequest =
          RemotingCommand.createRequestCommand(
              ClusterWithClusterCommand.CLUSTER_FORWARD_MESSAGE, header);
      forwardRequest.setBody(messages);
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
                              new String(messages),
                              server);
                          return;
                        } else {
                          // failed
                          if (retriable != null && retriable.isRetry()) {
                            doRetry(
                                RetriableRemotingCommand.builder()
                                    .retriable(retriable)
                                    .client(nettyRemotingSocketClient)
                                    .targetServerAddress(server)
                                    .header(header)
                                    .body(messages)
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
                                  .body(messages)
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
                                .body(messages)
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
                        .body(messages)
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

    if(nettyRemotingSocketClient != null) {
      nettyRemotingSocketClient.shutdown();
    }

    if(forwardRetryHashedWheelTimer != null) {
      forwardRetryHashedWheelTimer.stop();
    }
  }
}
