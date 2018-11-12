package com.acmedcare.framework.newim.server.core;

import com.acmedcare.framework.newim.protocol.Command.ClusterClientCommand;
import com.acmedcare.framework.newim.server.config.IMProperties;
import com.acmedcare.framework.newim.server.processor.DefaultIMProcessor;
import com.acmedcare.framework.newim.server.processor.RemotingClientRegisterAuthProcessor;
import com.acmedcare.tiffany.framework.remoting.ChannelEventListener;
import com.acmedcare.tiffany.framework.remoting.RemotingSocketClient;
import com.acmedcare.tiffany.framework.remoting.netty.NettyClientConfig;
import com.acmedcare.tiffany.framework.remoting.netty.NettyRemotingSocketClient;
import com.acmedcare.tiffany.framework.remoting.netty.NettyRemotingSocketServer;
import com.acmedcare.tiffany.framework.remoting.netty.NettyServerConfig;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import io.netty.channel.Channel;
import io.netty.util.concurrent.DefaultThreadFactory;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.ThreadPoolExecutor.CallerRunsPolicy;
import java.util.concurrent.TimeUnit;
import lombok.Getter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * New IM System Tcp Server
 *
 * @author <a href="mailto:iskp.me@gmail.com">Elve.Xu</a>
 * @version ${project.version} - 09/11/2018.
 */
@Component
public class NewIMTcpServer {

  private static final Logger LOG = LoggerFactory.getLogger(NewIMTcpServer.class);
  private static volatile boolean running = false;

  private final IMProperties imProperties;
  @Getter private NettyRemotingSocketServer imServer;
  private NettyServerConfig imServerConfig;
  private IMSession imSession;
  /** Master Connector Instance */
  private MasterConnector masterConnector;

  /** Default Executor */
  private ExecutorService defaultExecutor =
      new ThreadPoolExecutor(
          4,
          16,
          5000L,
          TimeUnit.MILLISECONDS,
          new LinkedBlockingQueue<Runnable>(64),
          new DefaultThreadFactory("new-im-netty-default-processor-executor-"),
          new CallerRunsPolicy());

  private ScheduledExecutorService channelConnectionChecker;

  @Autowired
  public NewIMTcpServer(IMProperties imProperties) {
    this.imProperties = imProperties;

    // build imServer config
    this.imServerConfig = new NettyServerConfig();
    this.imServerConfig.setListenPort(this.imProperties.getPort());
  }

  public void startup(long delay) {
    if (running) {
      LOG.warn("[NEW-IM] imServer already startup ~, ignore.");
      return;
    }

    LOG.info("[NEW-IM] Startup IM Server Instance listen on port :{}", imProperties.getPort());
    startupImServer(delay);

    LOG.info("[NEW-IM] Startup Master Connector Instance(s) ");
    startupMasterConnector(delay);

    LOG.info("[NEW-IM] System Startup Finished~");
  }

  public void shutdown(boolean immediately) {

    //
    running = false;
  }

  /** start im imServer */
  private void startupImServer(long delay) {
    imSession = new IMSession();
    if (imServer == null) {
      imServer =
          new NettyRemotingSocketServer(
              imServerConfig,
              new ChannelEventListener() {
                @Override
                public void onChannelConnect(String remoteAddr, Channel channel) {
                  LOG.debug("Remoting[{}] is connected", remoteAddr);
                }

                @Override
                public void onChannelClose(String remoteAddr, Channel channel) {
                  LOG.debug("Remoting[{}] is closed", remoteAddr);
                }

                @Override
                public void onChannelException(String remoteAddr, Channel channel) {
                  LOG.debug("Remoting[{}] is exception ,closing ..", remoteAddr);
                  try {
                    channel.close();
                  } catch (Exception ignore) {
                  }
                }

                @Override
                public void onChannelIdle(String remoteAddr, Channel channel) {
                  LOG.debug("Remoting[{}] is idle", remoteAddr);
                }
              });
    }

    // bind processors
    // default processor
    imServer.registerDefaultProcessor(new DefaultIMProcessor(), defaultExecutor);

    // TODO biz processors

    // register server process cluster register request
    imServer.registerProcessor(
        ClusterClientCommand.CLIENT_AUTH,
        new RemotingClientRegisterAuthProcessor(imSession),
        defaultExecutor);

    // start imServer
    imServer.start();

    // TODO start imServer checker

    // flag
    running = true;
  }

