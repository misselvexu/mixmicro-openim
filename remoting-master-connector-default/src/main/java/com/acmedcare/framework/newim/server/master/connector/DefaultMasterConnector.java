package com.acmedcare.framework.newim.server.master.connector;

import com.acmedcare.framework.kits.event.Event;
import com.acmedcare.framework.kits.event.EventBus;
import com.acmedcare.framework.kits.executor.AsyncRuntimeExecutor;
import com.acmedcare.framework.kits.lang.Nullable;
import com.acmedcare.framework.kits.thread.ThreadKit;
import com.acmedcare.framework.newim.BizResult;
import com.acmedcare.framework.newim.protocol.Command.MasterClusterCommand;
import com.acmedcare.framework.newim.protocol.request.ClusterPushSessionDataBody;
import com.acmedcare.framework.newim.protocol.request.ClusterPushSessionDataHeader;
import com.acmedcare.framework.newim.server.master.connector.event.PullClusterEvent;
import com.acmedcare.framework.newim.server.master.connector.processors.MasterNoticeClientChannelsRequestProcessor;
import com.acmedcare.framework.newim.server.master.connector.processors.MasterPushMessageRequestProcessor;
import com.acmedcare.tiffany.framework.remoting.netty.NettyClientConfig;
import com.acmedcare.tiffany.framework.remoting.netty.NettyRemotingSocketClient;
import com.acmedcare.tiffany.framework.remoting.protocol.RemotingCommand;
import com.alibaba.fastjson.JSON;
import io.netty.util.concurrent.DefaultThreadFactory;

import java.util.Random;
import java.util.Set;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * MasterConnector
 *
 * @author <a href="mailto:iskp.me@gmail.com">Elve.Xu</a>
 * @version ${project.version} - 2018-12-17.
 */
public final class DefaultMasterConnector extends MasterConnector {

  private static volatile AtomicBoolean startup = new AtomicBoolean(false);
  private DefaultMasterConnectorContext defaultMasterConnectorContext;

  private ScheduledExecutorService rollingPullClusterListExecutor;
  private ScheduledExecutorService syncChannelsExecutor;

  DefaultMasterConnector(MasterConnectorProperties properties) {
    super(properties, new DefaultMasterConnectorContext());
    this.defaultMasterConnectorContext = (DefaultMasterConnectorContext) super.context;
    logger.info("master connector properties : {}", properties);
    this.masterConnectorProperties = properties;
  }

  @Override
  protected MasterInstance registerClientProcessor(
      String nodeAddress, NettyClientConfig config, NettyRemotingSocketClient client) {
    DefaultMasterInstance defaultMasterInstance = DefaultMasterInstance.newInstance(nodeAddress);
    // processor

    client.registerProcessor(
        MasterClusterCommand.MASTER_NOTICE_CLIENT_CHANNELS,
        new MasterNoticeClientChannelsRequestProcessor(defaultMasterConnectorContext),
        null);

    client.registerProcessor(
        MasterClusterCommand.MASTER_PUSH_MESSAGES,
        new MasterPushMessageRequestProcessor(defaultMasterConnectorContext),
        null);

    // register
    defaultMasterInstance.registerClientInstance(
        this.defaultMasterConnectorContext, this.masterConnectorProperties, config, client);

    return defaultMasterInstance;
  }

  @Override
  protected void registerEvent(MasterConnectorSubscriber subscriber) {
    EventBus.register(PullClusterEvent.class, subscriber);
  }

  /**
   * Start up master connector by user
   *
   * <p>
   */
  public void startup(@Nullable DefaultMasterConnectorHandler handler) {

    if (startup.compareAndSet(false, true)) {
      this.defaultMasterConnectorContext.registerMasterConnectorHandler(handler);
      logger.info("register-ed master connector user's handler :{} ", handler);

      // startup client connect
      if (!defaultMasterInstances.isEmpty()) {
        CountDownLatch latch = new CountDownLatch(defaultMasterInstances.size());
        long start = System.currentTimeMillis();
        for (MasterInstance defaultMasterInstance : DefaultMasterConnector.this.defaultMasterInstances) {
          try {
            logger.info(
                "\r\n >>>> Try starting up connector - {}:{} ",
                defaultMasterInstance.getHost(),
                defaultMasterInstance.getPort());

            defaultMasterInstance.startup(latch);

          } catch (Exception e) {
            logger.error(
                "exception on connecting , self-thread will try daemon. - {}:{}",
                defaultMasterInstance.getHost(),
                defaultMasterInstance.getPort(),
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

  public void destroy() {
    logger.info("master connector is ready to destroy.");
    if (EventBus.isEnable()) {
      EventBus.unRegister(PullClusterEvent.class, this.subscriber);
      logger.info("un-register-ed master connector subscriber : {}", subscriber);
    }

    if (!defaultMasterInstances.isEmpty()) {
      for (MasterInstance defaultMasterInstance : DefaultMasterConnector.this.defaultMasterInstances) {
        //
        defaultMasterInstance.shutdown();
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
        int index = indexRandom.nextInt(DefaultMasterConnector.this.defaultMasterInstances.size());
        instance = DefaultMasterConnector.this.defaultMasterInstances.get(index);
        if (instance.isConnected()) {
          server = instance.serverAddress();
          break;
        }
      }

      if (server.trim().length() > 0) {

        // 获取IM服务器备份列表
        try {
          RemotingCommand pullRequest =
              RemotingCommand.createRequestCommand(
                  MasterClusterCommand.IM_SERVER_PULL_REPLICAS, null);
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
        final CountDownLatch countDownLatch = new CountDownLatch(defaultMasterInstances.size());
        for (MasterInstance defaultMasterInstance : DefaultMasterConnector.this.defaultMasterInstances) {

          if (!defaultMasterInstance.isConnected()) {
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

                      body.setPassportIds(defaultMasterConnectorContext.getOnlinePassports());
                      body.setDeviceIds(defaultMasterConnectorContext.getOnlineDevices());

                      requestCommand.setBody(JSON.toJSONBytes(body));

                      RemotingCommand response =
                          defaultMasterInstance
                              .getClient()
                              .invokeSync(
                                  defaultMasterInstance.serverAddress(),
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
                          defaultMasterInstance.serverAddress(),
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
