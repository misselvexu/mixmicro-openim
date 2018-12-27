package com.acmedcare.framework.newim.server.replica;

import com.acmedcare.framework.kits.thread.DefaultThreadFactory;
import com.acmedcare.framework.kits.thread.ThreadKit;
import com.acmedcare.framework.newim.BizResult;
import com.acmedcare.framework.newim.BizResult.ExceptionWrapper;
import com.acmedcare.framework.newim.InstanceType;
import com.acmedcare.framework.newim.protocol.Command.ClusterWithClusterCommand;
import com.acmedcare.framework.newim.server.replica.NodeReplicaProperties.ReplicaProperties;
import com.acmedcare.framework.newim.spi.util.Assert;
import com.acmedcare.tiffany.framework.remoting.ChannelEventListener;
import com.acmedcare.tiffany.framework.remoting.RemotingSocketServer;
import com.acmedcare.tiffany.framework.remoting.netty.NettyRemotingSocketServer;
import com.acmedcare.tiffany.framework.remoting.netty.NettyRequestProcessor;
import com.acmedcare.tiffany.framework.remoting.netty.NettyServerConfig;
import com.acmedcare.tiffany.framework.remoting.protocol.RemotingCommand;
import com.google.common.collect.Maps;
import io.netty.channel.Channel;
import io.netty.channel.ChannelHandlerContext;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.ThreadPoolExecutor.CallerRunsPolicy;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import lombok.Builder;
import lombok.Getter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.beans.factory.BeanFactoryAware;
import org.springframework.beans.factory.InitializingBean;

/**
 * NodeReplicaConnectorFactory
 *
 * @author <a href="mailto:iskp.me@gmail.com">Elve.Xu</a>
 * @version ${project.version} - 2018-12-26.
 */
public class NodeReplicaConnectorFactory implements BeanFactoryAware, InitializingBean {

  private static final Logger logger = LoggerFactory.getLogger(NodeReplicaConnectorFactory.class);
  /**
   * {@link NodeReplicaExecutor} Instances Cache
   *
   * @see InstanceType
   */
  private static Map<InstanceType, NodeReplicaExecutor> nodeReplicaConnectors =
      Maps.newConcurrentMap();

  private NodeReplicaProperties nodeReplicaProperties;

  /** Bean Factory Instance */
  private BeanFactory beanFactory;

  @Override
  public void setBeanFactory(BeanFactory beanFactory) throws BeansException {
    this.beanFactory = beanFactory;
  }

  @Override
  public void afterPropertiesSet() throws Exception {
    this.nodeReplicaProperties = beanFactory.getBean(NodeReplicaProperties.class);
    logger.info(
        "[REPLICA-FACTORY] found nodeReplicaProperties instance:{} from bean factory",
        this.nodeReplicaProperties);

    Assert.notNull(this.nodeReplicaProperties);

    // init
    this.nodeReplicaProperties
        .getReplicas()
        .forEach(
            (type, replicaProperties) -> {
              NodeReplicaService nodeReplicaService;
              try {
                String replicaServiceClass = replicaProperties.getReplicaServiceClass();
                Class<?> aClass = Class.forName(replicaServiceClass);
                nodeReplicaService = (NodeReplicaService) beanFactory.getBean(aClass);

                Assert.notNull(
                    nodeReplicaService,
                    "[REPLICA-FACTORY] Class: " + replicaServiceClass + " must be inited.");

                if (!type.equals(nodeReplicaService.type())) {
                  logger.warn(
                      "[REPLICA-FACTORY] Class:{} type is :{} , is not same with config file defined value:{}",
                      replicaServiceClass,
                      nodeReplicaService.type(),
                      type);
                  return;
                }
              } catch (Exception e) {
                logger.error(
                    "[REPLICA-FACTORY] NodeReplicaService class instance is not invalid.", e);
                return;
              }

              NodeReplicaExecutor nodeReplicaExecutor =
                  NodeReplicaExecutor.builder()
                      .replicaProperties(replicaProperties)
                      .nodeReplicaService(nodeReplicaService)
                      .build();

              nodeReplicaExecutor.startup();

              // save cache
              nodeReplicaConnectors.put(type, nodeReplicaExecutor);
            });

    // shutdown hook
    Runtime.getRuntime()
        .addShutdownHook(
            new Thread(
                () ->
                    nodeReplicaConnectors.forEach(
                        (instanceType, nodeReplicaExecutor) -> {
                          logger.info(
                              "[REPLICA-FACTORY] ready to shutdown {} connector.", instanceType);
                          nodeReplicaExecutor.shutdown();
                        })));
  }

  /** {@link NodeReplicaExecutor }Bean Defined */
  public static class NodeReplicaExecutor {

    private final ReplicaProperties replicaProperties;
    private final NodeReplicaService nodeReplicaService;
    private ReplicaServer server;
    private AtomicBoolean startup = new AtomicBoolean(false);

    @Builder
    public NodeReplicaExecutor(
        ReplicaProperties replicaProperties, NodeReplicaService nodeReplicaService) {
      this.replicaProperties = replicaProperties;
      this.nodeReplicaService = nodeReplicaService;
    }

