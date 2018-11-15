package com.acmedcare.framework.newim.server.core;

import com.acmedcare.framework.newim.protocol.Command.ClusterClientCommand;
import com.acmedcare.framework.newim.protocol.Command.ClusterWithClusterCommand;
import com.acmedcare.framework.newim.server.config.IMProperties;
import com.acmedcare.framework.newim.server.processor.ClusterForwardMessageRequestProcessor;
import com.acmedcare.framework.newim.server.processor.ClusterRegisterRequestProcessor;
import com.acmedcare.framework.newim.server.processor.DefaultIMProcessor;
import com.acmedcare.framework.newim.server.processor.RemotingClientPullSessionProcessor;
import com.acmedcare.framework.newim.server.processor.RemotingClientPushMessageProcessor;
import com.acmedcare.framework.newim.server.processor.RemotingClientRegisterAuthProcessor;
import com.acmedcare.framework.newim.server.service.MessageService;
import com.acmedcare.framework.newim.server.service.RemotingAuthService;
import com.acmedcare.tiffany.framework.remoting.ChannelEventListener;
import com.acmedcare.tiffany.framework.remoting.netty.NettyRemotingSocketServer;
import com.acmedcare.tiffany.framework.remoting.netty.NettyServerConfig;
import io.netty.channel.Channel;
import io.netty.util.concurrent.DefaultThreadFactory;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.ThreadPoolExecutor.CallerRunsPolicy;
import java.util.concurrent.TimeUnit;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * New IM Server Bootstrap Class
 *
 * @author <a href="mailto:iskp.me@gmail.com">Elve.Xu</a>
 * @version ${project.version} - 09/11/2018.
 */
@Component
public class NewIMServerBootstrap {

  private static final Logger LOG = LoggerFactory.getLogger(NewIMServerBootstrap.class);
  private static volatile boolean running = false;

  // ======================== Autowired Instance Properties =============================

  private final RemotingAuthService remotingAuthService;
  private final MessageService messageService;
  private final IMProperties imProperties;
  // ======================== Autowired Instance Properties =============================//

  // ======================== Local Properties =============================

  private NettyRemotingSocketServer imServer;
  private NettyServerConfig imServerConfig;

  private NettyRemotingSocketServer clusterServer;
  private NettyServerConfig clusterServerConfig;
  private IMSession imSession;
  // ======================== Local Properties =============================//

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

  private ScheduledExecutorService channelConnectionChecker;

  @Autowired
  public NewIMServerBootstrap(
      IMProperties imProperties,
      RemotingAuthService remotingAuthService,
      MessageService messageService) {
    this.imProperties = imProperties;
    this.remotingAuthService = remotingAuthService;
    this.messageService = messageService;

    // build imServer config
    this.imServerConfig = new NettyServerConfig();
    this.imServerConfig.setListenPort(this.imProperties.getPort());
  }

  public void startup(long delay) {
    if (running) {
      LOG.warn("[NEW-IM] Servers already startup ~, ignore.");
      return;
    }

    LOG.info("[NEW-IM] Startup IM Server listen on port :{}", imProperties.getPort());
    startupIMMainServer(delay);

    LOG.info(
        "[NEW-IM] Startup IM Inner Cluster Server listen on port :{}",
        imProperties.getClusterPort());
    startupInnerClusterServer(delay);

    LOG.info("[NEW-IM] System Startup Finished~");
    running = true;
  }

  public void shutdown(boolean immediately) {

    // TODO 停止服务,释放资源

    running = false;
  }

  private void startupInnerClusterServer(long delay) {
    clusterServerConfig = new NettyServerConfig();
    clusterServerConfig.setListenPort(imProperties.getClusterPort());
    clusterServer =
        new NettyRemotingSocketServer(
            clusterServerConfig,
            new ChannelEventListener() {
              @Override
              public void onChannelConnect(String remoteAddr, Channel channel) {
                LOG.debug("Cluster Remoting[{}] is connected", remoteAddr);
              }

              @Override
              public void onChannelClose(String remoteAddr, Channel channel) {
                LOG.debug("Cluster Remoting[{}] is closed", remoteAddr);
              }

              @Override
              public void onChannelException(String remoteAddr, Channel channel) {
                LOG.debug("Cluster Remoting[{}] is exception ,closing ..", remoteAddr);
                try {
                  channel.close();
                } catch (Exception ignore) {
                }
              }

              @Override
              public void onChannelIdle(String remoteAddr, Channel channel) {
                LOG.debug("Cluster Remoting[{}] is idle", remoteAddr);
              }
            });

    clusterServer.registerDefaultProcessor(new DefaultIMProcessor(), defaultExecutor);
    clusterServer.registerProcessor(
        ClusterWithClusterCommand.CLUSTER_REGISTER, new ClusterRegisterRequestProcessor(), null);
    clusterServer.registerProcessor(
        ClusterWithClusterCommand.CLUSTER_FORWARD_MESSAGE,
        new ClusterForwardMessageRequestProcessor(imSession),
        null);
    clusterServer.start();
  }

  /** start im imServer */
  private void startupIMMainServer(long delay) {
    if (imServer == null) {
      imServer =
          new NettyRemotingSocketServer(
              imServerConfig,
              new ChannelEventListener() {
                @Override
                public void onChannelConnect(String remoteAddr, Channel channel) {
                  LOG.debug("Client Remoting[{}] is connected", remoteAddr);
                }

                @Override
                public void onChannelClose(String remoteAddr, Channel channel) {
                  LOG.debug("Client Remoting[{}] is closed", remoteAddr);
                }

                @Override
                public void onChannelException(String remoteAddr, Channel channel) {
                  LOG.debug("Client Remoting[{}] is exception ,closing ..", remoteAddr);
                  try {
                    channel.close();
                  } catch (Exception ignore) {
                  }
                }

                @Override
                public void onChannelIdle(String remoteAddr, Channel channel) {
                  LOG.debug("Client Remoting[{}] is idle", remoteAddr);
                }
              });
    }

    imSession = new IMSession(imServer);

    // bind processors
    // default processor
    imServer.registerDefaultProcessor(new DefaultIMProcessor(), defaultExecutor);

    // TODO biz processors

    // register server process cluster register request
    imServer.registerProcessor(
        ClusterClientCommand.CLIENT_AUTH,
        new RemotingClientRegisterAuthProcessor(imSession, remotingAuthService),
        defaultExecutor);

    imServer.registerProcessor(
        ClusterClientCommand.CLIENT_PULL_OWNER_SESSIONS,
        new RemotingClientPullSessionProcessor(imSession),
        null);
    imServer.registerProcessor(
        ClusterClientCommand.CLIENT_PUSH_MESSAGE,
        new RemotingClientPushMessageProcessor(imSession, messageService),
        null);

    //    imServer.registerProcessor(ClusterClientCommand.CLIENT_PULL_OWNER_GROUPS, new
    // PullGroupProcessor(), null);
    //    imServer.registerProcessor(ClusterClientCommand.CLIENT_PULL_MESSAGE, new
    // PullMessageProcessor(), null);
    //    imServer.registerProcessor(ClusterClientCommand.CLIENT_PUSH_MESSAGE_READ_STATUS, new
    // PushMessageReadStatusProcessor(), null);
    //    imServer.registerProcessor(ClusterClientCommand.CLIENT_PULL_SESSION_STATUS, new
    // PullSessionStatusProcessor(), null);

    // start imServer
    imServer.start();

    // TODO start imServer checker
  }
}




