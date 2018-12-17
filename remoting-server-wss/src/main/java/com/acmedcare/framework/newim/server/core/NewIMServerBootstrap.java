package com.acmedcare.framework.newim.server.core;

import static com.acmedcare.framework.newim.server.ClusterLogger.imServerLog;
import static com.acmedcare.framework.newim.server.ClusterLogger.innerReplicaServerLog;

import com.acmedcare.framework.newim.protocol.Command.ClusterClientCommand;
import com.acmedcare.framework.newim.protocol.Command.ClusterWithClusterCommand;
import com.acmedcare.framework.newim.server.config.IMProperties;
import com.acmedcare.framework.newim.server.processor.ClusterForwardMessageRequestProcessor;
import com.acmedcare.framework.newim.server.processor.DefaultIMProcessor;
import com.acmedcare.framework.newim.server.processor.RemotingClientJoinOrLeaveGroupProcessor;
import com.acmedcare.framework.newim.server.processor.RemotingClientPullGroupMembersOnlineStatusProcessor;
import com.acmedcare.framework.newim.server.processor.RemotingClientPullGroupMembersProcessor;
import com.acmedcare.framework.newim.server.processor.RemotingClientPullGroupMessageReadStatusProcessor;
import com.acmedcare.framework.newim.server.processor.RemotingClientPullGroupProcessor;
import com.acmedcare.framework.newim.server.processor.RemotingClientPullMessageProcessor;
import com.acmedcare.framework.newim.server.processor.RemotingClientPushMessageProcessor;
import com.acmedcare.framework.newim.server.processor.RemotingClientPushMessageReadStatusProcessor;
import com.acmedcare.framework.newim.server.processor.RemotingClientRegisterAuthProcessor;
import com.acmedcare.framework.newim.server.service.GroupService;
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

/**
 * New IM Server Bootstrap Class
 *
 * @author <a href="mailto:iskp.me@gmail.com">Elve.Xu</a>
 * @version ${project.version} - 09/11/2018.
 */
public class NewIMServerBootstrap {

  private static volatile boolean running = false;

  // ======================== Autowired Instance Properties =============================

  private final RemotingAuthService remotingAuthService;
  private final MessageService messageService;
  private final GroupService groupService;
  private final IMProperties imProperties;
  // ======================== Autowired Instance Properties =============================//

  // ======================== Local Properties =============================
  private final IMSession imSession;
  private NettyRemotingSocketServer imServer;
  private NettyServerConfig imServerConfig;
  private NettyRemotingSocketServer clusterServer;
  private NettyServerConfig clusterServerConfig;
  private ClusterReplicaSession clusterReplicaSession;
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

  public NewIMServerBootstrap(
      IMProperties imProperties,
      RemotingAuthService remotingAuthService,
      MessageService messageService,
      GroupService groupService,
      IMSession imSession) {
    this.imProperties = imProperties;
    this.remotingAuthService = remotingAuthService;
    this.messageService = messageService;
    this.groupService = groupService;
    this.imSession = imSession;

    // build imServer config
    this.imServerConfig = new NettyServerConfig();
    this.imServerConfig.setListenPort(this.imProperties.getPort());
    this.imServerConfig.setServerChannelMaxIdleTimeSeconds(60);
  }

  public void startup(long delay) {
    if (running) {
      imServerLog.warn("[NEW-IM] Servers already startup ~, ignore.");
      return;
    }

    imServerLog.info("[NEW-IM] Startup IM Server listen on port :{}", imProperties.getPort());
    startupIMMainServer(delay);

    imServerLog.info(
        "[NEW-IM] Startup IM Inner Cluster Server listen on port :{}",
        imProperties.getClusterPort());
    startupInnerClusterServer(delay);

    imServerLog.info("[NEW-IM] System Startup Finished~");
    running = true;
  }

  public void shutdown(boolean immediately) {
    running = false;

    try {
      imServerLog.info("Ready to shutdown cluster defaultReplica acceptor server.");
      clusterServer.shutdown();
    } catch (Exception ignore) {
    }

    try {
      imServerLog.info("Ready to shutdown im server.");
      imServer.shutdown();
    } catch (Exception ignore) {
    }
  }

