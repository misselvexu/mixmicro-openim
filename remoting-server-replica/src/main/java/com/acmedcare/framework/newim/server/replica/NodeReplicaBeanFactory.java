package com.acmedcare.framework.newim.server.replica;

import com.acmedcare.framework.kits.Assert;
import com.acmedcare.framework.kits.StringUtils;
import com.acmedcare.framework.kits.event.Event;
import com.acmedcare.framework.kits.executor.AsyncRuntimeExecutor;
import com.acmedcare.framework.kits.thread.DefaultThreadFactory;
import com.acmedcare.framework.kits.thread.ThreadKit;
import com.acmedcare.framework.newim.BizResult;
import com.acmedcare.framework.newim.BizResult.ExceptionWrapper;
import com.acmedcare.framework.newim.InstanceType;
import com.acmedcare.framework.newim.Message;
import com.acmedcare.framework.newim.Message.GroupMessage;
import com.acmedcare.framework.newim.Message.MQMessage;
import com.acmedcare.framework.newim.Message.MessageType;
import com.acmedcare.framework.newim.Message.SingleMessage;
import com.acmedcare.framework.newim.RemotingEvent;
import com.acmedcare.framework.newim.client.MessageAttribute;
import com.acmedcare.framework.newim.protocol.Command.ClusterWithClusterCommand;
import com.acmedcare.framework.newim.protocol.request.ClusterForwardEventHeader;
import com.acmedcare.framework.newim.protocol.request.ClusterForwardMessageHeader;
import com.acmedcare.framework.newim.server.replica.NodeReplicaProperties.ReplicaProperties;
import com.acmedcare.tiffany.framework.remoting.ChannelEventListener;
import com.acmedcare.tiffany.framework.remoting.RemotingSocketServer;
import com.acmedcare.tiffany.framework.remoting.exception.RemotingConnectException;
import com.acmedcare.tiffany.framework.remoting.exception.RemotingSendRequestException;
import com.acmedcare.tiffany.framework.remoting.exception.RemotingTimeoutException;
import com.acmedcare.tiffany.framework.remoting.exception.RemotingTooMuchRequestException;
import com.acmedcare.tiffany.framework.remoting.netty.NettyClientConfig;
import com.acmedcare.tiffany.framework.remoting.netty.NettyRemotingSocketClient;
import com.acmedcare.tiffany.framework.remoting.netty.NettyRemotingSocketServer;
import com.acmedcare.tiffany.framework.remoting.netty.NettyRequestProcessor;
import com.acmedcare.tiffany.framework.remoting.netty.NettyServerConfig;
import com.acmedcare.tiffany.framework.remoting.protocol.RemotingCommand;
import com.acmedcare.tiffany.framework.remoting.protocol.RemotingSysRequestCode;
import com.alibaba.fastjson.JSON;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import io.netty.channel.Channel;
import io.netty.channel.ChannelHandlerContext;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.ThreadPoolExecutor.CallerRunsPolicy;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.beans.factory.BeanFactoryAware;
import org.springframework.beans.factory.InitializingBean;

/**
 * NodeReplicaBeanFactory
 *
 * @author <a href="mailto:iskp.me@gmail.com">Elve.Xu</a>
 * @version ${project.version} - 2018-12-26.
 */
public class NodeReplicaBeanFactory implements BeanFactoryAware, InitializingBean {

  private static final Logger logger = LoggerFactory.getLogger(NodeReplicaBeanFactory.class);
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

