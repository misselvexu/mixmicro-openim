package com.acmedcare.framework.newim.master.core;

import static com.acmedcare.framework.newim.MasterLogger.masterClusterAcceptorLog;
import static com.acmedcare.framework.newim.MasterLogger.startLog;
import static com.acmedcare.framework.newim.protocol.Command.MasterClusterCommand.CLUSTER_PULL_REPLICAS;

import com.acmedcare.framework.kits.Assert;
import com.acmedcare.framework.newim.BizResult;
import com.acmedcare.framework.newim.InstanceNode;
import com.acmedcare.framework.newim.master.MasterConfig;
import com.acmedcare.framework.newim.master.core.MasterSession.MasterClusterSession;
import com.acmedcare.framework.newim.master.processor.ClusterPushClientChannelsRequestProcessor;
import com.acmedcare.framework.newim.master.processor.DefaultMasterProcessor;
import com.acmedcare.framework.newim.protocol.Command.MasterClusterCommand;
import com.acmedcare.framework.newim.protocol.request.ClusterRegisterHeader;
import com.acmedcare.tiffany.framework.remoting.ChannelEventListener;
import com.acmedcare.tiffany.framework.remoting.netty.NettyRemotingSocketServer;
import com.acmedcare.tiffany.framework.remoting.netty.NettyRequestProcessor;
import com.acmedcare.tiffany.framework.remoting.netty.NettyServerConfig;
import com.acmedcare.tiffany.framework.remoting.protocol.RemotingCommand;
import com.alibaba.fastjson.JSON;
import io.netty.channel.Channel;
import io.netty.channel.ChannelHandlerContext;
import io.netty.util.AttributeKey;
import io.netty.util.concurrent.DefaultThreadFactory;
import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.ThreadPoolExecutor.CallerRunsPolicy;
import java.util.concurrent.TimeUnit;
import lombok.Getter;

/**
 * Server Acceptor
 *
 * @author <a href="mailto:iskp.me@gmail.com">Elve.Xu</a>
 * @version ${project.version} - 07/11/2018.
 */
public class MasterClusterAcceptorServer {

  private static final AttributeKey<InstanceNode> CLUSTER_INSTANCE_NODE_ATTRIBUTE_KEY =
      AttributeKey.newInstance("CLUSTER_INSTANCE_NODE_ATTRIBUTE_KEY");

  private final MasterConfig masterConfig;
  private MasterSession masterSession = new MasterSession();
  @Getter private MasterClusterSession masterClusterSession = new MasterClusterSession();
  /** 集群MServer配置 */
  private NettyServerConfig masterClusterConfig;
  /** 集群MServer 实例 */
  private NettyRemotingSocketServer masterClusterAcceptorServer;

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

