package com.acmedcare.framework.newim.master.replica;

import static com.acmedcare.framework.newim.MasterLogger.masterClusterAcceptorLog;
import static com.acmedcare.framework.newim.MasterLogger.masterReplicaClientLog;
import static com.acmedcare.framework.newim.MasterLogger.masterReplicaLog;

import com.acmedcare.framework.kits.thread.ThreadKit;
import com.acmedcare.framework.newim.BizResult;
import com.acmedcare.framework.newim.InstanceNode;
import com.acmedcare.framework.newim.InstanceNode.NodeType;
import com.acmedcare.framework.newim.master.MasterConfig;
import com.acmedcare.framework.newim.master.MasterConfig.Replica;
import com.acmedcare.framework.newim.master.processor.DefaultMasterProcessor;
import com.acmedcare.framework.newim.master.processor.MasterSyncClusterSessionRequestProcessor;
import com.acmedcare.framework.newim.master.replica.MasterSession.MasterReplicaSession;
import com.acmedcare.framework.newim.master.replica.MasterSession.RemoteReplicaConnectorInstance;
import com.acmedcare.framework.newim.master.replica.MasterSession.RemoteReplicaInstance;
import com.acmedcare.framework.newim.protocol.Command.MasterWithMasterCommand;
import com.acmedcare.tiffany.framework.remoting.ChannelEventListener;
import com.acmedcare.tiffany.framework.remoting.common.RemotingUtil;
import com.acmedcare.tiffany.framework.remoting.netty.NettyClientConfig;
import com.acmedcare.tiffany.framework.remoting.netty.NettyRemotingSocketClient;
import com.acmedcare.tiffany.framework.remoting.netty.NettyRemotingSocketServer;
import com.acmedcare.tiffany.framework.remoting.netty.NettyRequestProcessor;
import com.acmedcare.tiffany.framework.remoting.netty.NettyServerConfig;
import com.acmedcare.tiffany.framework.remoting.protocol.RemotingCommand;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelHandlerContext;
import io.netty.util.AttributeKey;
import io.netty.util.concurrent.DefaultThreadFactory;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.ThreadPoolExecutor.CallerRunsPolicy;
import java.util.concurrent.TimeUnit;
import lombok.Builder;

/**
 * Replica Connector For {@link
 * com.acmedcare.tiffany.framework.remoting.netty.NettyRemotingSocketClient} and {@link
 * com.acmedcare.tiffany.framework.remoting.netty.NettyRemotingSocketServer}
 *
 * @author <a href="mailto:iskp.me@gmail.com">Elve.Xu</a>
 * @version ${project.version} - 07/11/2018.
 */
public class MasterConnector {

  /**
   * Master Server Acceptor For Replica
   *
   * <p>
   */
  public static class MasterClusterAcceptor {

    private NettyServerConfig serverConfig;
    private NettyRemotingSocketServer clusterAcceptorServer;

    @Builder
    public MasterClusterAcceptor(NettyServerConfig serverConfig) {
      this.serverConfig = serverConfig;
    }

    public NettyRemotingSocketServer newServer() {
      if (clusterAcceptorServer != null) {
        return clusterAcceptorServer;
      }
      // new
      clusterAcceptorServer =
          new NettyRemotingSocketServer(
              serverConfig,
              new ChannelEventListener() {
                @Override
                public void onChannelConnect(String remoteAddr, Channel channel) {
                  masterClusterAcceptorLog.debug("Replica Remoting[{}] is connected", remoteAddr);
                }

                @Override
                public void onChannelClose(String remoteAddr, Channel channel) {
                  masterClusterAcceptorLog.debug("Replica Remoting[{}] is closed", remoteAddr);
                }

                @Override
                public void onChannelException(String remoteAddr, Channel channel) {
                  masterClusterAcceptorLog.debug(
                      "Replica Remoting[{}] is exception ,closing ..", remoteAddr);
                  try {
                    channel.close();
                  } catch (Exception ignore) {
                  }
                }

                @Override
                public void onChannelIdle(String remoteAddr, Channel channel) {
                  masterClusterAcceptorLog.debug("Replica Remoting[{}] is idle", remoteAddr);
                }
              });

      return clusterAcceptorServer;
    }
  }

  /**
   * Master Replica Server
   *
   * <p>
   */
  public static class MasterReplicaServer {

    private final MasterConfig masterConfig;
    /** Master Replica Session Instance of {@link MasterReplicaSession} */
    private MasterReplicaSession masterReplicaSession = new MasterReplicaSession();

    private MasterSession masterSession = new MasterSession();
    /** 集群MServer配置 */
    private NettyServerConfig masterReplicaConfig;
    /** 集群MServer 实例 */
    private NettyRemotingSocketServer masterReplicaServer;