  public void postEvent(InstanceType instanceType, RemotingEvent remotingEvent) {
    if (!nodeReplicaConnectors.containsKey(instanceType)) {
      throw new NodeReplicaException("Not " + instanceType + " defined in config file;");
    } else {
      NodeReplicaExecutor executor = nodeReplicaConnectors.get(instanceType);
      executor
          .connectors
          .getConnections()
          .forEach(
              (address, connectorClient) ->
                  // async
                  AsyncRuntimeExecutor.getAsyncThreadPool()
                      .execute(
                          () -> {
                            if (connectorClient != null && connectorClient.isConnected()) {

                              ClusterForwardEventHeader header = new ClusterForwardEventHeader();

                              header.setEventName(remotingEvent.getEvent());

                              RemotingCommand command =
                                  RemotingCommand.createRequestCommand(
                                      ClusterWithClusterCommand.CLUSTER_FORWARD_EVENT, header);

                              command.setBody(remotingEvent.getPayload());

                              try {
                                connectorClient.client.invokeAsync(
                                    address,
                                    command,
                                    5000,
                                    responseFuture -> {
                                      if (responseFuture.isSendRequestOK()) {
                                        logger.info(
                                            "[REPLICA-POST-EVENT] forward event request is send ok.");
                                        RemotingCommand response =
                                            responseFuture.getResponseCommand();
                                        if (response != null && response.getBody() != null) {
                                          BizResult bizResult =
                                              JSON.parseObject(response.getBody(), BizResult.class);
                                          if (bizResult != null && bizResult.getCode() == 0) {
                                            logger.info(
                                                "[REPLICA-POST-EVENT] forward event is processed.");
                                          } else {
                                            logger.info(
                                                "[REPLICA-POST-EVENT] forward event is processed.");
                                          }
                                        }
                                      }
                                    });

                              } catch (Exception e) {
                                e.printStackTrace();
                              }
                            }
                          }));
    }
  }

  /**
   * Post Message to Replica
   *
   * @param instanceType {@link InstanceType}
   * @param message message
   * @see InstanceType
   * @see Message
   */
  public void postMessage(InstanceType instanceType, Message message, MessageAttribute attribute) {
    if (!nodeReplicaConnectors.containsKey(instanceType)) {
      throw new NodeReplicaException("Not " + instanceType + " defined in config file;");
    } else {
      if (attribute == null) {
        attribute = MessageAttribute.builder().build();
      }

      NodeReplicaExecutor executor = nodeReplicaConnectors.get(instanceType);
      final MessageAttribute finalAttribute = attribute;
      executor
          .connectors
          .getConnections()
          .forEach(
              (address, connectorClient) ->
                  // async
                  AsyncRuntimeExecutor.getAsyncThreadPool()
                      .execute(
                          () -> {
                            if (connectorClient != null && connectorClient.isConnected()) {

                              ClusterForwardMessageHeader header =
                                  new ClusterForwardMessageHeader();
                              header.setMessageType(message.getMessageType().name());
                              header.setInnerType(message.getInnerType().name());
                              header.setMaxRetryTimes(finalAttribute.getMaxRetryTimes());
                              header.setPersistent(finalAttribute.isPersistent());
                              header.setQos(finalAttribute.isQos());
                              header.setRetryPeriod(finalAttribute.getRetryPeriod());
                              header.setNamespace(finalAttribute.getNamespace());

                              RemotingCommand command =
                                  RemotingCommand.createRequestCommand(
                                      ClusterWithClusterCommand.CLUSTER_FORWARD_MESSAGE, header);
                              command.setBody(message.bytes());

                              try {
                                connectorClient.client.invokeAsync(
                                    address,
                                    command,
                                    5000,
                                    responseFuture -> {
                                      if (responseFuture.isSendRequestOK()) {
                                        logger.info(
                                            "[REPLICA-POST-MESSAGE] forward message request is send ok.");
                                        RemotingCommand response =
                                            responseFuture.getResponseCommand();
                                        if (response != null && response.getBody() != null) {
                                          BizResult bizResult =
                                              JSON.parseObject(response.getBody(), BizResult.class);
                                          if (bizResult != null && bizResult.getCode() == 0) {
                                            logger.info(
                                                "[REPLICA-POST-MESSAGE] forward message is processed.");
                                          } else {
                                            logger.info(
                                                "[REPLICA-POST-MESSAGE] forward message is processed.");
                                          }
                                        }
                                      }
                                    });

                              } catch (Exception e) {
                                e.printStackTrace();
                              }
                            }
                          }));
    }
  }

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

