package com.acmedcare.framework.newim.server.master.connector;

import com.acmedcare.framework.kits.event.Event;
import com.acmedcare.framework.kits.event.EventBus;
import com.acmedcare.framework.kits.executor.AsyncRuntimeExecutor;
import com.acmedcare.framework.kits.executor.RetriableThreadExecutor;
import com.acmedcare.framework.kits.executor.RetriableThreadExecutor.ExecutorCallback;
import com.acmedcare.framework.kits.executor.RetriableThreadExecutor.RetriableAttribute;
import com.acmedcare.framework.kits.thread.ThreadKit;
import com.acmedcare.framework.newim.BizResult;
import com.acmedcare.framework.newim.Message;
import com.acmedcare.framework.newim.protocol.Command.MasterClusterCommand;
import com.acmedcare.framework.newim.protocol.request.ClusterForwardMessageHeader;
import com.acmedcare.framework.newim.protocol.request.ClusterPushSessionDataBody;
import com.acmedcare.framework.newim.protocol.request.ClusterPushSessionDataHeader;
import com.acmedcare.framework.newim.server.master.connector.event.PullClusterEvent;
import com.acmedcare.framework.newim.server.master.connector.processors.MasterNoticeClientChannelsRequestProcessor;
import com.acmedcare.framework.newim.server.master.connector.processors.MasterPushMessageRequestProcessor;
import com.acmedcare.tiffany.framework.remoting.ChannelEventListener;
import com.acmedcare.tiffany.framework.remoting.netty.NettyClientConfig;
import com.acmedcare.tiffany.framework.remoting.netty.NettyRemotingSocketClient;
import com.acmedcare.tiffany.framework.remoting.protocol.RemotingCommand;
import com.alibaba.fastjson.JSON;
import com.google.common.collect.Lists;
import io.netty.util.concurrent.DefaultThreadFactory;
import io.netty.util.internal.StringUtil;
import java.util.List;
import java.util.Random;
import java.util.Set;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * MasterConnector
 *
 * @author <a href="mailto:iskp.me@gmail.com">Elve.Xu</a>
 * @version ${project.version} - 2018-12-17.
 */
public final class MasterConnector {

  private static final Logger logger = LoggerFactory.getLogger(MasterConnector.class);
  private static volatile AtomicBoolean startup = new AtomicBoolean(false);
  private MasterConnectorProperties masterConnectorProperties;
  private MasterConnectorContext masterConnectorContext;
  private MasterConnectorSubscriber subscriber;
  private List<MasterInstance> masterInstances = Lists.newArrayList();

  private ScheduledExecutorService rollingPullClusterListExecutor;
  private ScheduledExecutorService syncChannelsExecutor;

  MasterConnector(MasterConnectorProperties properties) {
    logger.info("master connector properties : {}", properties);
    this.masterConnectorProperties = properties;
    logger.info("master connector is created by framework.");
    this.masterConnectorContext = new MasterConnectorContext();
  }

  /** Init Method */
  private void init() {

    // init event bus
    if (EventBus.isEnable()) {
      this.subscriber = new MasterConnectorSubscriber(this.masterConnectorContext, false);
      EventBus.register(PullClusterEvent.class, subscriber);
      logger.info("register master connector subscriber : {}", subscriber);
    }

    // create master instance(s)
    List<String> nodes = this.masterConnectorProperties.getNodes();
    if (nodes != null && !nodes.isEmpty()) {
      for (String node : nodes) {
        masterInstances.add(newMasterInstance(node));
      }
    } else {
      logger.warn("not config master server node(s) address.");
    }

    logger.info("master connector is inited.");
  }

  private MasterInstance newMasterInstance(String nodeAddress) {
    MasterInstance masterInstance = MasterInstance.newInstance(nodeAddress);

    NettyClientConfig config = new NettyClientConfig();
    config.setUseTLS(this.masterConnectorProperties.isConnectorEnableTls());
    config.setClientChannelMaxIdleTimeSeconds(
        (int) this.masterConnectorProperties.getConnectorIdleTime());
    config.setEnableHeartbeat(!this.masterConnectorProperties.isHeartbeatEnabled());

    ChannelEventListener listener = null;

    if (!StringUtil.isNullOrEmpty(
        this.masterConnectorProperties.getConnectorChannelEventListener())) {

      try {
        listener =
            (ChannelEventListener)
                Class.forName(this.masterConnectorProperties.getConnectorChannelEventListener())
                    .getConstructor() // must have default constructor
                    .newInstance();
      } catch (Exception e) {
        logger.warn("master connector channel event listener init failed ,ignore.", e);
      }
    }

    // build client
    NettyRemotingSocketClient client = new NettyRemotingSocketClient(config, listener);

    // processor
    client.registerProcessor(
        MasterClusterCommand.MASTER_NOTICE_CLIENT_CHANNELS,
        new MasterNoticeClientChannelsRequestProcessor(masterConnectorContext),
        null);

    client.registerProcessor(
        MasterClusterCommand.MASTER_PUSH_MESSAGES,
        new MasterPushMessageRequestProcessor(masterConnectorContext),
        null);

    client.updateNameServerAddressList(Lists.newArrayList(nodeAddress));

    // register
    masterInstance.registerClientInstance(
        this.masterConnectorContext, this.masterConnectorProperties, config, client);
    return masterInstance;
  }

