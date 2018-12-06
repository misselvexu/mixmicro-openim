package com.acmedcare.framework.newim.server.core.connector;

import static com.acmedcare.framework.newim.server.ClusterLogger.masterClusterLog;

import com.acmedcare.framework.kits.Assert;
import com.acmedcare.framework.kits.executor.RetriableThreadExecutor;
import com.acmedcare.framework.kits.executor.RetriableThreadExecutor.ExecutorCallback;
import com.acmedcare.framework.kits.executor.RetriableThreadExecutor.RetriableAttribute;
import com.acmedcare.framework.kits.thread.ThreadKit;
import com.acmedcare.framework.newim.BizResult;
import com.acmedcare.framework.newim.InstanceNode;
import com.acmedcare.framework.newim.InstanceNode.NodeType;
import com.acmedcare.framework.newim.protocol.Command.MasterClusterCommand;
import com.acmedcare.framework.newim.protocol.request.ClusterPushSessionDataBody;
import com.acmedcare.framework.newim.protocol.request.ClusterPushSessionDataHeader;
import com.acmedcare.framework.newim.protocol.request.ClusterRegisterHeader;
import com.acmedcare.framework.newim.server.config.IMProperties;
import com.acmedcare.framework.newim.server.core.IMSession;
import com.acmedcare.framework.newim.server.event.Event;
import com.acmedcare.framework.newim.server.event.Event.FetchNewClusterReplicaServerEvent;
import com.acmedcare.framework.newim.server.processor.MasterNoticeClientChannelsRequestProcessor;
import com.acmedcare.framework.newim.server.processor.MasterPushMessageRequestProcessor;
import com.acmedcare.tiffany.framework.remoting.ChannelEventListener;
import com.acmedcare.tiffany.framework.remoting.exception.RemotingConnectException;
import com.acmedcare.tiffany.framework.remoting.exception.RemotingSendRequestException;
import com.acmedcare.tiffany.framework.remoting.exception.RemotingTimeoutException;
import com.acmedcare.tiffany.framework.remoting.exception.RemotingTooMuchRequestException;
import com.acmedcare.tiffany.framework.remoting.netty.NettyClientConfig;
import com.acmedcare.tiffany.framework.remoting.netty.NettyRemotingSocketClient;
import com.acmedcare.tiffany.framework.remoting.protocol.RemotingCommand;
import com.acmedcare.tiffany.framework.remoting.protocol.RemotingSysRequestCode;
import com.alibaba.fastjson.JSON;
import com.google.common.collect.Lists;
import com.google.common.eventbus.AsyncEventBus;
import io.netty.channel.Channel;
import io.netty.util.concurrent.DefaultThreadFactory;
import java.util.List;
import java.util.Random;
import java.util.Set;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.ThreadPoolExecutor.CallerRunsPolicy;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import lombok.Getter;
import lombok.Setter;

/**
 * Master Connector For IM Server Instance
 *
 * @author <a href="mailto:iskp.me@gmail.com">Elve.Xu</a>
 * @version ${project.version} - 12/11/2018.
 * @see IMProperties#getMasterNodes() Master Node List
 * @see IMProperties#getMasterHeartbeat() Master Heaerbeat Value
 */
public class MasterConnector {

  private final IMSession imSession;
  private final IMProperties imProperties;
  private InstanceNode localReplicaNode;

  @Getter
  private List<RemoteMasterConnectorInstance> remoteMasterConnectorInstances = Lists.newArrayList();

  private ScheduledExecutorService rollingPullClusterListExecutor;
  private ScheduledExecutorService rollingPushRemotingChannelsExecutor;
  private ExecutorService asyncExecutor;

  public MasterConnector(IMProperties imProperties, IMSession imSession) {
    this.imProperties = imProperties;
    this.imSession = imSession;
    this.localReplicaNode =
        InstanceNode.builder()
            .host(imProperties.getHost() + ":" + imProperties.getClusterPort())
            .nodeType(NodeType.REPLICA)
            .build();
  }

