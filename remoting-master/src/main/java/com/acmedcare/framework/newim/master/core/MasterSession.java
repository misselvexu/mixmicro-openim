package com.acmedcare.framework.newim.master.core;

import static com.acmedcare.framework.newim.MasterLogger.masterClusterAcceptorLog;

import com.acmedcare.framework.kits.thread.DefaultThreadFactory;
import com.acmedcare.framework.kits.thread.ThreadKit;
import com.acmedcare.framework.newim.InstanceNode;
import com.acmedcare.framework.newim.Message;
import com.acmedcare.framework.newim.client.MessageAttribute;
import com.acmedcare.framework.newim.protocol.Command.MasterClusterCommand;
import com.acmedcare.framework.newim.protocol.request.ClusterPushSessionDataBody;
import com.acmedcare.framework.newim.protocol.request.ClusterRegisterBody.WssInstance;
import com.acmedcare.framework.newim.protocol.request.MasterNoticeSessionDataBody;
import com.acmedcare.framework.newim.protocol.request.MasterPushMessageHeader;
import com.acmedcare.tiffany.framework.remoting.protocol.RemotingCommand;
import com.alibaba.fastjson.JSON;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFutureListener;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.ThreadPoolExecutor.CallerRunsPolicy;
import java.util.concurrent.TimeUnit;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

/**
 * Master Session
 *
 * @author <a href="mailto:iskp.me@gmail.com">Elve.Xu</a>
 * @version ${project.version} - 14/11/2018.
 */
public class MasterSession {

  /**
   * Master Replica Client Session
   *
   * <p>
   */
  public static class MasterClusterSession {

    /**
     * 通行证登录连接
     *
     * <p>
     */
    private static Map<InstanceNode, Set<String>> passportsConnections = Maps.newConcurrentMap();

    /**
     * 设备登录连接
     *
     * <p>
     */
    private static Map<InstanceNode, Set<String>> devicesConnections = Maps.newConcurrentMap();

    /**
     * 通讯服务器客户端链接对象池
     *
     * <p>
     */
    private static Map<String, RemoteClusterClientInstance> clusterClientInstances =
        Maps.newConcurrentMap();

    private static Map<String, Map<String, WssInstance>> clusterWssServerInstance =
        Maps.newConcurrentMap();

    private ScheduledExecutorService notifierExecutor;
    private ExecutorService asyncNotifierExecutor;
    private ExecutorService distributeMessageExecutor;

    public MasterClusterSession() {
      notifier();

      // init distributeMessageExecutor
      distributeMessageExecutor =
          new ThreadPoolExecutor(
              4,
              16,
              5000,
              TimeUnit.MILLISECONDS,
              new LinkedBlockingQueue<>(64),
              new DefaultThreadFactory("master-message-distribute-thread-pool"),
              new CallerRunsPolicy());

      // add shutdown hook
      Runtime.getRuntime()
          .addShutdownHook(
              new Thread(
                  () -> {
                    masterClusterAcceptorLog.info("jvm hook , shutdown notifierExecutor .");
                    ThreadKit.gracefulShutdown(notifierExecutor, 10, 10, TimeUnit.SECONDS);
                    masterClusterAcceptorLog.info("jvm hook , shutdown asyncNotifierExecutor .");
                    ThreadKit.gracefulShutdown(asyncNotifierExecutor, 10, 10, TimeUnit.SECONDS);
                    masterClusterAcceptorLog.info(
                        "jvm hook , shutdown distributeMessageExecutor .");
                    ThreadKit.gracefulShutdown(distributeMessageExecutor, 10, 10, TimeUnit.SECONDS);
                  }));
    }

    public Set<String> clusterList() {
      return clusterClientInstances.keySet();
    }

    public List<WssInstance> wssList() {
      List<WssInstance> result = Lists.newArrayList();
      for (Map<String, WssInstance> map : clusterWssServerInstance.values()) {
        result.addAll(map.values());
      }
      return result;
    }

    public void registerClusterInstance(
        String clusterAddress, List<WssInstance> wssNodes, Channel channel) {
      RemoteClusterClientInstance original =
          clusterClientInstances.put(
              clusterAddress,
              RemoteClusterClientInstance.builder().clusterClientChannel(channel).build());

      // process wss
      Map<String, WssInstance> temp = Maps.newHashMap();
      for (WssInstance wssNode : wssNodes) {
        temp.put(wssNode.getWssName(), wssNode);
      }

      clusterWssServerInstance.put(clusterAddress, temp);

      if (original != null) {
        masterClusterAcceptorLog.info(
            "Cluster:{} registered, Auto-release original old instance", clusterAddress);
        // close
        try {
          original.getClusterClientChannel().close();
        } catch (Exception ignore) {
        }
      }
    }

    public void revokeClusterInstance(String clusterAddress) {
      RemoteClusterClientInstance preInstance = clusterClientInstances.remove(clusterAddress);
      if (preInstance != null) {
        masterClusterAcceptorLog.info("Revoke Cluster:{}", clusterAddress);
        // close
        try {
          preInstance.getClusterClientChannel().close();
        } catch (Exception ignore) {
        }
      }

      clusterWssServerInstance.remove(clusterAddress);
    }