  /**
   * Start up master connector by user
   *
   * <p>
   */
  public void startup(MasterConnectorHandler handler) {

    if (startup.compareAndSet(false, true)) {
      this.masterConnectorContext.registerMasterConnectorHandler(handler);
      logger.info("register-ed master connector user's handler :{} ", handler);

      // startup client connect
      if (!masterInstances.isEmpty()) {
        CountDownLatch latch = new CountDownLatch(masterInstances.size());
        long start = System.currentTimeMillis();
        for (MasterInstance masterInstance : masterInstances) {
          try {
            logger.info(
                "\r\n >>>> Try starting up connector - {}:{} ",
                masterInstance.getHost(),
                masterInstance.getPort());

            masterInstance.startup(latch);

          } catch (Exception e) {
            logger.error(
                "exception on connecting , self-thread will try daemon. - {}:{}",
                masterInstance.getHost(),
                masterInstance.getPort(),
                e);
          }
        }

        try {
          latch.await();
          logger.info(
              "master connector(s) all executed startup ,Use Time :{} ms",
              (System.currentTimeMillis() - start));
        } catch (InterruptedException ignored) {
        }

        // startup rolling pulling cluster-replica thread
        logger.info("starting up connector rolling pull cluster replica thread executor . ");
        rollingPullClusterListExecutor =
            new ScheduledThreadPoolExecutor(1, new DefaultThreadFactory("rolling-pull-thread"));
        rollingPullClusterListExecutor.scheduleWithFixedDelay(
            new RollingPullClusterReplicaThread(),
            this.masterConnectorProperties.getConnectorClusterReplicaRollingPullDelay(),
            this.masterConnectorProperties.getConnectorClusterReplicaRollingPullDelay(),
            TimeUnit.SECONDS);

        logger.info("starting up connector sync cluster channels data thread executor . ");
        if (syncChannelsExecutor == null) {
          syncChannelsExecutor =
              new ScheduledThreadPoolExecutor(1, new DefaultThreadFactory("rolling-push-thread"));
          syncChannelsExecutor.scheduleWithFixedDelay(
              new SyncPushClusterChannelsThread(),
              this.masterConnectorProperties.getConnectorClusterChannelsSyncDelay(),
              this.masterConnectorProperties.getConnectorClusterChannelsSyncPeriod(),
              TimeUnit.SECONDS);
        }

        logger.info("master connector(s) service is started. ");
      } else {
        logger.warn("not config master server node(s) address.");
      }
    }
  }

  /**
   * Broadcast connector message
   *
   * @param message instance of {@link Message}
   */
  public void broadcastMessage(Message message) {
    for (MasterInstance masterInstance : masterInstances) {
      RetriableThreadExecutor<Boolean> executor =
          new RetriableThreadExecutor<>(
              "BROADCAST-MQ-MESSAGE",
              () -> {
                ClusterForwardMessageHeader header = new ClusterForwardMessageHeader();
                header.setNamespace(message.getNamespace());
                RemotingCommand remotingCommand =
                    RemotingCommand.createRequestCommand(
                        MasterClusterCommand.CLUSTER_FORWARD_MESSAGES, header);
                if (masterInstance.isConnected()) {
                  RemotingCommand response =
                      masterInstance
                          .getClient()
                          .invokeSync(
                              masterInstance.serverAddress(),
                              remotingCommand,
                              masterConnectorProperties.getConnectorRequestTimeout());
                  if (response != null && response.getBody() != null) {
                    BizResult result = BizResult.fromBytes(response.getBody(), BizResult.class);
                    return result.getCode() == 0;
                  }
                }
                return false;
              },
              new RetriableAttribute(2, 1, TimeUnit.SECONDS),
              new ExecutorCallback<Boolean>() {
                @Override
                public void onCompleted(Boolean result) {
                  logger.info("Broadcast mq -> Master completed.");
                }

                @Override
                public void onFailed(String message) {
                  logger.warn("Broadcast -> Master failed ,{}", message);
                }
              });

      // execute
      executor.execute();
    }
  }

