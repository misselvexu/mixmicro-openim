package com.acmedcare.framework.newim.server.mq;

import com.acmedcare.framework.aorp.client.AorpClient;
import com.acmedcare.framework.kits.thread.DefaultThreadFactory;
import com.acmedcare.framework.kits.thread.ThreadKit;
import com.acmedcare.framework.newim.Message;
import com.acmedcare.framework.newim.Message.MQMessage;
import com.acmedcare.framework.newim.Message.MessageType;
import com.acmedcare.framework.newim.SessionBean;
import com.acmedcare.framework.newim.server.IdService;
import com.acmedcare.framework.newim.server.Server;
import com.acmedcare.framework.newim.server.master.connector.MasterConnector;
import com.acmedcare.framework.newim.server.master.connector.MasterConnectorHandler;
import com.acmedcare.framework.newim.server.mq.processor.MQProcessor;
import com.acmedcare.framework.newim.server.mq.service.MQService;
import com.acmedcare.framework.newim.spi.Extension;
import com.acmedcare.tiffany.framework.remoting.ChannelEventListener;
import com.acmedcare.tiffany.framework.remoting.netty.NettyRemotingSocketServer;
import com.acmedcare.tiffany.framework.remoting.netty.NettyServerConfig;
import com.alibaba.fastjson.JSON;
import com.google.common.collect.Lists;
import io.netty.channel.Channel;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.ThreadPoolExecutor.CallerRunsPolicy;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * MQ Server
 *
 * @author <a href="mailto:iskp.me@gmail.com">Elve.Xu</a>
 * @version ${project.version} - 2018-12-12.
 */
@Extension("mqserver")
public class MQServer implements Server {

  private static final Logger logger = LoggerFactory.getLogger(MQServer.class);
  private static volatile AtomicBoolean startup = new AtomicBoolean(false);

  @Autowired private MQServerProperties mqServerProperties;
  @Autowired private MQService mqService;
  @Autowired private MasterConnector masterConnector;
  @Autowired private AorpClient aorpClient;
  @Autowired private DefaultMQReplicaService defaultMQReplicaService;

  @Autowired(required = false)
  private IdService idService;

  private NettyRemotingSocketServer server;
  private NettyServerConfig config;
  private ExecutorService defaultProcessorExecutor; // processor
  private int corePoolSize = Runtime.getRuntime().availableProcessors() << 1;
  private int maximumPoolSize = corePoolSize << 2;
  private MQContext context;

  /**
   * Server Startup Method
   *
   * @return server instance
   */
  @Override
  public Server startup() {

    if (startup.compareAndSet(false, true)) {
      logger.info("[MQServer] ready to startup mq-server...");
      logger.info("Configuration: {}", JSON.toJSONString(mqServerProperties));

      context = new MQContext(mqServerProperties);
      defaultMQReplicaService.setParentContext(context);

      config = new NettyServerConfig();
      config.setListenPort(this.mqServerProperties.getPort());
      config.setServerChannelMaxIdleTimeSeconds(mqServerProperties.getIdleTime());
      logger.info("[MQServer] build server config :{} ", config);

      defaultProcessorExecutor =
          new ThreadPoolExecutor(
              corePoolSize,
              maximumPoolSize,
              5000L,
              TimeUnit.MILLISECONDS,
              new LinkedBlockingQueue<>(),
              new DefaultThreadFactory("MQ-SERVER-DEFAULT-PROCESSOR-EXECUTOR-"),
              new CallerRunsPolicy());
      logger.info(
          "[MQServer] build server default processor executor :{} ", defaultProcessorExecutor);

      server =
          new NettyRemotingSocketServer(
              config,
              new ChannelEventListener() {
                @Override
                public void onChannelConnect(String address, Channel channel) {}

                @Override
                public void onChannelClose(String address, Channel channel) {}

                @Override
                public void onChannelException(String address, Channel channel) {}

                @Override
                public void onChannelIdle(String address, Channel channel) {}
              });

      logger.info("[MQServer] create new mq server instance :{} ", server);

      MQProcessor processor = new MQProcessor(context, mqService, aorpClient, idService);
      server.registerDefaultProcessor(processor, defaultProcessorExecutor);
      logger.info("[MQServer] register-ed default processor :{} ", processor);

      server.start();
      logger.info("[MQServer] server started , on port: {}", config.getListenPort());

      logger.info("[MQServer] starting up master connector ");
      masterConnector.startup(new MQServerMasterConnectorHandler());

    } else {
      logger.warn("[MQServer] mq server is startup-ed.");
    }
    return this;
  }

  /**
   * Shutdown Server
   *
   * <p>
   */
  @Override
  public void shutdown() {
    logger.info("[MQServer] ready to shutdown mq-server...");
    if (startup.compareAndSet(true, false)) {
      try {
        server.shutdown();
      } catch (Exception ignored) {
      } finally {
        server = null;
        config = null;
      }

      if (defaultProcessorExecutor != null) {
        try {
          ThreadKit.gracefulShutdown(defaultProcessorExecutor, 5, 5, TimeUnit.SECONDS);
        } catch (Exception ignore) {
        } finally {
          defaultProcessorExecutor = null;
        }
      }
    }
  }

  private class MQServerMasterConnectorHandler implements MasterConnectorHandler {

    @Override
    public void processOnlineConnections(
        Set<SessionBean> passportsConnections, Set<SessionBean> devicesConnections) {
      logger.info(
          "Rvd Master Connections : {} , {}",
          passportsConnections.size(),
          devicesConnections.size());
    }

    @Override
    public void processMasterForwardMessage(
        String namespace, MessageType messageType, Message message) {
      logger.info("Rvd Master Message : {} , {} ,{}", namespace, messageType, message.toString());
      if (message instanceof MQMessage) {
        MQMessage mqMessage = (MQMessage) message;
        logger.info(">>>> Master forward mq message to subscribe topic's client(s)");
        mqService.broadcastTopicMessages(context, mqMessage);
      }
    }

    @Override
    public void onClusterReplicas(Set<String> clusterReplicas) {
      logger.info("Rvd Cluster Replicas Data : {}", clusterReplicas);
      context.refreshReplicas(clusterReplicas);
    }

    @Override
    public List<SessionBean> getOnlinePassports() {
      System.out.println("获取在线用户");
      return Lists.newArrayList();
    }

    @Override
    public List<SessionBean> getOnlineDevices() {
      System.out.println("获取在线设备");
      return Lists.newArrayList();
    }

    @Override
    public Object getWssEndpoints() {
      return null;
    }
  }
}
