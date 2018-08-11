package com.acmedcare.microservices.im.core;

import com.acmedcare.microservices.im.biz.BizCode;
import com.acmedcare.microservices.im.core.processors.AuthProcessor;
import com.acmedcare.microservices.im.core.processors.ClientPushMessage;
import com.acmedcare.microservices.im.core.processors.DefaultProcessor;
import com.acmedcare.microservices.im.core.processors.PullGroupProcessor;
import com.acmedcare.microservices.im.core.processors.PullMessageProcessor;
import com.acmedcare.microservices.im.core.processors.PullSessionProcessor;
import com.acmedcare.microservices.im.core.processors.PullSessionStatusProcessor;
import com.acmedcare.microservices.im.core.processors.PushMessageReadStatusProcessor;
import com.acmedcare.microservices.im.kits.DefaultThreadFactory;
import com.acmedcare.microservices.im.kits.ThreadKit;
import com.acmedcare.tiffany.framework.remoting.ChannelEventListener;
import com.acmedcare.tiffany.framework.remoting.common.RemotingHelper;
import com.acmedcare.tiffany.framework.remoting.netty.NettyRemotingSocketServer;
import com.acmedcare.tiffany.framework.remoting.netty.NettyRequestProcessor;
import com.acmedcare.tiffany.framework.remoting.netty.NettyServerConfig;
import com.acmedcare.tiffany.framework.remoting.protocol.RemotingCommand;
import io.netty.channel.Channel;
import io.netty.channel.ChannelHandlerContext;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.ThreadPoolExecutor.AbortPolicy;
import java.util.concurrent.TimeUnit;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Main Server
 *
 * @author Elve.Xu [iskp.me<at>gmail.com]
 * @version v1.0 - 02/07/2018.
 */
@SuppressWarnings("ALL")
public final class TiffanySocketServer {
  /** Logger * */
  private static final Logger LOGGER = LoggerFactory.getLogger(TiffanySocketServer.class);
  /** running flag * */
  private static volatile boolean running = false;
  /** Socket Server Instance * */
  private NettyRemotingSocketServer server;

  private NettyServerConfig nettyServerConfig;
  /** Default Executor */
  private ExecutorService defaultExecutor =
      new ThreadPoolExecutor(
          1,
          1,
          0L,
          TimeUnit.MILLISECONDS,
          new LinkedBlockingQueue<Runnable>(32),
          new DefaultThreadFactory("tiffany-netty-default-processor-executor-"),
          new AbortPolicy());

  private ScheduledExecutorService channelConnectionChecker;

  public TiffanySocketServer(NettyServerConfig nettyServerConfig) {
    if (nettyServerConfig == null) {
      nettyServerConfig = new NettyServerConfig();
    }
    this.nettyServerConfig = nettyServerConfig;
  }

  /** Start run Server */
  public synchronized void start() {

    if (server == null) {
      server =
          new NettyRemotingSocketServer(
              nettyServerConfig,
              new ChannelEventListener() {
                @Override
                public void onChannelConnect(String remoteAddr, Channel channel) {
                  LOGGER.debug("Remoting[{}] is connected", remoteAddr);
                }

                @Override
                public void onChannelClose(String remoteAddr, Channel channel) {
                  LOGGER.debug("Remoting[{}] is closed", remoteAddr);
                }

                @Override
                public void onChannelException(String remoteAddr, Channel channel) {
                  LOGGER.debug("Remoting[{}] is exception ,closing ..", remoteAddr);
                  try {
                    channel.close();
                  } catch (Exception ignore) {
                  }
                }

                @Override
                public void onChannelIdle(String remoteAddr, Channel channel) {
                  LOGGER.debug("Remoting[{}] is idle", remoteAddr);
                }
              });
    }

    // register server processor
    server.registerDefaultProcessor(new DefaultProcessor(), null);

    // register auth processor
    server.registerProcessor(BizCode.AUTH, new AuthProcessor(), null);

    server.registerProcessor(BizCode.CLIENT_PULL_OWNER_SESSIONS, new PullSessionProcessor(), null);
    server.registerProcessor(BizCode.CLIENT_PULL_OWNER_GROUPS, new PullGroupProcessor(), null);
    server.registerProcessor(BizCode.CLIENT_PULL_MESSAGE, new PullMessageProcessor(), null);
    server.registerProcessor(
        BizCode.CLIENT_PUSH_MESSAGE_READ_STATUS, new PushMessageReadStatusProcessor(), null);
    server.registerProcessor(
        BizCode.CLIENT_PULL_SESSION_STATUS, new PullSessionStatusProcessor(), null);

    server.registerProcessor(BizCode.CLIENT_PUSH_MESSAGE, new ClientPushMessage(), null);

    // resgiter heartbeat processor
    server.registerProcessor(
        BizCode.HEARTBEAT,
        new NettyRequestProcessor() {
          @Override
          public RemotingCommand processRequest(
              ChannelHandlerContext channelHandlerContext, RemotingCommand remotingCommand)
              throws Exception {
            // processor client heartbeat
            RemotingCommand response =
                RemotingCommand.createResponseCommand(remotingCommand.getCode(), "PONG");

            LOGGER.trace(
                "remote ack heartbeat , address :{}",
                RemotingHelper.parseChannelRemoteAddr(channelHandlerContext.channel()));

            return response;
          }

          @Override
          public boolean rejectRequest() {
            return false;
          }
        },
        null);

    if (!running) {
      running = true;
      server.start();
    }

    // delay start schedule
    if (this.channelConnectionChecker == null) {
      channelConnectionChecker =
          new ScheduledThreadPoolExecutor(1, new DefaultThreadFactory("clients-channel-checker"));
    }

    // start
    this.channelConnectionChecker.scheduleAtFixedRate(
        new Runnable() {
          @Override
          public void run() {
            try {
              ServerFacade.scheduleCleanCaches();
            } catch (Exception ignore) {
            }
          }
        },
        10,
        10,
        TimeUnit.SECONDS);
  }

  public void destory() {

    if (defaultExecutor != null) {
      ThreadKit.gracefulShutdown(defaultExecutor, 10, 10, TimeUnit.SECONDS);
    }

    if (this.channelConnectionChecker != null) {
      ThreadKit.gracefulShutdown(channelConnectionChecker, 10, 10, TimeUnit.SECONDS);
    }

    ServerFacade.destory();
  }
}