    /** Default Executor */
    private ExecutorService defaultExecutor =
        new ThreadPoolExecutor(
            1,
            1,
            0,
            TimeUnit.MILLISECONDS,
            new LinkedBlockingQueue<Runnable>(64),
            new DefaultThreadFactory("new-im-netty-default-processor-executor-"),
            new CallerRunsPolicy());

    public MasterReplicaServer(MasterConfig masterConfig) {
      this.masterConfig = masterConfig;
      this.masterReplicaConfig = new NettyServerConfig();
      this.masterReplicaConfig.setListenPort(masterConfig.getPort());
      masterReplicaLog.info("初始化Master-Replica-Server");
      masterReplicaServer =
          new NettyRemotingSocketServer(
              masterReplicaConfig,
              new ChannelEventListener() {
                @Override
                public void onChannelConnect(String remoteAddr, Channel channel) {
                  masterReplicaLog.debug("Master Replica Remoting[{}] is connected", remoteAddr);
                }

                @Override
                public void onChannelClose(String remoteAddr, Channel channel) {
                  masterReplicaLog.debug("Master Replica Remoting[{}] is closed", remoteAddr);
                }

                @Override
                public void onChannelException(String remoteAddr, Channel channel) {
                  masterReplicaLog.debug(
                      "Master Replica Remoting[{}] is exception ,closing ..", remoteAddr);
                  try {
                    channel.close();
                  } catch (Exception ignore) {
                  }
                }

                @Override
                public void onChannelIdle(String remoteAddr, Channel channel) {
                  masterReplicaLog.debug("Master Replica Remoting[{}] is idle", remoteAddr);
                }
              });

      // register default processor
      masterReplicaServer.registerDefaultProcessor(new DefaultMasterProcessor(), defaultExecutor);
      // register biz processor

      // 同步 session 处理器
      masterReplicaServer.registerProcessor(
          MasterWithMasterCommand.SYNC_SESSIONS,
          new MasterSyncClusterSessionRequestProcessor(masterSession),
          null);

      // master 相互注册处理器
      masterReplicaServer.registerProcessor(
          MasterWithMasterCommand.MASTER_REGISTER,
          new NettyRequestProcessor() {
            @Override
            public RemotingCommand processRequest(
                ChannelHandlerContext channelHandlerContext, RemotingCommand remotingCommand)
                throws Exception {
              // 注册副本
              RemotingCommand response =
                  RemotingCommand.createResponseCommand(remotingCommand.getCode(), null);

              String remoteAddress =
                  RemotingUtil.socketAddress2String(
                      channelHandlerContext.channel().remoteAddress());
              // register new remote client
              masterReplicaSession.registerReplica(
                  remoteAddress,
                  RemoteReplicaInstance.builder()
                      .masterRemoteReplicaChannel(channelHandlerContext.channel())
                      .build());
              masterReplicaLog.info(
                  "Master replica remote:{} instance register succeed", remoteAddress);

              response.setBody(BizResult.SUCCESS.bytes());
              return response;
            }

            @Override
            public boolean rejectRequest() {
              return false;
            }
          },
          null);

      // master 注销处理器
      masterReplicaServer.registerProcessor(
          MasterWithMasterCommand.MASTER_SHUTDOWN,
          new NettyRequestProcessor() {
            @Override
            public RemotingCommand processRequest(
                ChannelHandlerContext channelHandlerContext, RemotingCommand remotingCommand)
                throws Exception {
              RemotingCommand response =
                  RemotingCommand.createResponseCommand(remotingCommand.getCode(), null);

              // 停止副本服务器
              String remoteAddress =
                  RemotingUtil.socketAddress2String(
                      channelHandlerContext.channel().remoteAddress());

              masterReplicaSession.revokeReplica(remoteAddress);

              masterReplicaLog.info(
                  "Master replica remote:{} instance revoke succeed", remoteAddress);

              response.setBody(BizResult.SUCCESS.bytes());
              return response;
            }

            @Override
            public boolean rejectRequest() {
              return false;
            }
          },
          null);

      // 启动
      masterReplicaServer.start();
    }
  }

  /**
   * Master Replica Client
   *
   * <p>
   */
  public static class MasterReplicaClient {

    private static final AttributeKey<InstanceNode> INSTANCE_NODE_ATTRIBUTE_KEY =
        AttributeKey.newInstance("INSTANCE_NODE_ATTRIBUTE_KEY");

    private static Map<InstanceNode, RemoteReplicaConnectorInstance> connectedReplicas =
        Maps.newConcurrentMap();
    private static Map<InstanceNode, ScheduledExecutorService> connectionKeeper = Maps.newHashMap();

    private final MasterConfig masterConfig;

