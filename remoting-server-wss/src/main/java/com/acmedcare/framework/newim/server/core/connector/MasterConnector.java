package com.acmedcare.framework.newim.server.core.connector;

import static com.acmedcare.framework.newim.server.ClusterLogger.masterClusterLog;

import com.acmedcare.framework.kits.thread.ThreadKit;
import com.acmedcare.framework.newim.BizResult;
import com.acmedcare.framework.newim.InstanceNode;
import com.acmedcare.framework.newim.InstanceNode.NodeType;
import com.acmedcare.framework.newim.protocol.Command.MasterClusterCommand;
import com.acmedcare.framework.newim.protocol.request.ClusterRegisterHeader;
import com.acmedcare.framework.newim.server.config.IMProperties;
import com.acmedcare.framework.newim.server.core.IMSession;
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
import com.google.common.collect.Maps;
import io.netty.channel.Channel;
import io.netty.util.concurrent.DefaultThreadFactory;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
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

  private final IMSession imSession;
  private final IMProperties imProperties;
  @Getter private RemoteMasterConnectorInstance remoteMasterConnectorInstance;

  public MasterConnector(IMProperties imProperties, IMSession imSession) {
    this.imProperties = imProperties;
    this.imSession = imSession;
  }

  public void start() {
    List<String> masterNodes = this.imProperties.getMasterNodes();
    if (masterNodes != null && masterNodes.size() > 0) {
      masterClusterLog.info("Ready master client connecting ...");
      remoteMasterConnectorInstance = newMasterConnectorInstance(masterNodes);
      remoteMasterConnectorInstance.start();
    }
  }

  public void shutdown() {
    masterClusterLog.info("shutdown master client connections.");
    remoteMasterConnectorInstance.shutdown();
  }

  private RemoteMasterConnectorInstance newMasterConnectorInstance(List<String> nodeAddresses) {
    List<InstanceNode> nodes = Lists.newArrayList();
    for (String address : nodeAddresses) {
      nodes.add(new InstanceNode(address, NodeType.MASTER, null));
    }

    RemoteMasterConnectorInstance instance = new RemoteMasterConnectorInstance();
    InstanceNode localNode =
        new InstanceNode(
            imProperties.getHost() + ":" + imProperties.getPort(), NodeType.CLUSTER, null);
    instance.setLocalNode(localNode);
    NettyClientConfig config = new NettyClientConfig();
    config.setEnableHeartbeat(false);
    config.setClientChannelMaxIdleTimeSeconds(40); // idle

    NettyRemotingSocketClient client =
        new NettyRemotingSocketClient(
            config,
            new ChannelEventListener() {
              @Override
              public void onChannelConnect(String remoteAddr, Channel channel) {
                masterClusterLog.info("Master Cluster Client[{}] is connected", remoteAddr);

                try {
                  instance.register(imProperties);
                } catch (Exception e) {
                  masterClusterLog.error("master client register exception", e);
                }
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

    client.updateNameServerAddressList(nodeAddresses);

    client.registerProcessor(
        MasterClusterCommand.MASTER_NOTICE_CLIENT_CHANNELS,
        new MasterNoticeClientChannelsRequestProcessor(imSession),
        null);

    client.registerProcessor(
        MasterClusterCommand.MASTER_PUSH_MESSAGES,
        new MasterPushMessageRequestProcessor(imSession),
        null);

    // set
    instance.setMasterNodes(nodes);
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

    private List<InstanceNode> masterNodes;
    private Map<String, Boolean> availableCache = Maps.newConcurrentMap();
    private InstanceNode localNode;
    /** 副本配置 */
    private NettyClientConfig nettyClientConfig;
    /** 副本客户端对象 */
    private NettyRemotingSocketClient nettyRemotingSocketClient;

    private ScheduledExecutorService heartbeatExecutor;
    private ScheduledExecutorService rollingPullClusterListExecutor;

    private Long heartbeatTimeoutMillis = 5000L; // 心跳请求超时时间

    void start() {
      if (nettyRemotingSocketClient != null) {
        nettyRemotingSocketClient.start();
        try {
          handshake();
        } catch (Exception e) {
          masterClusterLog.error("Master-Cluster-Client connect exception", e);
        }
      }
    }

    private void handshake() throws Exception {
      RemotingCommand handshakeRequest =
          RemotingCommand.createRequestCommand(MasterClusterCommand.CLUSTER_HANDSHAKE, null);
      for (InstanceNode masterNode : masterNodes) {
        nettyRemotingSocketClient.invokeOneway(masterNode.getHost(), handshakeRequest, 2000);
      }
    }

    void register(IMProperties imProperties) throws Exception {
      ClusterRegisterHeader header = new ClusterRegisterHeader();
      header.setClusterServerHost(localNode.getHost());

      // send register command
      RemotingCommand registerRequest =
          RemotingCommand.createRequestCommand(MasterClusterCommand.CLUSTER_REGISTER, header);

      registerRequest.setBody(JSON.toJSONBytes(imProperties.loadWssEndpoints()));

      for (InstanceNode masterNode : masterNodes) {
        nettyRemotingSocketClient.invokeAsync(
            masterNode.getHost(),
            registerRequest,
            2000,
            responseFuture -> {
              if (responseFuture.isSendRequestOK()) {
                RemotingCommand response = responseFuture.getResponseCommand();
                BizResult bizResult = JSON.parseObject(response.getBody(), BizResult.class);
                if (bizResult != null && bizResult.getCode() == 0) {
                  masterClusterLog.info(
                      "Master-Cluster-Client:{} register succeed. ", masterNode.getHost());
                  availableCache.put(masterNode.getHost(), true);
                  heartbeat();
                  syncClusterListTask();
                }
              }
            });
      }
    }

    private void syncClusterListTask() {
      if (rollingPullClusterListExecutor == null) {
        rollingPullClusterListExecutor =
            new ScheduledThreadPoolExecutor(1, new DefaultThreadFactory("rolling-pull-thread"));
        rollingPullClusterListExecutor.scheduleWithFixedDelay(
            () -> {
              Set<String> servers = availableCache.keySet();
              for (String server : servers) {
                try {
                  if (availableCache.get(server)) {
                    RemotingCommand pullRequest =
                        RemotingCommand.createRequestCommand(
                            MasterClusterCommand.CLUSTER_PULL_REPLICAS, null);
                    RemotingCommand response =
                        nettyRemotingSocketClient.invokeSync(server, pullRequest, 3000);
                    if (response != null) {
                      byte[] body = response.getBody();
                      if (body != null) {
                        Set<String> clusterReplicas = JSON.parseObject(body, Set.class);
                        if (clusterReplicas != null && clusterReplicas.size() > 0) {
                          System.out.println(
                              "获取到Cluster Replica节点列表:" + JSON.toJSONString(clusterReplicas));

                          break;
                        }
                      }
                    }
                  }
                } catch (Exception e) {
                  masterClusterLog.error(
                      "rolling pull cluster replicas server list failed with request:{} ,will try next",
                      server,
                      e);
                }
              }
            },
            11,
            30,
            TimeUnit.SECONDS);
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
              for (InstanceNode masterNode : masterNodes) {
                RemotingCommand heartbeat =
                    RemotingCommand.createRequestCommand(RemotingSysRequestCode.HEARTBEAT, null);
                try {
                  nettyRemotingSocketClient.invokeOneway(
                      masterNode.getHost(),
                      heartbeat,
                      heartbeatTimeoutMillis,
                      b -> {
                        if (b) {
                          availableCache.put(masterNode.getHost(), true);
                        }
                      });

                } catch (InterruptedException
                    | RemotingTooMuchRequestException
                    | RemotingSendRequestException
                    | RemotingTimeoutException
                    | RemotingConnectException e) {
                  masterClusterLog.warn(
                      "heartbeat thread interrupted exception or too much request exception or lose connection or request timeout , ignore");
                  availableCache.put(masterNode.getHost(), false);
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
      if (rollingPullClusterListExecutor != null) {
        ThreadKit.gracefulShutdown(rollingPullClusterListExecutor, 5, 10, TimeUnit.SECONDS);
      }
      if (nettyRemotingSocketClient != null) {
        nettyRemotingSocketClient.shutdown();
      }
    }
  }
}