  public void start() {
    List<String> masterNodes = this.imProperties.getMasterNodes();
    if (masterNodes != null && masterNodes.size() > 0) {
      masterClusterLog.info("Ready master client connecting ...");
      for (String masterNode : masterNodes) {
        remoteMasterConnectorInstances.add(newMasterConnectorInstance(masterNode));
      }

      // start all
      for (RemoteMasterConnectorInstance remoteMasterConnectorInstance :
          remoteMasterConnectorInstances) {
        remoteMasterConnectorInstance.start();
      }

      masterClusterLog.info("Startup schedule rolling pull cluster server list thread.");
      startupRollingPullCluster();
      masterClusterLog.info("Startup schedule rolling push channels thread.");
      startupRollingPushChannels();
    }
  }

  private void startupRollingPushChannels() {

    if (asyncExecutor == null) {
      asyncExecutor =
          new ThreadPoolExecutor(
              4,
              16,
              0L,
              TimeUnit.MILLISECONDS,
              new LinkedBlockingQueue<>(16),
              new com.acmedcare.framework.kits.thread.DefaultThreadFactory("notifier-executor"),
              new CallerRunsPolicy());
    }

    if (rollingPushRemotingChannelsExecutor == null) {
      rollingPushRemotingChannelsExecutor =
          new ScheduledThreadPoolExecutor(1, new DefaultThreadFactory("rolling-push-thread"));
      rollingPushRemotingChannelsExecutor.scheduleWithFixedDelay(
          () -> {
            try {
              CountDownLatch countDownLatch =
                  new CountDownLatch(remoteMasterConnectorInstances.size());
              for (RemoteMasterConnectorInstance remoteMasterConnectorInstance :
                  remoteMasterConnectorInstances) {

                asyncExecutor.execute(
                    () -> {
                      try {
                        ClusterPushSessionDataHeader header = new ClusterPushSessionDataHeader();

                        RemotingCommand requestCommand =
                            RemotingCommand.createRequestCommand(
                                MasterClusterCommand.CLUSTER_PUSH_CLIENT_CHANNELS, header);

                        ClusterPushSessionDataBody body = new ClusterPushSessionDataBody();

                        body.setPassportIds(Lists.newArrayList(imSession.getOnlinePassports()));
                        body.setDeviceIds(Lists.newArrayList(imSession.getOnlineDevices()));

                        requestCommand.setBody(JSON.toJSONBytes(body));

                        RemotingCommand response =
                            remoteMasterConnectorInstance
                                .getNettyRemotingSocketClient()
                                .invokeSync(
                                    remoteMasterConnectorInstance.getMasterNode().getHost(),
                                    requestCommand,
                                    3000);

                        if (response != null) {
                          BizResult bizResult =
                              JSON.parseObject(response.getBody(), BizResult.class);
                          if (bizResult != null && bizResult.getCode() == 0) {
                            masterClusterLog.info(
                                "cluster push remoting channels timer execute succeed.");
                          } else {
                            masterClusterLog.warn(
                                "cluster push remoting channels timer execute failed ,response is : {}",
                                JSON.toJSONString(bizResult));
                          }
                        } else {
                          masterClusterLog.warn(
                              "cluster push remoting channels timer execute failed without return response .");
                        }

                      } catch (Exception e) {
                        masterClusterLog.error(
                            "cluster push remoting channels timer execute failed with request :{} ,will try next",
                            remoteMasterConnectorInstance.getMasterNode().getHost(),
                            e);
                      } finally{
                        countDownLatch.countDown();
                      }
                    });
              }

              countDownLatch.await();
            } catch (Exception e) {
              masterClusterLog.error("cluster push remoting channels timer execute failed", e);
            }
          },
          10,
          20,
          TimeUnit.SECONDS);
    }
  }

