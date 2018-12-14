package com.acmedcare.framework.newim.server.mq;

import com.acmedcare.framework.kits.thread.DefaultThreadFactory;
import com.acmedcare.framework.kits.thread.ThreadKit;
import com.acmedcare.framework.newim.server.Server;
import com.acmedcare.framework.newim.server.mq.processor.MQProcessor;
import com.acmedcare.framework.newim.server.mq.service.MQService;
import com.acmedcare.framework.newim.spi.Extension;
import com.acmedcare.tiffany.framework.remoting.ChannelEventListener;
import com.acmedcare.tiffany.framework.remoting.netty.NettyRemotingSocketServer;
import com.acmedcare.tiffany.framework.remoting.netty.NettyServerConfig;
import com.alibaba.fastjson.JSON;
import io.netty.channel.Channel;
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

  private NettyRemotingSocketServer server;
  private NettyServerConfig config;
  private ExecutorService defaultProcessorExecutor; // processor
  private int corePoolSize = Runtime.getRuntime().availableProcessors() << 1;
  private int maximumPoolSize = corePoolSize << 2;

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

      MQProcessor processor = new MQProcessor(mqService);
      server.registerDefaultProcessor(processor, defaultProcessorExecutor);
      logger.info("[MQServer] register-ed default processor :{} ", processor);

      server.start();
      logger.info("[MQServer] server started , on port: {}", config.getListenPort());

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
}
