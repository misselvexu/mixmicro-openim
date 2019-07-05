package com.acmedcare.framework.newim.master.core;

import com.acmedcare.framework.kits.thread.DefaultThreadFactory;
import com.acmedcare.framework.kits.thread.ThreadKit;
import com.acmedcare.framework.newim.*;
import com.acmedcare.framework.newim.client.MessageAttribute;
import com.acmedcare.framework.newim.protocol.Command.MasterClusterCommand;
import com.acmedcare.framework.newim.protocol.request.ClusterPushSessionDataBody;
import com.acmedcare.framework.newim.protocol.request.ClusterRegisterBody.WssInstance;
import com.acmedcare.framework.newim.protocol.request.MasterNoticeSessionDataBody;
import com.acmedcare.framework.newim.protocol.request.MasterPushMessageHeader;
import com.acmedcare.tiffany.framework.remoting.protocol.RemotingCommand;
import com.acmedcare.tiffany.framework.remoting.protocol.RemotingSerializable;
import com.alibaba.fastjson.JSON;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import io.netty.channel.Channel;
import io.netty.util.AttributeKey;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.*;
import java.util.concurrent.ThreadPoolExecutor.CallerRunsPolicy;

import static com.acmedcare.framework.newim.MasterLogger.masterClusterAcceptorLog;

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

    public static final AttributeKey<InstanceNode> CLUSTER_INSTANCE_NODE_ATTRIBUTE_KEY =
        AttributeKey.newInstance("CLUSTER_INSTANCE_NODE_ATTRIBUTE_KEY");

    /**
     * 通行证登录连接
     *
     * <p>
     */
    private static Map<InstanceNode, Set<SessionBean>> passportsConnections =
        Maps.newConcurrentMap();

    /**
     * 设备登录连接
     *
     * <p>
     */
    private static Map<InstanceNode, Set<SessionBean>> devicesConnections = Maps.newConcurrentMap();

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
    private MasterClusterAcceptorServer masterClusterAcceptorServer;

    MasterClusterSession() {
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

    public static InstanceNode decodeInstanceNode(Channel channel) {
      return channel.attr(CLUSTER_INSTANCE_NODE_ATTRIBUTE_KEY).get();
    }

    public Set<String> clusterList(InstanceType instanceType, String zone) {
      Set<String> result = Sets.newHashSet();
      clusterClientInstances.forEach(
          (s, remoteClusterClientInstance) -> {
            if (remoteClusterClientInstance.getInstanceType().equals(instanceType)
                // add zone flag
                && zone.equalsIgnoreCase(remoteClusterClientInstance.getZone())) {
              result.add(s);
            }
          });

      return result;
    }

    public Set<String> clusterReplicaList(InstanceType instanceType) {
      Set<String> replicas = Sets.newHashSet();
      clusterClientInstances.forEach(
          (s, remoteClusterClientInstance) -> {
            if (remoteClusterClientInstance.getInstanceType().equals(instanceType)) {
              replicas.add(remoteClusterClientInstance.getClusterReplicaAddress());
            }
          });
      return replicas;
    }

    public List<WssInstance> wssList(String zone) {
      List<WssInstance> result = Lists.newArrayList();
      for (Map<String, WssInstance> map : clusterWssServerInstance.values()) {
        for (WssInstance value : map.values()) {
          if (zone.equalsIgnoreCase(value.getZone())) {
            result.add(value);
          }
        }
      }
      return result;
    }

    public void registerClusterInstance(
        InstanceNode remoteNode,
        String clusterAddress,
        String clusterReplicaAddress,
        List<WssInstance> wssNodes,
        Channel channel) {

      InstanceType instanceType = remoteNode.getInstanceType();

      RemoteClusterClientInstance original =
          clusterClientInstances.put(
              clusterAddress,
              RemoteClusterClientInstance.builder()
                  .instanceType(instanceType)
                  .clusterReplicaAddress(clusterReplicaAddress)
                  .clusterClientChannel(channel)
                  .zone(remoteNode.getZone())
                  .build());

      if (wssNodes != null && !wssNodes.isEmpty()) {
        // process wss
        Map<String, WssInstance> temp = Maps.newHashMap();
        for (WssInstance wssNode : wssNodes) {
          temp.put(wssNode.getWssName(), wssNode);
        }
        clusterWssServerInstance.put(clusterAddress, temp);
      }

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

    public void distributeMessage(
        MessageAttribute attribute, Message message, String excludeForwardAddress) {
      Map<String, RemoteClusterClientInstance> temp = Maps.newConcurrentMap();
      clusterClientInstances.forEach(
          (address, remoteClusterClientInstance) -> {
            if (excludeForwardAddress != null && !Objects.equals(excludeForwardAddress, address)) {
              temp.put(address, remoteClusterClientInstance);
            }
          });
      //
      distributeMessage(attribute, message, temp);
    }

    private void distributeMessage(
        MessageAttribute attribute,
        Message message,
        Map<String, RemoteClusterClientInstance> clusterClientInstances) {
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
                    header.setNamespace(attribute.getNamespace());

                    RemotingCommand distributeRequest =
                        RemotingCommand.createRequestCommand(
                            MasterClusterCommand.MASTER_PUSH_MESSAGES, header);
                    distributeRequest.setBody(message.bytes());

                    RemotingCommand response =
                        masterClusterAcceptorServer
                            .getMasterClusterAcceptorServer()
                            .invokeSync(channel, distributeRequest, 3000);

                    if (response != null) {
                      BizResult bizResult =
                          RemotingSerializable.decode(response.getBody(), BizResult.class);
                      if (bizResult.getCode() == 0) {
                        masterClusterAcceptorLog.info(
                            "master distribute message to server:{} succeed.", address);
                      } else {
                        // TODO failed
                      }
                    } else {
                      // TODO failed

                    }
                  } catch (Exception e) {
                    masterClusterAcceptorLog.error(
                        "master distribute message to server:{} exception", address, e);
                    // TODO exception
                  }
                });
          });
    }

    /**
     * 分发消息
     *
     * @param attribute 消息属性
     * @param message 消息
     */
    public void distributeMessage(MessageAttribute attribute, Message message) {
      distributeMessage(attribute, message, clusterClientInstances);
    }

    void notifier() {
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
            try {
              if (clusterClientInstances.size() > 0) {

                if (devicesConnections.size() == 0 || passportsConnections.size() == 0) {
                  return;
                }

                CountDownLatch count = new CountDownLatch(clusterClientInstances.size());

                clusterClientInstances.forEach(
                    (key, value) ->
                        asyncNotifierExecutor.execute(
                            () -> {
                              try {

                                // type
                                InstanceType instanceType = value.getInstanceType();

                                if (value.getClusterClientChannel().isWritable()) {

                                  RemotingCommand notifyRequest =
                                      RemotingCommand.createRequestCommand(
                                          MasterClusterCommand.MASTER_NOTICE_CLIENT_CHANNELS, null);
                                  MasterNoticeSessionDataBody body =
                                      new MasterNoticeSessionDataBody();

                                  devicesConnections.forEach(
                                      (instanceNode, sessionBeans) -> {
                                        for (SessionBean sessionBean : sessionBeans) {
                                          if (sessionBean
                                              .getNamespace()
                                              .equals(instanceType.name())) {
                                            body.getDevicesConnections().add(sessionBean);
                                          }
                                        }
                                      });

                                  passportsConnections.forEach(
                                      (instanceNode, sessionBeans) -> {
                                        for (SessionBean sessionBean : sessionBeans) {
                                          if (sessionBean
                                              .getNamespace()
                                              .equals(instanceType.name())) {
                                            body.getPassportsConnections().add(sessionBean);
                                          }
                                        }
                                      });

                                  notifyRequest.setBody(JSON.toJSONBytes(body));

                                  RemotingCommand response =
                                      masterClusterAcceptorServer
                                          .getMasterClusterAcceptorServer()
                                          .invokeSync(
                                              value.getClusterClientChannel(), notifyRequest, 3000);

                                  if (response != null) {
                                    byte[] byteBody = response.getBody();
                                    if (byteBody != null && byteBody.length > 0) {
                                      BizResult bizResult =
                                          RemotingSerializable.decode(
                                              response.getBody(), BizResult.class);
                                      if (bizResult != null && bizResult.getCode() == 0) {
                                        masterClusterAcceptorLog.info(
                                            "master notify push session data succeed.");
                                      } else {
                                        masterClusterAcceptorLog.warn(
                                            "master notify push session data failed, cluster return response : {} "
                                                + bizResult.json());
                                      }
                                    }
                                  } else {
                                    masterClusterAcceptorLog.warn(
                                        "master notify push session data failed, without cluster response");
                                  }
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

                try {
                  count.await();
                } catch (InterruptedException e) {
                  masterClusterAcceptorLog.info("wait current round execute finished failed.", e);
                }
              }
            } catch (Exception e) {
              masterClusterAcceptorLog.error(
                  "master notify push session time executor execute failed", e);
            }
          },
          10,
          10,
          TimeUnit.SECONDS);
    }

    void shutdownAll() {
      clusterClientInstances.forEach(
          (key, value) -> {
            try {
              masterClusterAcceptorLog.info(
                  "shutdown remote client channel: {} - {}",
                  value.getInstanceType(),
                  value.getClusterClientChannel());
              value.getClusterClientChannel().close();
            } catch (Exception ignore) {
            }
          });
    }

    public void registerServerInstance(MasterClusterAcceptorServer masterClusterAcceptorServer) {
      this.masterClusterAcceptorServer = masterClusterAcceptorServer;
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

    private InstanceType instanceType;

    /** 客户端 Channel对象 */
    private Channel clusterClientChannel;

    private String clusterReplicaAddress;

    @Builder.Default private String zone = "default";
  }
}