  private void startupRollingPullCluster() {
    if (rollingPullClusterListExecutor == null) {
      rollingPullClusterListExecutor =
          new ScheduledThreadPoolExecutor(1, new DefaultThreadFactory("rolling-pull-thread"));
      rollingPullClusterListExecutor.scheduleWithFixedDelay(
          () -> {
            String server = null;
            RemoteMasterConnectorInstance instance = null;
            while (true) {
              Random indexRandom = new Random();
              int index = indexRandom.nextInt(remoteMasterConnectorInstances.size());
              instance = remoteMasterConnectorInstances.get(index);
              if (instance.isConnecting()) {
                server = instance.getMasterNode().getHost();
                break;
              }
            }

            if (server != null && server.trim().length() > 0) {
              try {
                RemotingCommand pullRequest =
                    RemotingCommand.createRequestCommand(
                        MasterClusterCommand.CLUSTER_PULL_REPLICAS, null);
                RemotingCommand response =
                    instance.getNettyRemotingSocketClient().invokeSync(server, pullRequest, 3000);
                if (response != null) {
                  byte[] body = response.getBody();
                  if (body != null) {
                    BizResult bizResult = JSON.parseObject(body, BizResult.class);
                    if (bizResult != null && bizResult.getCode() == 0) {
                      @SuppressWarnings("unchecked")
                      Set<String> clusterReplicas =
                          JSON.parseObject(JSON.toJSONString(bizResult.getData()), Set.class);
                      if (clusterReplicas != null && clusterReplicas.size() > 0) {

                        if (masterClusterLog.isDebugEnabled()) {
                          masterClusterLog.debug(
                              "从Master:{},服务器获取的最新的备份列表:{}",
                              server,
                              JSON.toJSONString(clusterReplicas));
                        }

                        clusterReplicas.remove(localReplicaNode.getHost());

                        if (clusterReplicas.size() > 0) {
                          Event refreshEvent =
                              new FetchNewClusterReplicaServerEvent(
                                  Lists.newArrayList(clusterReplicas));
                          instance.getAsyncEventBus().post(refreshEvent);
                          if (masterClusterLog.isDebugEnabled()) {
                            masterClusterLog.debug("成功发送刷新事件:{} ", refreshEvent);
                          }
                        }
                      }
                    }
                  }
                }
              } catch (Exception e) {
                masterClusterLog.error(
                    "Rolling pull cluster replicas server list failed with request:{} ,will try next",
                    server,
                    e);
              }
            } else {
              masterClusterLog.warn("Current time has no available master servers.");
            }
          },
          11,
          30,
          TimeUnit.SECONDS);
    }
  }

  public void shutdown() {
    if (rollingPullClusterListExecutor != null) {
      ThreadKit.gracefulShutdown(rollingPullClusterListExecutor, 5, 10, TimeUnit.SECONDS);
    }

    if (rollingPushRemotingChannelsExecutor != null) {
      ThreadKit.gracefulShutdown(rollingPushRemotingChannelsExecutor, 5, 10, TimeUnit.SECONDS);
    }

    if (asyncExecutor != null) {
      ThreadKit.gracefulShutdown(asyncExecutor, 5, 10, TimeUnit.SECONDS);
    }

    masterClusterLog.info("shutdown master client connections.");
    for (RemoteMasterConnectorInstance remoteMasterConnectorInstance :
        remoteMasterConnectorInstances) {
      remoteMasterConnectorInstance.shutdown();
    }
  }