    void startup() {
      logger.info("[REPLICA-SERVER-ACCEPTOR] ready to startup replica acceptor server ...");
      Thread startupThread =
          new Thread(
              () -> {
                // delay
                ThreadKit.sleep(replicaProperties.getStartupDelay());
                // startup
                startupReplicaAcceptor();
              });
      startupThread.setName("REPLICA-SERVER-ACCEPTOR-STARTUP-THREAD");
      startupThread.start();

      // startup connector
      logger.info("[REPLICA-SERVER-CONNECTORS] ready to startup replica connectors client ...");
      new Connectors(
              nodeReplicaService,
              this.replicaProperties.getStartupDelay(),
              this.replicaProperties.getInstancesRefreshPeriod())
          .startup();
    }

    /**
     * Startup Replica Acceptor
     *
     * @see ReplicaProperties#getHost()
     * @see ReplicaProperties#getPort()
     */
    private void startupReplicaAcceptor() {
      if (startup.compareAndSet(false, true)) {
        server =
            ReplicaServer.newServer(
                replicaProperties.getPort(),
                new ChannelEventListener() {
                  @Override
                  public void onChannelConnect(String remotingAddress, Channel channel) {}

                  @Override
                  public void onChannelClose(String remotingAddress, Channel channel) {}

                  @Override
                  public void onChannelException(String remotingAddress, Channel channel) {}

                  @Override
                  public void onChannelIdle(String remotingAddress, Channel channel) {}
                });
      }

      // startup
      server.getServer().start();
      logger.info(
          "[REPLICA-SERVER-ACCEPTOR] replica acceptor server started , listener on port : ",
          replicaProperties.getPort());
    }

    void shutdown() {
      if (startup.compareAndSet(true, false)) {
        if (server != null) {
          server.getServer().shutdown();
        }
      }
    }
  }

  private static class Connectors {
    private final NodeReplicaService nodeReplicaService;
    private final ScheduledExecutorService refreshService;
    private final long startupDelay;
    private final long refreshPeriod;

    private Connectors(
        NodeReplicaService nodeReplicaService, long startupDelay, long refreshPeriod) {
      this.nodeReplicaService = nodeReplicaService;
      this.startupDelay = startupDelay;
      this.refreshPeriod = refreshPeriod;
      // startup schedule thread
      refreshService =
          new ScheduledThreadPoolExecutor(
              1, new DefaultThreadFactory("REPLICAS-REFRESH-SERVICES-THREAD"));
    }

    void startup() {
      refreshService.scheduleWithFixedDelay(
          () -> {
            // TODO
          },
          startupDelay,
          refreshPeriod,
          TimeUnit.MILLISECONDS);
    }
  }

  /** Replica Server Instance */
  private static class ReplicaServer {
    private static final int CORE_POOL_SIZE = Runtime.getRuntime().availableProcessors() << 1;
    private static final int MAXIMUM_POOL_SIZE = Runtime.getRuntime().availableProcessors() << 2;
    private static ExecutorService defaultReplicaProcessorExecutor =
        new ThreadPoolExecutor(
            CORE_POOL_SIZE,
            MAXIMUM_POOL_SIZE,
            5000,
            TimeUnit.MILLISECONDS,
            new LinkedBlockingQueue<>(64),
            new DefaultThreadFactory("REPLICA-ACCEPTOR"),
            new CallerRunsPolicy());
    private static Map<String, Channel> replicaRemotingChannels = Maps.newConcurrentMap();
    @Getter private NettyServerConfig nettyServerConfig;
    @Getter private RemotingSocketServer server;

    private ReplicaServer() {}

    static ReplicaServer newServer(int port, ChannelEventListener listener) {
      ReplicaServer replicaServer = new ReplicaServer();
      replicaServer.nettyServerConfig = new NettyServerConfig();
      replicaServer.nettyServerConfig.setListenPort(port);
      replicaServer.server =
          new NettyRemotingSocketServer(replicaServer.nettyServerConfig, listener);
      replicaServer.server.registerDefaultProcessor(
          new ReplicaServerProcessor(replicaRemotingChannels), defaultReplicaProcessorExecutor);
      return replicaServer;
    }

    private static class ReplicaServerProcessor implements NettyRequestProcessor {

      private final Map<String, Channel> replicaRemotingChannels;

      ReplicaServerProcessor(Map<String, Channel> replicaRemotingChannels) {
        this.replicaRemotingChannels = replicaRemotingChannels;
      }

      @Override
      public RemotingCommand processRequest(
          ChannelHandlerContext channelHandlerContext, RemotingCommand remotingCommand)
          throws Exception {

        RemotingCommand defaultResponse =
            RemotingCommand.createResponseCommand(remotingCommand.getCode(), "DEFAULT-RESPONSE");

        try {
          int code = remotingCommand.getCode();

          switch (code) {
            case ClusterWithClusterCommand.CLUSTER_REGISTER:
              break;

            case ClusterWithClusterCommand.CLUSTER_SHUTDOWN:
              break;

            case ClusterWithClusterCommand.CLUSTER_FORWARD_MESSAGE:
              break;

            default:
              defaultResponse.setBody(
                  BizResult.builder()
                      .code(-1)
                      .exception(
                          ExceptionWrapper.builder()
                              .message("Unknown biz code : 0x" + Integer.toHexString(code))
                              .build())
                      .build()
                      .bytes());
          }

        } catch (Exception e) {
          logger.error("[REPLICA-SERVER-ACCEPTOR] processor execute failed with exception", e);
          defaultResponse.setBody(
              BizResult.builder()
                  .code(-1)
                  .exception(ExceptionWrapper.builder().message(e.getMessage()).build())
                  .build()
                  .bytes());
        }
        return defaultResponse;
      }

      @Override
      public boolean rejectRequest() {
        return false;
      }
    }
  }
}