              logger.info("[REPLICA-EXECUTOR] startup replica executor :{} ", nodeReplicaExecutor);

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
    private Connectors connectors;
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
                logger.info(
                    "[REPLICA-SERVER-ACCEPTOR-STARTUP-THREAD] delay starting {} ms",
                    replicaProperties.getStartupDelay());
                // delay
                ThreadKit.sleep(replicaProperties.getStartupDelay());
                // startup
                startupReplicaAcceptor();
              });
      startupThread.setName("REPLICA-SERVER-ACCEPTOR-STARTUP-THREAD");
      startupThread.start();

      // startup connector
      logger.info("[REPLICA-SERVER-CONNECTORS] ready to startup replica connectors client ...");
      connectors = new Connectors(nodeReplicaService, replicaProperties).startup();
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
                  public void onChannelConnect(String remotingAddress, Channel channel) {
                    logger.info(
                        "[REPLICA-ACCEPTOR] {} - {} is connected.", remotingAddress, channel);
                  }

                  @Override
                  public void onChannelClose(String remotingAddress, Channel channel) {
                    logger.info("[REPLICA-ACCEPTOR] {} - {} is closed.", remotingAddress, channel);
                  }

                  @Override
                  public void onChannelException(String remotingAddress, Channel channel) {
                    logger.info(
                        "[REPLICA-ACCEPTOR] {} - {} is exception.", remotingAddress, channel);
                  }

                  @Override
                  public void onChannelIdle(String remotingAddress, Channel channel) {
                    logger.info("[REPLICA-ACCEPTOR] {} - {} is idle.", remotingAddress, channel);
                  }
                },
                nodeReplicaService);
      }

      // startup
      server.getServer().start();
      logger.info(
          "[REPLICA-SERVER-ACCEPTOR] replica acceptor server started , listener on port : {}",
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

  /** Replica Client(s) */
  private static class Connectors {
    private static NodeReplicaService nodeReplicaService;
    private final ScheduledExecutorService refreshService;
    private final ReplicaProperties replicaProperties;
    @Getter private final Map<String, ConnectorClient> connections = Maps.newConcurrentMap();

    private Connectors(NodeReplicaService nodeReplicaService, ReplicaProperties replicaProperties) {
      Connectors.nodeReplicaService = nodeReplicaService;
      this.replicaProperties = replicaProperties;
      // startup schedule thread
      refreshService =
          new ScheduledThreadPoolExecutor(
              1,
              new DefaultThreadFactory(
                  "REPLICAS-REFRESH-SERVICES-THREAD-" + nodeReplicaService.type().name()));
    }

    Connectors startup() {
      logger.info("[REPLICA-CONNECTORS] startup replica connectors refresh execute service ...");
      refreshService.scheduleWithFixedDelay(
          () -> {
            List<NodeReplicaInstance> instances = nodeReplicaService.loadNodeInstances();
            logger.debug(
                "[REPLICA-CLIENT-TIMER] 获取到的Replica节点数据:{} ", JSON.toJSONString(instances));
            if (instances != null && !instances.isEmpty()) {
              for (NodeReplicaInstance instance : instances) {
                String nodeAddress = instance.getNodeAddress();
                if (StringUtils.equals(nodeAddress, replicaProperties.selfAddress())) {
                  continue;
                }

                if (connections.containsKey(nodeAddress)) { // already exist
                  // ignore
                  logger.info("[REPLICA-CLIENT-TIMER] 节点已存在,忽略~");
                } else {
                  ConnectorClient connectorClient =
                      ConnectorClient.builder()
                          .remotingAddress(nodeAddress)
                          .replicaProperties(replicaProperties)
                          .build();
                  logger.info("[REPLICA-CLIENT-TIMER] 初始化Replica客户端:{} ", connectorClient);
                  connections.put(nodeAddress, connectorClient);
                  logger.info("[REPLICA-CLIENT-TIMER] 启动客户端开始连接...");
                  connectorClient.startup();
                }
              }
            }
          },
          replicaProperties.getStartupDelay(),
          replicaProperties.getInstancesRefreshPeriod(),
          TimeUnit.MILLISECONDS);

      return this;
    }

    @Getter
    @NoArgsConstructor
    private static class ConnectorClient {

      private static final int CONNECT_RETRY_PERIOD = 5;
      private static AtomicInteger retryTimes = new AtomicInteger(1);
      private ReplicaProperties replicaProperties;
      private String remotingAddress;
      private NettyRemotingSocketClient client;
      private NettyClientConfig config;
      private AtomicBoolean startup = new AtomicBoolean(false);
      private volatile boolean connected = false;
      private ScheduledExecutorService heartbeatExecutor;
      private ScheduledExecutorService connectExecutor;

      @Builder
      public ConnectorClient(ReplicaProperties replicaProperties, String remotingAddress) {
        this.replicaProperties = replicaProperties;
        this.remotingAddress = remotingAddress;
      }

      @Override
      public String toString() {
        return "REPLICA-CLIENT:[" + remotingAddress + "]";
      }

      void startup() {
        if (startup.compareAndSet(false, true)) {
          if (client == null) {
            config = new NettyClientConfig();
            config.setUseTLS(false);
            config.setEnableHeartbeat(!replicaProperties.isConnectorHeartbeatEnabled());
            client = new NettyRemotingSocketClient(config);
            client.updateNameServerAddressList(Lists.newArrayList(remotingAddress));
          }

          logger.info("[REPLICA-CLIENT] 客户端:{} ", client);

          // startup
          client.start();
          logger.info("[REPLICA-CLIENT] 客户端已启动.");

          if (connectExecutor == null) {
            connectExecutor =
                new ScheduledThreadPoolExecutor(1, new DefaultThreadFactory("CONNECT-THREAD-"));
          }

          logger.info("[REPLICA-CLIENT] 启动远程连接线程池.");
          connectExecutor.scheduleWithFixedDelay(
              () -> {
                if (!connected) {
                  // send handshake
                  try {
                    handshake();
                  } catch (Exception e) {
                    int times = retryTimes.incrementAndGet();
                    logger.warn(
                        "[REPLICA-CONNECT-THREAD] handshake request failed , will try {} s later.",
                        times * CONNECT_RETRY_PERIOD);
                    ThreadKit.sleep(CONNECT_RETRY_PERIOD * times, TimeUnit.SECONDS);
                  }
                }
              },
              1,
              10,
              TimeUnit.SECONDS);

          // startup keepalive
          if (heartbeatExecutor == null) {
            heartbeatExecutor =
                new ScheduledThreadPoolExecutor(1, new DefaultThreadFactory("HEARTBEAT-THREAD-"));
          }
          heartbeatExecutor.scheduleWithFixedDelay(
              () -> {
                try {
                  if (isConnected()) {
                    heartbeat();
                  }
                } catch (Exception e) {
                  logger.warn("[REPLICA-CONNECTOR] heartbeat send failed, will retry next time.");
                }
              },
              replicaProperties.getStartupDelay(),
              replicaProperties.getConnectorHeartbeatPeriod(),
              TimeUnit.MILLISECONDS);
        }
      }

      void handshake() throws Exception {
        RemotingCommand handshakeRequest =
            RemotingCommand.createRequestCommand(ClusterWithClusterCommand.CLUSTER_HANDSHAKE, null);
        logger.info("[REPLICA-CLIENT] 发送握手请求:{} ", handshakeRequest);
        client.invokeOneway(
            remotingAddress,
            handshakeRequest,
            replicaProperties.getRequestTimeout(),
            sendOk -> {
              if (sendOk) {
                connected = true;
                logger.info("[REPLICA-CONNECTOR] replica connect is connected.");
                retryTimes.set(1);
              } else {
                logger.warn("[REPLICA-CLIENT] 握手请求发送失败,等待下次重试");
              }
            });
      }

      void heartbeat() {

        try {
          RemotingCommand heartbeat =
              RemotingCommand.createRequestCommand(RemotingSysRequestCode.HEARTBEAT, null);
          client.invokeOneway(
              remotingAddress,
              heartbeat,
              2000,
              isSendOk -> {
                if (!isSendOk) {
                  logger.warn(
                      "[REPLICA-CONNECTOR] remoting :{} heartbeat send failed ", remotingAddress);
                }
              });
        } catch (RemotingTooMuchRequestException | InterruptedException e) {
          logger.warn(
              "[REPLICA-CONNECTOR] remoting :{} heartbeat send failed with exception ,try next round. ",
              remotingAddress,
              e);
        } catch (RemotingSendRequestException
            | RemotingConnectException
            | RemotingTimeoutException e) {
          connected = false;
        }
      }

      void shutdown() {
        try {
          if (heartbeatExecutor != null) {
            ThreadKit.gracefulShutdown(heartbeatExecutor, 5, 5, TimeUnit.SECONDS);
          }
          if (connectExecutor != null) {
            ThreadKit.gracefulShutdown(connectExecutor, 5, 5, TimeUnit.SECONDS);
          }

          if (client != null) {
            client.shutdown();
          }

          startup.set(false);

        } catch (Exception ignore) {
        }
      }
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
    private static NodeReplicaService nodeReplicaService;
    @Getter private NettyServerConfig nettyServerConfig;
    @Getter private RemotingSocketServer server;

    static ReplicaServer newServer(
        int port, ChannelEventListener listener, NodeReplicaService nodeReplicaService) {
      ReplicaServer replicaServer = new ReplicaServer();
      replicaServer.nettyServerConfig = new NettyServerConfig();
      replicaServer.nettyServerConfig.setListenPort(port);
      ReplicaServer.nodeReplicaService = nodeReplicaService;
      replicaServer.server =
          new NettyRemotingSocketServer(replicaServer.nettyServerConfig, listener);
      replicaServer.server.registerDefaultProcessor(
          new ReplicaServerProcessor(), defaultReplicaProcessorExecutor);
      return replicaServer;
    }

    static void onMessage(Message message) {
      // async
      AsyncRuntimeExecutor.getAsyncThreadPool()
          .execute(() -> nodeReplicaService.onReceivedMessage(message));
    }

    static void onEvent(RemotingEvent remotingEvent) {
      AsyncRuntimeExecutor.getAsyncThreadPool()
          .execute(() -> nodeReplicaService.onReceivedEvent(remotingEvent));
    }

    private static class ReplicaServerProcessor implements NettyRequestProcessor {

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
              try {
                ClusterForwardMessageHeader header =
                    (ClusterForwardMessageHeader)
                        remotingCommand.decodeCommandCustomHeader(
                            ClusterForwardMessageHeader.class);

                com.acmedcare.framework.kits.Assert.notNull(
                    header, "replica forward message header must not be null.");

                MessageType messageType = header.decodeType();
                byte[] messageByte = remotingCommand.getBody();
                if (messageByte != null && messageByte.length > 0) {
                  switch (messageType) {
                    case MQ:
                      MQMessage mqMessage = JSON.parseObject(messageByte, MQMessage.class);
                      onMessage(mqMessage);
                      break;
                    case GROUP:
                      GroupMessage groupMessage = JSON.parseObject(messageByte, GroupMessage.class);
                      onMessage(groupMessage);
                      break;
                    case SINGLE:
                      SingleMessage singleMessage =
                          JSON.parseObject(messageByte, SingleMessage.class);
                      onMessage(singleMessage);
                      break;

                    default:
                      defaultResponse.setBody(
                          BizResult.builder()
                              .code(-1)
                              .exception(
                                  ExceptionWrapper.builder()
                                      .message("unsupported message type: " + messageType)
                                      .build())
                              .build()
                              .bytes());
                  }

                  // succeed
                  defaultResponse.setBody(BizResult.SUCCESS.bytes());
                } else {
                  defaultResponse.setBody(
                      BizResult.builder()
                          .code(-1)
                          .exception(
                              ExceptionWrapper.builder()
                                  .message("message body must not be null.")
                                  .build())
                          .build()
                          .bytes());
                }

              } catch (Exception e) {
                logger.error("[REPLICA-CONNECTORS-PROCESSOR] process forward message failed.", e);
                defaultResponse.setBody(
                    BizResult.builder()
                        .code(-1)
                        .exception(ExceptionWrapper.builder().message(e.getMessage()).build())
                        .build()
                        .bytes());
              }
              break;

            case ClusterWithClusterCommand.CLUSTER_FORWARD_EVENT:
              try {
                ClusterForwardEventHeader header =
                    (ClusterForwardEventHeader)
                        remotingCommand.decodeCommandCustomHeader(ClusterForwardEventHeader.class);

                com.acmedcare.framework.kits.Assert.notNull(
                    header, "replica forward event header must not be null.");

                onEvent(
                    RemotingEvent.builder()
                        .event(header.getEventName())
                        .payload(remotingCommand.getBody())
                        .build());

              } catch (Exception e) {
                logger.error("[REPLICA-CONNECTORS-PROCESSOR] process forward event failed.", e);
                defaultResponse.setBody(
                    BizResult.builder()
                        .code(-1)
                        .exception(ExceptionWrapper.builder().message(e.getMessage()).build())
                        .build()
                        .bytes());
              }
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