  private void startupInnerClusterServer(long delay) {
    clusterServerConfig = new NettyServerConfig();
    clusterServerConfig.setServerChannelMaxIdleTimeSeconds(60);
    clusterServerConfig.setListenPort(imProperties.getClusterPort());
    clusterServer =
        new NettyRemotingSocketServer(
            clusterServerConfig,
            new ChannelEventListener() {
              @Override
              public void onChannelConnect(String remoteAddr, Channel channel) {
                innerReplicaServerLog.info("Cluster Remoting[{}] is connected", remoteAddr);
              }

              @Override
              public void onChannelClose(String remoteAddr, Channel channel) {
                innerReplicaServerLog.info("Cluster Remoting[{}] is closed", remoteAddr);
              }

              @Override
              public void onChannelException(String remoteAddr, Channel channel) {
                innerReplicaServerLog.info(
                    "Cluster Remoting[{}] is exception ,closing ..", remoteAddr);
                try {
                  channel.close();
                } catch (Exception ignore) {
                }
              }

              @Override
              public void onChannelIdle(String remoteAddr, Channel channel) {
                innerReplicaServerLog.info("Cluster Remoting[{}] is idle", remoteAddr);
              }
            });

    clusterServer.registerDefaultProcessor(new DefaultIMProcessor(), defaultExecutor);
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
                  imServerLog.info("Client Remoting[{}] is connected", remoteAddr);
                }

                @Override
                public void onChannelClose(String remoteAddr, Channel channel) {
                  imServerLog.info("Client Remoting[{}] is closed", remoteAddr);
                }

                @Override
                public void onChannelException(String remoteAddr, Channel channel) {
                  imServerLog.info("Client Remoting[{}] is exception ,closing ..", remoteAddr);
                  try {
                    channel.close();
                  } catch (Exception ignore) {
                  }
                }

                @Override
                public void onChannelIdle(String remoteAddr, Channel channel) {
                  imServerLog.info("Client Remoting[{}] is idle", remoteAddr);
                }
              });
    }

    imSession.registerNewIMServer(imServer);

    // bind processors
    // default processor
    imServer.registerDefaultProcessor(new DefaultIMProcessor(), defaultExecutor);

    // register server process cluster register request
    imServer.registerProcessor(
        ClusterClientCommand.CLIENT_AUTH,
        new RemotingClientRegisterAuthProcessor(imSession, remotingAuthService),
        defaultExecutor);

    imServer.registerProcessor(
        ClusterClientCommand.CLIENT_PUSH_MESSAGE,
        new RemotingClientPushMessageProcessor(imSession, messageService),
        null);

    imServer.registerProcessor(
        ClusterClientCommand.CLIENT_PULL_OWNER_GROUPS,
        new RemotingClientPullGroupProcessor(groupService, imSession),
        null);

    imServer.registerProcessor(
        ClusterClientCommand.CLIENT_PULL_MESSAGE,
        new RemotingClientPullMessageProcessor(messageService, imSession),
        null);

    imServer.registerProcessor(
        ClusterClientCommand.CLIENT_JOIN_GROUP,
        new RemotingClientJoinOrLeaveGroupProcessor(groupService, imSession),
        null);

    imServer.registerProcessor(
        ClusterClientCommand.CLIENT_QUIT_GROUP,
        new RemotingClientJoinOrLeaveGroupProcessor(groupService, imSession),
        null);

    // @since 2.2.0
    imServer.registerProcessor(
        ClusterClientCommand.CLIENT_PUSH_MESSAGE_READ_STATUS,
        new RemotingClientPushMessageReadStatusProcessor(messageService, imSession),
        null);

    // @since 2.2.0
    imServer.registerProcessor(
        ClusterClientCommand.CLIENT_PULL_GROUP_MESSAGE_READ_STATUS,
        new RemotingClientPullGroupMessageReadStatusProcessor(
            messageService, groupService, imSession),
        null);

    // @since 2.2.0
    imServer.registerProcessor(
        ClusterClientCommand.CLIENT_PULL_GROUP_MEMBERS_ONLINE_STATUS,
        new RemotingClientPullGroupMembersOnlineStatusProcessor(groupService,imSession),
        null);

    // @since 2.2.0
    imServer.registerProcessor(
        ClusterClientCommand.CLIENT_PULL_GROUP_MEMBERS,
        new RemotingClientPullGroupMembersProcessor(imSession, groupService),
        null);

    // start imServer
    imServer.start();
  }
}