    public void merge(InstanceNode node, ClusterPushSessionDataBody data) {
      if (data != null) {
        passportsConnections.put(node, Sets.newHashSet(data.getPassportIds()));
        devicesConnections.put(node, Sets.newHashSet(data.getDeviceIds()));
      }
    }

    /**
     * 批量分发消息(同类型消息)
     *
     * @param attribute 消息属性
     * @param messages 消息内容列表
     */
    public void batchDistributeMessage(MessageAttribute attribute, Message... messages) {
      if (messages != null && messages.length > 0) {
        for (Message message : messages) {
          distributeMessage(attribute, message);
        }
      }
    }

    /**
     * 分发消息
     *
     * @param attribute 消息属性
     * @param message 消息
     */
    public void distributeMessage(MessageAttribute attribute, Message message) {
      // MASTER_PUSH_MESSAGES
      masterClusterAcceptorLog.info("master distribute message to servers.");
      clusterClientInstances.forEach(
          (address, instance) -> {
            masterClusterAcceptorLog.info("master distribute message to server:{}", address);
            Channel channel = instance.getClusterClientChannel();
            distributeMessageExecutor.execute(
                () -> {
                  try {
                    MasterPushMessageHeader header = new MasterPushMessageHeader();
                    // build header
                    header.setInnerType(message.getInnerType().name());
                    header.setMaxRetryTimes(attribute.getMaxRetryTimes());
                    header.setMessageType(message.getMessageType().name());
                    header.setPersistent(attribute.isPersistent());
                    header.setQos(attribute.isQos());
                    header.setRetryPeriod(attribute.getRetryPeriod());

                    RemotingCommand distributeRequest =
                        RemotingCommand.createRequestCommand(
                            MasterClusterCommand.MASTER_PUSH_MESSAGES, header);
                    distributeRequest.setBody(message.bytes());

                    if (channel != null && channel.isWritable()) {
                      channel
                          .writeAndFlush(distributeRequest)
                          .addListener(
                              (ChannelFutureListener)
                                  future -> {
                                    if (future.isSuccess()) {
                                      // success
                                      masterClusterAcceptorLog.info(
                                          "master distribute message to server:{} succeed.",
                                          address);
                                    } else {
                                      // TODO send failed
                                    }
                                  });
                    } else {
                      // TODO no available
                    }
                  } catch (Exception e) {
                    masterClusterAcceptorLog.error(
                        "master distribute message to server:{} exception", address, e);
                    // TODO exception
                  }
                });
          });
    }

    public void notifier() {
      notifierExecutor =
          new ScheduledThreadPoolExecutor(
              1, new DefaultThreadFactory("master-sessions-notifier-thread"));

      asyncNotifierExecutor =
          new ThreadPoolExecutor(
              4,
              16,
              0L,
              TimeUnit.MILLISECONDS,
              new LinkedBlockingQueue<>(16),
              new DefaultThreadFactory("notifier-executor"),
              new CallerRunsPolicy());

      notifierExecutor.scheduleWithFixedDelay(
          () -> {
            if (clusterClientInstances.size() > 0) {

              if (devicesConnections.size() == 0 && passportsConnections.size() == 0) {
                return;
              }

              CountDownLatch count = new CountDownLatch(clusterClientInstances.size());

              RemotingCommand notifyRequest =
                  RemotingCommand.createRequestCommand(
                      MasterClusterCommand.MASTER_NOTICE_CLIENT_CHANNELS, null);
              MasterNoticeSessionDataBody body = new MasterNoticeSessionDataBody();
              body.setDevicesConnections(devicesConnections);
              body.setPassportsConnections(passportsConnections);
              notifyRequest.setBody(JSON.toJSONBytes(body));

              clusterClientInstances.forEach(
                  (key, value) ->
                      asyncNotifierExecutor.execute(
                          () -> {
                            try {
                              if (value.getClusterClientChannel().isWritable()) {
                                value
                                    .getClusterClientChannel()
                                    .writeAndFlush(notifyRequest)
                                    .addListener(
                                        (ChannelFutureListener)
                                            future -> {
                                              if (future.isSuccess()) {
                                                masterClusterAcceptorLog.info(
                                                    "master notify push session data succeed.");
                                              }
                                            });
                              } else {
                                masterClusterAcceptorLog.warn(
                                    "master notify push session data fail , cause by cluster client channel is un-writable");
                              }
                            } catch (Exception e) {
                              masterClusterAcceptorLog.error(
                                  "master notify push session data exception", e);
                            } finally {
                              // release
                              count.countDown();
                            }
                          }));
            }
          },
          10,
          10,
          TimeUnit.SECONDS);
    }

    public void shutdownAll() {
      clusterClientInstances.forEach(
          (key, value) -> {
            try {
              masterClusterAcceptorLog.info(
                  "shutdown remote client channel:{}", value.getClusterClientChannel());
              value.getClusterClientChannel().close();
            } catch (Exception ignore) {
            }
          });
    }
  }

  /**
   * Master所连接的Cluster客户端实例对象
   *
   * <p>
   */
  @Getter
  @Setter
  @Builder
  public static class RemoteClusterClientInstance {

    /** 客户端 Channel对象 */
    private Channel clusterClientChannel;
  }
}
