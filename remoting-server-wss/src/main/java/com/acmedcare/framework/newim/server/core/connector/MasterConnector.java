package com.acmedcare.framework.newim.server.core.connector;

import static com.acmedcare.framework.newim.server.ClusterLogger.masterClusterLog;

import com.acmedcare.framework.kits.thread.ThreadKit;
import com.acmedcare.framework.newim.InstanceNode;
import com.acmedcare.framework.newim.InstanceNode.NodeType;
import com.acmedcare.framework.newim.protocol.Command.MasterClusterCommand;
import com.acmedcare.framework.newim.protocol.request.ClusterRegisterHeader;
import com.acmedcare.framework.newim.server.config.IMProperties;
import com.acmedcare.framework.newim.server.core.IMSession;
import com.acmedcare.framework.newim.server.processor.MasterNoticeClientChannelsRequestProcessor;
import com.acmedcare.tiffany.framework.remoting.ChannelEventListener;
import com.acmedcare.tiffany.framework.remoting.common.RemotingUtil;
import com.acmedcare.tiffany.framework.remoting.exception.RemotingConnectException;
import com.acmedcare.tiffany.framework.remoting.exception.RemotingSendRequestException;
import com.acmedcare.tiffany.framework.remoting.exception.RemotingTimeoutException;
import com.acmedcare.tiffany.framework.remoting.exception.RemotingTooMuchRequestException;
import com.acmedcare.tiffany.framework.remoting.netty.NettyClientConfig;
import com.acmedcare.tiffany.framework.remoting.netty.NettyRemotingSocketClient;
import com.acmedcare.tiffany.framework.remoting.protocol.RemotingCommand;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFutureListener;
import io.netty.util.AttributeKey;
import io.netty.util.concurrent.DefaultThreadFactory;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import lombok.Builder;
import lombok.Builder.Default;
import lombok.Getter;
import lombok.Setter;

/**
 * Master Connector For IM Server Instance
 *
 * @author <a href="mailto:iskp.me@gmail.com">Elve.Xu</a>
 * @version ${project.version} - 12/11/2018.
 * @see IMProperties#masterNodes Master Node List
 * @see IMProperties#masterHeartbeat Master Heaerbeat Value
 */
public class MasterConnector {

  private static final AttributeKey<InstanceNode> INSTANCE_NODE_ATTRIBUTE_KEY =
      AttributeKey.newInstance("INSTANCE_NODE_ATTRIBUTE_KEY");
  private static Map<InstanceNode, RemoteMasterConnectorInstance> connectedReplicas =
      Maps.newConcurrentMap();
  private static Map<InstanceNode, ScheduledExecutorService> connectionKeeper = Maps.newHashMap();
  private static Map<InstanceNode, RetryDelay> connectionKeeperDelay = Maps.newHashMap();
  private final IMSession imSession;
  private final IMProperties imProperties;

  public MasterConnector(IMProperties imProperties, IMSession imSession) {
    this.imProperties = imProperties;
    this.imSession = imSession;
  }

  public void start() {
    List<String> masterNodes = this.imProperties.getMasterNodes();
    if (masterNodes != null && masterNodes.size() > 0) {
      List<RemoteMasterConnectorInstance> replicaConnectorInstances = Lists.newArrayList();
      for (String node : masterNodes) {
        replicaConnectorInstances.add(newMasterConnectorInstance(node));
      }

      // foreach start
      for (RemoteMasterConnectorInstance replicaConnectorInstance : replicaConnectorInstances) {
        masterClusterLog.info("Ready master client connecting ...");
        replicaConnectorInstance.start();
        // wait 2s for init
        ThreadKit.sleep(2000, TimeUnit.MILLISECONDS);

        // timer thread
        final String address = replicaConnectorInstance.getMasterNode().getHost();
        final InstanceNode node = replicaConnectorInstance.getMasterNode();
        String threadName = address.replace(".", "-").replace(":", "-") + "-Connection-Keeper";
        masterClusterLog.info(
            "Starting # master client:{} connection keeper thread ,thread name :{}",
            address,
            threadName);
        ScheduledExecutorService scheduledExecutorService =
            new ScheduledThreadPoolExecutor(1, new DefaultThreadFactory(threadName));

        connectionKeeper.put(replicaConnectorInstance.getMasterNode(), scheduledExecutorService);

        scheduledExecutorService.scheduleWithFixedDelay(
            () -> {
              try {
                if (connectedReplicas.containsKey(node)) {
                  if (connectedReplicas
                      .get(node)
                      .getNettyRemotingSocketClient()
                      .isChannelWritable(node.getHost())) {
                    return;
                  }
                }

                if (connectionKeeperDelay.containsKey(node)) {
                  // 延迟处理
                  ThreadKit.sleep(connectionKeeperDelay.get(node).getDelay());
                }

                // retry
                masterClusterLog.info("[Timer] retry master client connecting ... ");
                RemoteMasterConnectorInstance instance = newMasterConnectorInstance(address);

                instance.start(); // start

                // put
                connectedReplicas.put(node, instance);
                connectionKeeperDelay.remove(node);

              } catch (Exception e) {
                masterClusterLog.warn("Connection keeper connect failed", e);

                // update retry delay time
                RetryDelay temp = connectionKeeperDelay.get(node);
                if (temp == null) {
                  temp = RetryDelay.builder().build();
                }
                temp.retry(); // 每失败一次,时间就加一倍, 减少服务器资源消耗

                connectionKeeperDelay.put(node, temp);
              }
            },
            20,
            10,
            TimeUnit.SECONDS);
        masterClusterLog.info(
            "Started # master client:{} connection keeper thread ,thread name :{}",
            address,
            threadName);
      }
    }
  }