  public MasterClusterAcceptorServer(MasterConfig masterConfig) {
    this.masterConfig = masterConfig;
    this.masterClusterConfig = new NettyServerConfig();
    this.masterClusterConfig.setListenPort(masterConfig.getPort());
    this.masterClusterConfig.setServerChannelMaxIdleTimeSeconds(60); // idle
    masterClusterAcceptorServer =
        new NettyRemotingSocketServer(
            masterClusterConfig,
            new ChannelEventListener() {
              @Override
              public void onChannelConnect(String remoteAddr, Channel channel) {
                masterClusterAcceptorLog.info("cluster Remoting[{}] is connected", remoteAddr);
              }

              @Override
              public void onChannelClose(String remoteAddr, Channel channel) {
                masterClusterAcceptorLog.info("cluster Remoting[{}] is closed", remoteAddr);
                InstanceNode node = channel.attr(CLUSTER_INSTANCE_NODE_ATTRIBUTE_KEY).get();
                // 移除本地副本实例
                masterClusterSession.revokeClusterInstance(node.getHost());
              }

              @Override
              public void onChannelException(String remoteAddr, Channel channel) {
                masterClusterAcceptorLog.info(
                    "cluster Remoting[{}] is exception ,closing ..", remoteAddr);
                try {
                  channel.close();
                } catch (Exception ignore) {
                }
              }

              @Override
              public void onChannelIdle(String remoteAddr, Channel channel) {
                masterClusterAcceptorLog.info("cluster Remoting[{}] is idle", remoteAddr);
              }
            });

    // register default processor
    masterClusterAcceptorServer.registerDefaultProcessor(
        new DefaultMasterProcessor(), defaultExecutor);
    // register biz processor

    // 同步 session 处理器
    masterClusterAcceptorServer.registerProcessor(
        MasterClusterCommand.CLUSTER_PUSH_CLIENT_CHANNELS,
        new ClusterPushClientChannelsRequestProcessor(masterClusterSession),
        null);

    // master 相互注册处理器
    masterClusterAcceptorServer.registerProcessor(
        MasterClusterCommand.CLUSTER_REGISTER,
        new NettyRequestProcessor() {
          @Override
          public RemotingCommand processRequest(
              ChannelHandlerContext channelHandlerContext, RemotingCommand remotingCommand)
              throws Exception {

            masterClusterAcceptorLog.info("cluster request to register...");

            // 注册副本
            RemotingCommand response =
                RemotingCommand.createResponseCommand(remotingCommand.getCode(), null);

            ClusterRegisterHeader header =
                (ClusterRegisterHeader)
                    remotingCommand.decodeCommandCustomHeader(ClusterRegisterHeader.class);
            Assert.notNull(header, "cluster register header must not be null");

            masterClusterAcceptorLog.info("cluster remote address:{}", header.getHost());

            InstanceNode node =
                channelHandlerContext.channel().attr(CLUSTER_INSTANCE_NODE_ATTRIBUTE_KEY).get();
            Assert.isNull(node, "remote channel has been distorted.");

            node = header.instance();

            // register new remote client
            masterClusterSession.registerClusterInstance(
                node.getHost(), channelHandlerContext.channel());

            channelHandlerContext.channel().attr(CLUSTER_INSTANCE_NODE_ATTRIBUTE_KEY).set(node);

            masterClusterAcceptorLog.info(
                "cluster remote:{} instance register succeed", node.getHost());

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
    masterClusterAcceptorServer.registerProcessor(
        MasterClusterCommand.CLUSTER_SHUTDOWN,
        new NettyRequestProcessor() {
          @Override
          public RemotingCommand processRequest(
              ChannelHandlerContext channelHandlerContext, RemotingCommand remotingCommand)
              throws Exception {
            RemotingCommand response =
                RemotingCommand.createResponseCommand(remotingCommand.getCode(), null);

            InstanceNode node =
                channelHandlerContext.channel().attr(CLUSTER_INSTANCE_NODE_ATTRIBUTE_KEY).get();

            masterClusterSession.revokeClusterInstance(node.getHost());

            masterClusterAcceptorLog.info(
                "cluster remote:{} instance revoke succeed", node.getHost());

            response.setBody(BizResult.SUCCESS.bytes());
            return response;
          }

          @Override
          public boolean rejectRequest() {
            return false;
          }
        },
        null);

    masterClusterAcceptorServer.registerProcessor(
        CLUSTER_PULL_REPLICAS,
        new NettyRequestProcessor() {
          @Override
          public RemotingCommand processRequest(
              ChannelHandlerContext channelHandlerContext, RemotingCommand remotingCommand)
              throws Exception {
            RemotingCommand response =
                RemotingCommand.createResponseCommand(remotingCommand.getCode(), null);

            Set<String> servers = masterClusterSession.clusterList();
            response.setBody(JSON.toJSONBytes(servers));

            return response;
          }

          @Override
          public boolean rejectRequest() {
            return false;
          }
        },
        null);

    // 启动
    masterClusterAcceptorServer.start();
    startLog.info(
        "master cluster acceptor server startup , listen on : {}", masterConfig.getPort());

    Runtime.getRuntime()
        .addShutdownHook(
            new Thread(
                () -> {
                  masterClusterAcceptorLog.info("jvm hook, shutdown all connected clients");
                  try {
                    masterClusterSession.shutdownAll();
                  } catch (Exception ignore) {
                  }
                }));
  }
}