  private RemoteMasterConnectorInstance newMasterConnectorInstance(String nodeAddress) {
    InstanceNode node = new InstanceNode(nodeAddress, NodeType.MASTER, null);

    RemoteMasterConnectorInstance instance = new RemoteMasterConnectorInstance(imProperties);
    instance.registerEventPostHolder(imSession.getAsyncEventBus());
    InstanceNode localNode =
        new InstanceNode(
            imProperties.getHost() + ":" + imProperties.getPort(), NodeType.CLUSTER, null);
    instance.setLocalNode(localNode);
    NettyClientConfig config = new NettyClientConfig();
    config.setEnableHeartbeat(false);
    config.setUseTLS(false);
    config.setClientChannelMaxIdleTimeSeconds(40); // idle

    NettyRemotingSocketClient client =
        new NettyRemotingSocketClient(
            config,
            new ChannelEventListener() {
              @Override
              public void onChannelConnect(String remoteAddr, Channel channel) {
                masterClusterLog.info("Master Cluster Client[{}] is connected", remoteAddr);
              }

              @Override
              public void onChannelClose(String remoteAddr, Channel channel) {
                masterClusterLog.info("Master Cluster Client[{}] is closed", remoteAddr);
              }

              @Override
              public void onChannelException(String remoteAddr, Channel channel) {
                masterClusterLog.info(
                    "Master Cluster Client[{}] is exception ,closing ..", remoteAddr);
                try {
                  channel.close();
                } catch (Exception ignore) {
                }
              }

              @Override
              public void onChannelIdle(String remoteAddr, Channel channel) {
                masterClusterLog.info("Master Cluster Client[{}] is idle", remoteAddr);
              }
            });

    client.updateNameServerAddressList(Lists.newArrayList(nodeAddress));

    client.registerProcessor(
        MasterClusterCommand.MASTER_NOTICE_CLIENT_CHANNELS,
        new MasterNoticeClientChannelsRequestProcessor(imSession),
        null);

    client.registerProcessor(
        MasterClusterCommand.MASTER_PUSH_MESSAGES,
        new MasterPushMessageRequestProcessor(imSession),
        null);

    // set
    instance.setMasterNode(node);
    instance.setNettyClientConfig(config);
    instance.setNettyRemotingSocketClient(client);
    return instance;
  }

  /**
   * 远程副本链接客户端
   *
   * <p>
   */
  @Getter
  @Setter
  public static class RemoteMasterConnectorInstance {

    private static final long CONNECT_DELAY = 5000; // 5秒, 没失败一次增加5秒延时
    @Getter private volatile boolean connecting = false; // 连接状态
    private volatile boolean started = false;
    private volatile AtomicInteger connectTimes = new AtomicInteger(1);
    private AsyncEventBus asyncEventBus;
    private InstanceNode masterNode;
    private InstanceNode localNode;
    /** 副本配置 */
    private NettyClientConfig nettyClientConfig;
    /** 副本客户端对象 */
    private NettyRemotingSocketClient nettyRemotingSocketClient;

    private ScheduledExecutorService heartbeatExecutor;
    private ScheduledExecutorService connectExecutor;

    private Long heartbeatTimeoutMillis = 5000L; // 心跳请求超时时间
    private IMProperties imProperties;

    RemoteMasterConnectorInstance(IMProperties imProperties) {
      this.imProperties = imProperties;
    }

    void start() {

      if (!started) {
        started = true;
        if (nettyRemotingSocketClient != null) {
          nettyRemotingSocketClient.start();
        }

        //
        connectExecutor =
            new ScheduledThreadPoolExecutor(1, new DefaultThreadFactory("connect-thread"));
        connectExecutor.scheduleWithFixedDelay(
            () -> {
              try {
                if (!connecting) {
                  ThreadKit.sleep(CONNECT_DELAY * connectTimes.get());
                  doConnect();
                } // ok
              } catch (Throwable e) {
                masterClusterLog.warn(
                    "Connect to master server:{} failed, wait next time connect..",
                    masterNode.getHost());
                connectTimes.incrementAndGet();
              }
            },
            3,
            10,
            TimeUnit.SECONDS);
      }
    }

    void doConnect() {
      try {
        handshake();
      } catch (Exception e) {
        masterClusterLog.error("Master-Cluster-Client connect exception", e);
      }
    }

    void resetConnectParams() {
      connectTimes.set(1);
    }

