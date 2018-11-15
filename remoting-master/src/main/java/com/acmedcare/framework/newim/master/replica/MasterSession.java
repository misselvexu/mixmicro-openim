package com.acmedcare.framework.newim.master.replica;

import static com.acmedcare.framework.newim.MasterLogger.masterClusterAcceptorLog;
import static com.acmedcare.framework.newim.MasterLogger.masterReplicaLog;

import com.acmedcare.framework.kits.thread.DefaultThreadFactory;
import com.acmedcare.framework.kits.thread.ThreadKit;
import com.acmedcare.framework.newim.InstanceNode;
import com.acmedcare.framework.newim.master.processor.request.MasterSyncClusterSessionBody;
import com.acmedcare.framework.newim.master.processor.request.MasterSyncClusterSessionHeader;
import com.acmedcare.framework.newim.protocol.Command.MasterClusterCommand;
import com.acmedcare.framework.newim.protocol.request.ClusterPushSessionDataBody;
import com.acmedcare.framework.newim.protocol.request.MasterNoticeSessionDataBody;
import com.acmedcare.tiffany.framework.remoting.netty.NettyClientConfig;
import com.acmedcare.tiffany.framework.remoting.netty.NettyRemotingSocketClient;
import com.acmedcare.tiffany.framework.remoting.protocol.RemotingCommand;
import com.alibaba.fastjson.JSON;
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
import javax.annotation.PostConstruct;
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
   * Replica Sync DataVersion Cache
   *
   * <p>
   *
   * @see MasterSyncClusterSessionHeader#dataVersion
   */
  private static Map<InstanceNode, Integer> syncVersions = Maps.newConcurrentMap();

  /**
   * 用户链接节点缓存
   *
   * <p>key-value
   *
   * <p>Master-Replica-Node: (passportId -> Set[])
   *
   * @see com.acmedcare.framework.newim.InstanceNode.NodeType#CLUSTER
   */
  private static Map<InstanceNode, Map<String, Set<InstanceNode>>> passportsConnections =
      Maps.newConcurrentMap();

  /**
   * 设备链接节点缓存
   *
   * <p>key-value
   *
   * <p>Master-Replica-Node: (deviceId -> Set[])
   *
   * @see com.acmedcare.framework.newim.InstanceNode.NodeType#CLUSTER
   */
  private static Map<InstanceNode, Map<String, Set<InstanceNode>>> devicesConnections =
      Maps.newConcurrentMap();

  /**
   * 校验同步的数据版本
   *
   * @param node 节点实例
   * @param dataVersion 数据版本
   * @return true/false
   */
  public boolean checkSyncDataVersion(InstanceNode node, Integer dataVersion) {
    if (syncVersions.containsKey(node)) {
      return syncVersions.get(node) <= dataVersion;
    }
    return true;
  }

  /**
   * 合并覆盖数据
   *
   * @param node 节点实例
   * @param data 数据
   */
  public void merge(InstanceNode node, MasterSyncClusterSessionBody data, Integer dataVersion) {
    // merge
    if (data.getDeviceIds() != null) {
      for (Map.Entry<InstanceNode, List<String>> entry : data.getDeviceIds().entrySet()) {
        for (String deviceId : entry.getValue()) {
          Map<String, Set<InstanceNode>> temp = Maps.newConcurrentMap();
          temp.put(deviceId, Sets.newHashSet(entry.getKey()));
          devicesConnections.put(node, temp);
        }
      }
    }

    if (data.getPassportIds() != null) {
      for (Map.Entry<InstanceNode, List<String>> entry : data.getPassportIds().entrySet()) {
        for (String passportId : entry.getValue()) {
          Map<String, Set<InstanceNode>> temp = Maps.newConcurrentMap();
          temp.put(passportId, Sets.newHashSet(entry.getKey()));
          passportsConnections.put(node, temp);
        }
      }
    }

    // set newest version
    syncVersions.put(node, dataVersion);
  }

  /**
   * Master Replica Session
   *
   * <p>
   */
  public static class MasterReplicaSession {

    /**
     * Master副本链接集合缓存
     *
     * <p>
     */
    private static Map<String, RemoteReplicaInstance> replicaInstances = Maps.newConcurrentMap();

    public void registerReplica(String replicaAddress, RemoteReplicaInstance instance) {
      masterReplicaLog.info(
          "[MASTER-SESSION] Replica-Server :{} request to register..", replicaAddress);
      RemoteReplicaInstance original = replicaInstances.put(replicaAddress, instance);
      if (original != null) {
        masterReplicaLog.warn(
            "[MASTER-SESSION] New Replica-Server connected ,shutdown all old replica server.");
        // shutdown original client
        original.getMasterRemoteReplicaChannel().close();
      }
    }

    public void revokeReplica(String replicaAddress) {
      RemoteReplicaInstance instance = replicaInstances.remove(replicaAddress);
      if (instance != null) {
        instance.getMasterRemoteReplicaChannel().close();
      }
    }
  }

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

    private ScheduledExecutorService notifierExecutor;
    private ExecutorService asyncNotifierExecutor;

    public void registerClusterInstance(String clusterAddress, Channel channel) {
      RemoteClusterClientInstance original =
          clusterClientInstances.put(
              clusterAddress,
              RemoteClusterClientInstance.builder().clusterClientChannel(channel).build());
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
    }

    public void merge(InstanceNode node, ClusterPushSessionDataBody data) {
      if (data != null) {
        passportsConnections.put(node, Sets.newHashSet(data.getPassportIds()));
        devicesConnections.put(node, Sets.newHashSet(data.getDeviceIds()));
      }
    }

    @PostConstruct
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

      // add shutdown hook
      Runtime.getRuntime()
          .addShutdownHook(
              new Thread(
                  () -> {
                    masterClusterAcceptorLog.info("jvm hook , shutdown notifierExecutor .");
                    ThreadKit.gracefulShutdown(notifierExecutor, 10, 10, TimeUnit.SECONDS);
                    masterClusterAcceptorLog.info("jvm hook , shutdown asyncNotifierExecutor .");
                    ThreadKit.gracefulShutdown(asyncNotifierExecutor, 10, 10, TimeUnit.SECONDS);
                  }));
    }
  }

  /**
   * Master副本远程实例
   *
   * <p>
   */
  @Getter
  @Setter
  @Builder
  public static class RemoteReplicaInstance {
    private Channel masterRemoteReplicaChannel;
  }

  /**
   * 远程副本链接客户端
   *
   * <p>
   */
  @Getter
  @Setter
  public static class RemoteReplicaConnectorInstance {

    private InstanceNode connectorNode;
    /** 副本配置 */
    private NettyClientConfig nettyClientConfig;
    /** 副本客户端对象 */
    private NettyRemotingSocketClient nettyRemotingSocketClient;

    public void start() {
      if (nettyRemotingSocketClient != null) {
        nettyRemotingSocketClient.start();
      }
    }

    public void shutdown() {
      if (nettyRemotingSocketClient != null) {
        nettyRemotingSocketClient.shutdown();
      }
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