  public void shutdown() {

    masterClusterLog.info("shutdown connection keepers timer-threads.");
    // shutdown threads
    connectionKeeper.forEach(
        (key, value) -> {
          try {
            ThreadKit.gracefulShutdown(value, 10, 10, TimeUnit.SECONDS);
          } catch (Exception ignore) {
          }
        });

    masterClusterLog.info("shutdown master client connections.");
    connectedReplicas.forEach(
        (key, value) -> {
          try {
            value.shutdown();
          } catch (Exception ignore) {

          }
        });
  }

  private RemoteMasterConnectorInstance newMasterConnectorInstance(String nodeAddress) {
    InstanceNode node = new InstanceNode(nodeAddress, NodeType.MASTER);
    RemoteMasterConnectorInstance instance = new RemoteMasterConnectorInstance();
    InstanceNode localNode =
        new InstanceNode(
            RemotingUtil.getLocalAddress() + ":" + imProperties.getPort(), NodeType.CLUSTER);
    instance.setLocalNode(localNode);
    NettyClientConfig config = new NettyClientConfig();
    config.setEnableHeartbeat(true);
    config.setClientChannelMaxIdleTimeSeconds(40); // idle

    NettyRemotingSocketClient client =
        new NettyRemotingSocketClient(
            config,
            new ChannelEventListener() {
              @Override
              public void onChannelConnect(String remoteAddr, Channel channel) {
                masterClusterLog.info("Master Cluster Client[{}] is connected", remoteAddr);

                ClusterRegisterHeader header = new ClusterRegisterHeader();
                header.setHost(localNode.getHost());

                // send register command
                RemotingCommand registerRequest =
                    RemotingCommand.createRequestCommand(
                        MasterClusterCommand.CLUSTER_REGISTER, header);

                channel
                    .writeAndFlush(registerRequest)
                    .addListener(
                        (ChannelFutureListener)
                            future -> {
                              if (future.isSuccess()) {
                                connectedReplicas.put(node, instance);
                                channel.attr(INSTANCE_NODE_ATTRIBUTE_KEY).set(node);
                                masterClusterLog.info(
                                    "Master-Cluster-Client:{} register succeed. ", nodeAddress);
                              }
                            });
              }

              @Override
              public void onChannelClose(String remoteAddr, Channel channel) {
                InstanceNode instanceNode = channel.attr(INSTANCE_NODE_ATTRIBUTE_KEY).get();
                masterClusterLog.info(
                    "Master Cluster Client[{}] is closed", instanceNode.getHost());
                connectedReplicas.remove(instanceNode);
                masterClusterLog.info("Master-Cluster-Client:{} revoked.", instanceNode.getHost());
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

    // set
    instance.setMasterNode(node);
    instance.setNettyClientConfig(config);
    instance.setNettyRemotingSocketClient(client);
    return instance;
  }

  @Getter
  @Setter
  @Builder
  private static class RetryDelay {
    @Default private int times = 1;
    private long delay;

    public void retry() {
      this.times++;
      this.delay = delay * times;
    }
  }

  /**
   * 远程副本链接客户端
   *
   * <p>
   */
  @Getter
  @Setter
  public static class RemoteMasterConnectorInstance {

    private InstanceNode masterNode;
    private InstanceNode localNode;
    /** 副本配置 */
    private NettyClientConfig nettyClientConfig;
    /** 副本客户端对象 */
    private NettyRemotingSocketClient nettyRemotingSocketClient;

    public void start() {
      if (nettyRemotingSocketClient != null) {
        nettyRemotingSocketClient.start();

        // send register command
        RemotingCommand sharkHands =
            RemotingCommand.createRequestCommand(MasterClusterCommand.CLUSTER_SHAKEHAND, null);

        try {
          System.out.println("握手请求");
          nettyRemotingSocketClient.invokeOneway(masterNode.getHost(), sharkHands, 2000);
        } catch (InterruptedException e) {
          e.printStackTrace();
        } catch (RemotingConnectException e) {
          e.printStackTrace();
        } catch (RemotingSendRequestException e) {
          e.printStackTrace();
        } catch (RemotingTimeoutException e) {
          e.printStackTrace();
        } catch (RemotingTooMuchRequestException e) {
          e.printStackTrace();
        }
      }
    }

    public void shutdown() {
      if (nettyRemotingSocketClient != null) {
        nettyRemotingSocketClient.shutdown();
      }
    }
  }
}