    private void handshake() throws Exception {
      CountDownLatch count = new CountDownLatch(1);
      masterClusterLog.info("send handshake request to server :{} ", masterNode.getHost());
      RemotingCommand handshakeRequest =
          RemotingCommand.createRequestCommand(MasterClusterCommand.CLUSTER_HANDSHAKE, null);
      nettyRemotingSocketClient.invokeOneway(
          masterNode.getHost(),
          handshakeRequest,
          2000,
          b -> {
            if (b) {
              masterClusterLog.info(
                  "send handshake request succeed, then send register request ...");

              RetriableThreadExecutor<Void> retriableThreadExecutor =
                  new RetriableThreadExecutor<>(
                      "Register-Retry-Thread",
                      () -> {
                        register(imProperties);
                        return null;
                      },
                      new RetriableAttribute(2, 3000, TimeUnit.MILLISECONDS),
                      new ExecutorCallback<Void>() {
                        @Override
                        public void onCompleted(Void result) {
                          count.countDown();
                        }

                        @Override
                        public void onFailed(String message) {
                          count.countDown();
                        }
                      });

              retriableThreadExecutor.execute();
            }
          });

      count.await();
    }

    void register(IMProperties imProperties) throws Exception {
      ClusterRegisterHeader header = new ClusterRegisterHeader();
      header.setClusterServerHost(localNode.getHost());
      header.setClusterReplicaAddress(imProperties.getHost() + ":" + imProperties.getClusterPort());

      // send register command
      masterClusterLog.info("send register request to server :{} ", masterNode.getHost());

      RemotingCommand registerRequest =
          RemotingCommand.createRequestCommand(MasterClusterCommand.CLUSTER_REGISTER, header);

      registerRequest.setBody(JSON.toJSONBytes(imProperties.loadWssEndpoints()));
      RemotingCommand response =
          nettyRemotingSocketClient.invokeSync(masterNode.getHost(), registerRequest, 3000);

      Assert.notNull(response, "register response must not be null.");
      BizResult bizResult = JSON.parseObject(response.getBody(), BizResult.class);
      if (bizResult != null && bizResult.getCode() == 0) {
        masterClusterLog.info("Master-Cluster-Client:{} register succeed. ", masterNode.getHost());

        connecting = true;
        // reset connect params
        resetConnectParams();

        // heartbeat startup
        heartbeat();
      }
    }

    private void heartbeat() {
      if (heartbeatExecutor == null) {
        masterClusterLog.info("startup master client heartbeat schedule thread.");
        // startup heartbeat
        heartbeatExecutor =
            new ScheduledThreadPoolExecutor(1, new DefaultThreadFactory("heartbeat-thread"));

        heartbeatExecutor.scheduleWithFixedDelay(
            () -> {
              RemotingCommand heartbeat =
                  RemotingCommand.createRequestCommand(RemotingSysRequestCode.HEARTBEAT, null);
              try {
                if (connecting) {
                  nettyRemotingSocketClient.invokeOneway(
                      masterNode.getHost(),
                      heartbeat,
                      heartbeatTimeoutMillis,
                      b -> {
                        if (b) {
                          if (masterClusterLog.isDebugEnabled()) {
                            masterClusterLog.debug("master connector send heartbeat succeed.");
                          }
                        }
                      });
                }
              } catch (InterruptedException
                  | RemotingTooMuchRequestException
                  | RemotingSendRequestException
                  | RemotingTimeoutException
                  | RemotingConnectException e) {
                masterClusterLog.warn(
                    "heartbeat thread interrupted exception or too much request exception or lose connection or request timeout , ignore");

                if (e instanceof RemotingConnectException) {
                  connecting = false;
                  masterClusterLog.warn(
                      "remote master connection is lose, waiting for re-connect ...");
                }
              }
            },
            10,
            40,
            TimeUnit.SECONDS);
      }
    }

    public void shutdown() {
      if (heartbeatExecutor != null) {
        ThreadKit.gracefulShutdown(heartbeatExecutor, 5, 10, TimeUnit.SECONDS);
      }
      if (connectExecutor != null) {
        ThreadKit.gracefulShutdown(connectExecutor, 5, 10, TimeUnit.SECONDS);
      }
      if (nettyRemotingSocketClient != null) {
        this.nettyRemotingSocketClient.shutdown();
      }
    }

    public void registerEventPostHolder(AsyncEventBus asyncEventBus) {
      this.asyncEventBus = asyncEventBus;
    }
  }
}
