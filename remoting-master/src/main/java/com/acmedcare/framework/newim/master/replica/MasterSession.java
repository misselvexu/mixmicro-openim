package com.acmedcare.framework.newim.master.replica;

import static com.acmedcare.framework.newim.MasterLogger.masterReplicaLog;

import com.acmedcare.framework.newim.InstanceNode;
import com.acmedcare.framework.newim.master.processor.body.MasterSyncClusterSessionBody;
import com.acmedcare.tiffany.framework.remoting.netty.NettyClientConfig;
import com.acmedcare.tiffany.framework.remoting.netty.NettyRemotingSocketClient;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import io.netty.channel.Channel;
import java.util.Map;
import java.util.Set;
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
   * @see
   *     com.acmedcare.framework.newim.master.processor.header.MasterSyncClusterSessionHeader#dataVersion
   */
  private static Map<InstanceNode, Integer> syncVersions = Maps.newConcurrentMap();
  /**
   * 用户链接节点缓存
   *
   * <p>passportId -> List[]
   *
   * @see com.acmedcare.framework.newim.InstanceNode.NodeType#CLUSTER
   */
  private static Map<String, Set<InstanceNode>> passportsConnections = Maps.newConcurrentMap();

  /**
   * 设备链接节点缓存
   *
   * <p>deviceId -> List[]
   *
   * @see com.acmedcare.framework.newim.InstanceNode.NodeType#CLUSTER
   */
  private static Map<String, Set<InstanceNode>> devicesConnections = Maps.newConcurrentMap();

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
   * 合并数据
   *
   * @param node 节点实例
   * @param data 数据
   */
  public void merge(InstanceNode node, MasterSyncClusterSessionBody data, Integer dataVersion) {
    // merge
    if (data.getDeviceIds() != null) {
      for (String deviceId : data.getDeviceIds()) {
        if (devicesConnections.containsKey(deviceId)) {
          devicesConnections.get(deviceId).add(node);
        } else {
          devicesConnections.put(deviceId, Sets.newHashSet(node));
        }
      }
    }

    if (data.getPassportIds() != null) {
      for (String passportId : data.getPassportIds()) {
        if (passportsConnections.containsKey(passportId)) {
          passportsConnections.get(passportId).add(node);
        } else {
          passportsConnections.put(passportId, Sets.newHashSet(node));
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
   * Master Cluster Client Session
   *
   * <p>
   */
  public static class MasterClusterSession {

    /**
     * 通讯服务器客户端链接对象池
     *
     * <p>
     */
    private static Map<String, RemoteClusterClientInstance> clusterClientInstances =
        Maps.newConcurrentMap();
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
    /** 副本配置 */
    private NettyClientConfig nettyClientConfig;
    /** 副本客户端对象 */
    private NettyRemotingSocketClient nettyRemotingSocketClient;
  }

  /**
   * Master所连接的Cluster客户端实例对象
   *
   * <p>
   */
  @Getter
  @Setter
  public static class RemoteClusterClientInstance {

    /** 客户端 Channel对象 */
    private Channel clusterClientChannel;
  }
}
