package com.acmedcare.framework.newim.server.core.connector;

import com.acmedcare.framework.kits.thread.DefaultThreadFactory;
import com.acmedcare.framework.kits.thread.ThreadKit;
import com.acmedcare.framework.newim.server.config.IMProperties;
import com.acmedcare.tiffany.framework.remoting.ChannelEventListener;
import com.acmedcare.tiffany.framework.remoting.RemotingSocketClient;
import com.acmedcare.tiffany.framework.remoting.netty.NettyClientConfig;
import com.acmedcare.tiffany.framework.remoting.netty.NettyRemotingSocketClient;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import io.netty.channel.Channel;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import lombok.Getter;
import lombok.Setter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Master Connector For IM Server Instance
 *
 * @author <a href="mailto:iskp.me@gmail.com">Elve.Xu</a>
 * @version ${project.version} - 12/11/2018.
 * @see IMProperties#masterNodes Master Node List
 * @see IMProperties#masterHeartbeat Master Heaerbeat Value
 */
public class MasterConnector {

  private static final Logger LOG = LoggerFactory.getLogger(MasterConnector.class);
  /** Master Config Properties */
  private IMProperties properties;
  /** Master Connector Cache */
  private Map<String, Connector> masterConnectorCache = Maps.newHashMap();

  public MasterConnector(IMProperties properties) {
    this.properties = properties;
  }

  /**
   * 上报本机连接的客户端数据(定时上报)
   *
   * <p>
   */
  public static void uploadConnectedClientSessions() {
    // TODO 同步通信服务器链接数据

  }

  public static void downloadClusterConnectedClientSessions() {}

  public void startup(long delay) {

    LOG.info("[NEW-IM] [MASTER-CONNECTOR] begin to init master connectors.");
    List<String> nodes = properties.getMasterNodes();

    // async start client connect
    final CountDownLatch clientConnectLatch = new CountDownLatch(nodes.size());

    for (String node : nodes) {

      final Connector connector = new Connector(node);

      // connect
      connect(connector, clientConnectLatch);
    }

    // start
    masterConnectorCache.forEach((node, client) -> client.getClient().start());

    try {
      // 等待全部启动结束
      LOG.info("[NEW-IM] Waiting master connector connecting..., Wait Timeout: {}", 60);
      clientConnectLatch.await(60, TimeUnit.SECONDS);

    } catch (InterruptedException e) {
      LOG.error("[NEW-IM] 异步启动Master Connector异常", e);
    }
  }

  /**
   * Connect Master Server
   *
   * @param connector connector instance
   * @param countDownLatch wait lock
   */
  private void connect(Connector connector, CountDownLatch countDownLatch) {
    // init config
    NettyClientConfig nettyClientConfig = new NettyClientConfig();
    nettyClientConfig.setEnableHeartbeat(true);
    nettyClientConfig.setClientChannelMaxIdleTimeSeconds(60); // 60秒空闲

    // init client
    NettyRemotingSocketClient nettyRemotingSocketClient =
        new NettyRemotingSocketClient(
            nettyClientConfig,
            new ChannelEventListener() {
              @Override
              public void onChannelConnect(String remoteAddr, Channel channel) {
                LOG.debug("Master Connector[{}] is connected", remoteAddr);
                countDownLatch.countDown();
                connector.startupTask();
              }

              @Override
              public void onChannelClose(String remoteAddr, Channel channel) {
                LOG.debug("Master Connector[{}] is closed", remoteAddr);
              }

              @Override
              public void onChannelException(String remoteAddr, Channel channel) {
                LOG.debug("Master Connector[{}] is exception ,closing ..", remoteAddr);
                try {
                  channel.close();
                } catch (Exception ignore) {
                } finally {
                  masterConnectorCache.remove(remoteAddr);
                }
              }

              @Override
              public void onChannelIdle(String remoteAddr, Channel channel) {
                LOG.debug("Master Connector[{}] is idle", remoteAddr);
              }
            });
    // update target master address
    nettyRemotingSocketClient.updateNameServerAddressList(
        Lists.newArrayList(connector.getRemoteAddress()));
    // TODO register processor

    connector.setConfig(nettyClientConfig);
    connector.setClient(nettyRemotingSocketClient);
    // cache
    masterConnectorCache.put(connector.getRemoteAddress(), connector);
  }

  public void shutdown(boolean immediately) {

    // TODO shutdown master connector
  }

  /**
   * 拉取 CLuster Nodes列表
   *
   * @param masterServer Master服务器链接对象
   *     <p>
   */
  public void pullClusterNodesList(NettyRemotingSocketClient masterServer) {
    // TODO 拉取通讯节点列表

  }

  @Getter
  @Setter
  public static class Connector {
    private String remoteAddress;
    private RemotingSocketClient client;
    private NettyClientConfig config;
    private ScheduledExecutorService uploadSessionExecutor;
    private ScheduledExecutorService pullClusterNodesExecutor;

    public Connector(String remoteAddress) {
      this.remoteAddress = remoteAddress;
      this.uploadSessionExecutor =
          new ScheduledThreadPoolExecutor(1, new DefaultThreadFactory("SYNC-SESSION-THREAD-POOL"));
      this.pullClusterNodesExecutor =
          new ScheduledThreadPoolExecutor(
              1, new DefaultThreadFactory("PULL-CLUSTER-NODES-THREAD-POOL"));
    }

    public Connector(String remoteAddress, NettyClientConfig config, RemotingSocketClient client) {
      this(remoteAddress);
      this.config = config;
      this.client = client;
    }

    public void release() {
      if (uploadSessionExecutor != null) {
        ThreadKit.gracefulShutdown(uploadSessionExecutor, 10, 20, TimeUnit.SECONDS);
      }
      if (pullClusterNodesExecutor != null) {
        ThreadKit.gracefulShutdown(pullClusterNodesExecutor, 10, 20, TimeUnit.SECONDS);
      }
    }

    public void startupTask() {
      LOG.info("[NEW-IM] Master Connector(s) 全部启动完成.");
      LOG.info("[NEW-IM] 启动Session同步定时线程,参数:[5-10-S]");
      uploadSessionExecutor.scheduleAtFixedRate(
          () -> {
            try {
              LOG.info("[NEW-IN-SYNC-SESSION] 同步SESSION操作");
              uploadConnectedClientSessions();
            } catch (Exception e) {
              LOG.error("[NEW-IN-SYNC-SESSION] 同步SESSION操作异常,等待再次同步", e);
            }
          },
          5,
          10,
          TimeUnit.SECONDS);

      LOG.info("[NEW-IM] 启动拉取通讯节点列表定时线程,参数:[10-30-S]");
      pullClusterNodesExecutor.scheduleAtFixedRate(
          () -> {
            try {
              LOG.info("[NEW-IM-PULL-CLUSTER-NODES] 拉取通讯节点操作");
              // TODO 拉取通讯节点

            } catch (Exception e) {
              LOG.error("[NEW-IM-PULL-CLUSTER-NODES] 拉取通讯节点操作异常,等待再次拉取", e);
            }
          },
          10,
          30,
          TimeUnit.SECONDS);
    }
  }
}