    public MasterReplicaClient(MasterConfig masterConfig) {
      this.masterConfig = masterConfig;

      List<Replica> replicas = this.masterConfig.getReplicas();
      if (replicas != null && replicas.size() > 0) {
        List<RemoteReplicaConnectorInstance> replicaConnectorInstances = Lists.newArrayList();
        for (Replica replica : replicas) {
          replicaConnectorInstances.add(newReplicaConnectorInstance(replica.getNodeAddress()));
        }

        // foreach start
        for (RemoteReplicaConnectorInstance replicaConnectorInstance : replicaConnectorInstances) {
          masterReplicaClientLog.info("Ready replica client connecting ...");
          replicaConnectorInstance.start();
          // wait 2s for init
          ThreadKit.sleep(2000, TimeUnit.MILLISECONDS);

          // timer thread
          final String address = replicaConnectorInstance.getConnectorNode().getHost();
          final InstanceNode node = replicaConnectorInstance.getConnectorNode();
          String threadName = address.replace(".", "-").replace(":", "-") + "-Connection-Keeper";
          masterReplicaClientLog.info(
              "Starting # replica client:{} connection keeper thread ,thread name :{}",
              address,
              threadName);
          ScheduledExecutorService scheduledExecutorService =
              new ScheduledThreadPoolExecutor(1, new DefaultThreadFactory(threadName));

          connectionKeeper.put(
              replicaConnectorInstance.getConnectorNode(), scheduledExecutorService);

          scheduledExecutorService.scheduleWithFixedDelay(
              () -> {
                try {
                  if (connectedReplicas.containsKey(node)) {
                    return;
                  }

                  // retry
                  masterReplicaClientLog.info("[Timer] retry replica client connecting ... ");
                  RemoteReplicaConnectorInstance instance = newReplicaConnectorInstance(address);

                  instance.start(); // start

                } catch (Exception e) {
                  masterReplicaClientLog.warn("Connection keeper connect failed", e);
                }
              },
              20,
              10,
              TimeUnit.SECONDS);
          masterReplicaClientLog.info(
              "Started # replica client:{} connection keeper thread ,thread name :{}",
              address,
              threadName);
        }

        //

      }
    }

    public void shutdown() {

      masterReplicaClientLog.info("shutdown connection keepers timer-threads.");
      // shutdown threads
      connectionKeeper.forEach(
          (key, value) -> {
            try {
              ThreadKit.gracefulShutdown(value, 10, 10, TimeUnit.SECONDS);
            } catch (Exception ignore) {
            }
          });

      masterReplicaClientLog.info("shutdown replica client connections.");
      connectedReplicas.forEach(
          (key, value) -> {
            try {
              value.shutdown();
            } catch (Exception ignore) {

            }
          });
    }

    RemoteReplicaConnectorInstance newReplicaConnectorInstance(String nodeAddress) {
      InstanceNode node = new InstanceNode(nodeAddress, NodeType.CLIENT);
      RemoteReplicaConnectorInstance instance = new RemoteReplicaConnectorInstance();
      NettyClientConfig config = new NettyClientConfig();
      config.setEnableHeartbeat(true);
      NettyRemotingSocketClient client =
          new NettyRemotingSocketClient(
              config,
              new ChannelEventListener() {
                @Override
                public void onChannelConnect(String remoteAddr, Channel channel) {
                  masterReplicaClientLog.debug(
                      "Master Replica Client[{}] is connected", remoteAddr);

                  // send register command
                  RemotingCommand registerRequest =
                      RemotingCommand.createRequestCommand(
                          MasterWithMasterCommand.MASTER_REGISTER, null);

                  channel
                      .writeAndFlush(registerRequest)
                      .addListener(
                          (ChannelFutureListener)
                              future -> {
                                if (future.isSuccess()) {
                                  connectedReplicas.put(node, instance);
                                  channel.attr(INSTANCE_NODE_ATTRIBUTE_KEY).set(node);
                                  masterReplicaClientLog.info(
                                      "Master-Replica-Client:{} register succeed. ", nodeAddress);
                                }
                              });
                }

                @Override
                public void onChannelClose(String remoteAddr, Channel channel) {
                  masterReplicaClientLog.debug("Master Replica Client[{}] is closed", remoteAddr);
                  InstanceNode instanceNode = channel.attr(INSTANCE_NODE_ATTRIBUTE_KEY).get();
                  connectedReplicas.remove(instanceNode);
                  masterReplicaClientLog.info(
                      "Master-Replica-Client:{} revoked.", instanceNode.getHost());
                }

                @Override
                public void onChannelException(String remoteAddr, Channel channel) {
                  masterReplicaClientLog.debug(
                      "Master Replica Client[{}] is exception ,closing ..", remoteAddr);
                  try {
                    channel.close();
                  } catch (Exception ignore) {
                  }
                }

                @Override
                public void onChannelIdle(String remoteAddr, Channel channel) {
                  masterReplicaClientLog.debug("Master Replica Client[{}] is idle", remoteAddr);
                }
              });

      client.updateNameServerAddressList(Lists.newArrayList(nodeAddress));

      // set
      instance.setConnectorNode(node);
      instance.setNettyClientConfig(config);
      instance.setNettyRemotingSocketClient(client);
      return instance;
    }
  }
}