  private void destroy() {
    logger.info("master connector is ready to destroy.");
    if (EventBus.isEnable()) {
      EventBus.unRegister(PullClusterEvent.class, subscriber);
      logger.info("un-register-ed master connector subscriber : {}", subscriber);
    }

    if (!masterInstances.isEmpty()) {
      for (MasterInstance masterInstance : masterInstances) {
        //
        masterInstance.shutdown();
      }
    }

    if (rollingPullClusterListExecutor != null) {
      ThreadKit.gracefulShutdown(this.rollingPullClusterListExecutor, 5, 5, TimeUnit.SECONDS);
      logger.info("shutdown-ed rolling pull thread executor .");
    }

    if (syncChannelsExecutor != null) {
      ThreadKit.gracefulShutdown(this.syncChannelsExecutor, 5, 5, TimeUnit.SECONDS);
      logger.info("shutdown-ed sync channels data thread executor .");
    }
  }

  /**
   * Rolling Pull Cluster Replica Thread
   *
   * @since 2.2.0
   */
  class RollingPullClusterReplicaThread implements Runnable {
    @Override
    public void run() {
      String server = null;
      MasterInstance instance = null;
      while (true) {
        Random indexRandom = new Random();
        int index = indexRandom.nextInt(masterInstances.size());
        instance = masterInstances.get(index);
        if (instance.isConnected()) {
          server = instance.serverAddress();
          break;
        }
      }

      if (server.trim().length() > 0) {
        try {
          RemotingCommand pullRequest =
              RemotingCommand.createRequestCommand(
                  MasterClusterCommand.CLUSTER_PULL_REPLICAS, null);
          RemotingCommand response =
              instance
                  .getClient()
                  .invokeSync(
                      server, pullRequest, masterConnectorProperties.getConnectorRequestTimeout());
          if (response != null) {
            byte[] body = response.getBody();
            if (body != null) {
              BizResult bizResult = JSON.parseObject(body, BizResult.class);
              if (bizResult != null && bizResult.getCode() == 0) {
                @SuppressWarnings("unchecked")
                Set<String> clusterReplicas =
                    JSON.parseObject(JSON.toJSONString(bizResult.getData()), Set.class);
                if (clusterReplicas != null && clusterReplicas.size() > 0) {

                  if (logger.isDebugEnabled()) {
                    logger.debug(
                        "Master - {} returned cluster replicas :{}",
                        server,
                        JSON.toJSONString(clusterReplicas));
                  }

                  if (clusterReplicas.size() > 0) {
                    Event refreshEvent = new PullClusterEvent(clusterReplicas);
                    EventBus.post(refreshEvent);
                    if (logger.isDebugEnabled()) {
                      logger.debug("posted event :{} ", refreshEvent);
                    }
                  }
                }
              }
            }
          }
        } catch (Exception e) {
          logger.error(
              "Rolling pull cluster replicas server list failed with request:{} ,will try next",
              server,
              e);
        }
      } else {
        logger.warn("Current time has no available(connected) master servers.");
      }
    }
  }

  class SyncPushClusterChannelsThread implements Runnable {

    @Override
    public void run() {
      try {
        final CountDownLatch countDownLatch = new CountDownLatch(masterInstances.size());
        for (MasterInstance masterInstance : masterInstances) {

          if (!masterInstance.isConnected()) {
            countDownLatch.countDown();
            continue;
          }
          AsyncRuntimeExecutor.getAsyncThreadPool()
              .execute(
                  () -> {
                    try {

                      ClusterPushSessionDataHeader header = new ClusterPushSessionDataHeader();
                      RemotingCommand requestCommand =
                          RemotingCommand.createRequestCommand(
                              MasterClusterCommand.CLUSTER_PUSH_CLIENT_CHANNELS, header);

                      ClusterPushSessionDataBody body = new ClusterPushSessionDataBody();

                      body.setPassportIds(masterConnectorContext.getOnlinePassports());
                      body.setDeviceIds(masterConnectorContext.getOnlineDevices());

                      requestCommand.setBody(JSON.toJSONBytes(body));

                      RemotingCommand response =
                          masterInstance
                              .getClient()
                              .invokeSync(
                                  masterInstance.serverAddress(),
                                  requestCommand,
                                  masterConnectorProperties.getConnectorRequestTimeout());

                      if (response != null) {
                        BizResult bizResult = JSON.parseObject(response.getBody(), BizResult.class);
                        if (bizResult != null && bizResult.getCode() == 0) {
                          logger.info("cluster push remoting channels timer execute succeed.");
                        } else {
                          logger.warn(
                              "cluster push remoting channels timer execute failed ,response is : {}",
                              JSON.toJSONString(bizResult));
                        }
                      } else {
                        logger.warn(
                            "cluster push remoting channels timer execute failed without return response .");
                      }

                    } catch (Exception e) {
                      logger.error(
                          "cluster push remoting channels timer execute failed with request :{} ,will try next",
                          masterInstance.serverAddress(),
                          e);
                    } finally {
                      countDownLatch.countDown();
                    }
                  });
        }

        countDownLatch.await();
      } catch (Exception e) {
        logger.error("cluster push remoting channels timer execute failed", e);
      }
    }
  }
}