  /** start connector */
  private void startupMasterConnector(long delay) {

    // master connector instance
    masterConnector = new MasterConnector(imProperties);

    // check target master nodes config
    List<String> masterNodes = imProperties.getMasterNodes();
    if (masterNodes == null || masterNodes.size() == 0) {
      LOG.info("[NEW-IM] Master(s) Address is not set.");
    }

    // start up
    masterConnector.startup(delay);
  }
}

/**
 * Master Connector For IM Server Instance
 *
 * @see IMProperties#masterNodes Master Node List
 * @see IMProperties#masterHeartbeat Master Heaerbeat Value
 */
class MasterConnector {

  private static final Logger LOG = LoggerFactory.getLogger(MasterConnector.class);
  /** Master Config Properties */
  private IMProperties properties;
  /** Master Connector Cache */
  private Map<String, RemotingSocketClient> masterConnectorCache = Maps.newHashMap();

  private Map<String, NettyClientConfig> masterConfigCache = Maps.newHashMap();

  public MasterConnector(IMProperties properties) {
    this.properties = properties;
  }

  void startup(long delay) {
    // TODO foreach init connector
    LOG.info("[NEW-IM] [MASTER-CONNECTOR] begin to init master connectors.");
    List<String> nodes = properties.getMasterNodes();

    // async start client connect
    final CountDownLatch clientConnectLatch = new CountDownLatch(nodes.size());

    for (String node : nodes) {
      // init config
      NettyClientConfig nettyClientConfig = new NettyClientConfig();
      nettyClientConfig.setEnableHeartbeat(true);
      nettyClientConfig.setClientChannelMaxIdleTimeSeconds(60); // 60秒空闲

      masterConfigCache.put(node, nettyClientConfig);

      // init client
      NettyRemotingSocketClient nettyRemotingSocketClient =
          new NettyRemotingSocketClient(
              nettyClientConfig,
              new ChannelEventListener() {
                @Override
                public void onChannelConnect(String remoteAddr, Channel channel) {
                  LOG.debug("Master Connector[{}] is connected", remoteAddr);
                  clientConnectLatch.countDown();
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
                    masterConfigCache.remove(remoteAddr);
                    masterConnectorCache.remove(remoteAddr);
                  }
                }

                @Override
                public void onChannelIdle(String remoteAddr, Channel channel) {
                  LOG.debug("Master Connector[{}] is idle", remoteAddr);
                }
              });
      // update target master address
      nettyRemotingSocketClient.updateNameServerAddressList(Lists.newArrayList(node));

      // TODO register processor

      masterConnectorCache.put(node, nettyRemotingSocketClient);
    }

    // start
    masterConnectorCache.forEach((node, client) -> client.start());

    // release
    try {
      // 等待全部启动结束
      LOG.info("[NEW-IM] Waiting master connector connecting..., Wait Timeout: {}", 60);
      clientConnectLatch.await(60, TimeUnit.SECONDS);
      // goon...
      LOG.info("[NEW-IM] Master Connector(s) 全部启动完成.");

    } catch (InterruptedException e) {
      LOG.error("[NEW-IM] 异步启动Master Connector异常", e);
    }
  }

  void shutdown(boolean immediately) {

    // TODO shutdown master connector

  }

  /**
   * 同步 IMServer Client Session 到 MasterServer
   *
   * <p>
   */
  public void syncIMServerSessions(Object object) {
    // TODO 同步通信服务器链接数据
  }

  /**
   * 拉取 CLuster Nodes列表
   *
   * <p>
   */
  public void pullClusterNodesList() {
    // TODO 拉取通讯节点列表
  }
}

/**
 * Cluster Connector(s) For Clusters
 *
 * @see MasterConnector#pullClusterNodesList() dynamic connector list
 */
class CLusterConnector {




}
