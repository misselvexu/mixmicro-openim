package com.acmedcare.framework.newim.master.replica;

import static com.acmedcare.framework.newim.MasterLogger.masterClusterAcceptorLog;
import static com.acmedcare.framework.newim.MasterLogger.masterReplicaLog;

import com.acmedcare.framework.newim.BizResult;
import com.acmedcare.framework.newim.master.MasterConfig;
import com.acmedcare.framework.newim.master.processor.DefaultMasterProcessor;
import com.acmedcare.framework.newim.master.processor.MasterSyncClusterSessionRequestProcessor;
import com.acmedcare.framework.newim.master.replica.MasterSession.MasterReplicaSession;
import com.acmedcare.framework.newim.master.replica.MasterSession.RemoteReplicaInstance;
import com.acmedcare.framework.newim.protocol.Command.MasterWithMasterCommand;
import com.acmedcare.tiffany.framework.remoting.ChannelEventListener;
import com.acmedcare.tiffany.framework.remoting.common.RemotingUtil;
import com.acmedcare.tiffany.framework.remoting.netty.NettyRemotingSocketServer;
import com.acmedcare.tiffany.framework.remoting.netty.NettyRequestProcessor;
import com.acmedcare.tiffany.framework.remoting.netty.NettyServerConfig;
import com.acmedcare.tiffany.framework.remoting.protocol.RemotingCommand;
import io.netty.channel.Channel;
import io.netty.channel.ChannelHandlerContext;
import io.netty.util.concurrent.DefaultThreadFactory;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.ThreadPoolExecutor.CallerRunsPolicy;
import java.util.concurrent.TimeUnit;
import lombok.Builder;

/**
 * Cluster Connector For {@link
 * com.acmedcare.tiffany.framework.remoting.netty.NettyRemotingSocketClient} and {@link
 * com.acmedcare.tiffany.framework.remoting.netty.NettyRemotingSocketServer}
 *
 * @author <a href="mailto:iskp.me@gmail.com">Elve.Xu</a>
 * @version ${project.version} - 07/11/2018.
 */
public class MasterConnector {

  /**
   * Master Server Acceptor For Cluster
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
                  masterClusterAcceptorLog.debug("Cluster Remoting[{}] is connected", remoteAddr);
                }

                @Override
                public void onChannelClose(String remoteAddr, Channel channel) {
                  masterClusterAcceptorLog.debug("Cluster Remoting[{}] is closed", remoteAddr);
                }

                @Override
                public void onChannelException(String remoteAddr, Channel channel) {
                  masterClusterAcceptorLog.debug(
                      "Cluster Remoting[{}] is exception ,closing ..", remoteAddr);
                  try {
                    channel.close();
                  } catch (Exception ignore) {
                  }
                }

                @Override
                public void onChannelIdle(String remoteAddr, Channel channel) {
                  masterClusterAcceptorLog.debug("Cluster Remoting[{}] is idle", remoteAddr);
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
  public static class MasterReplicaClient {}
}
