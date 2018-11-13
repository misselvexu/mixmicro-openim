package com.acmedcare.framework.newim.server.core.connector;

import com.acmedcare.tiffany.framework.remoting.RemotingSocketClient;
import com.acmedcare.tiffany.framework.remoting.netty.NettyClientConfig;
import com.acmedcare.tiffany.framework.remoting.netty.NettyRemotingSocketClient;
import com.google.common.collect.Maps;
import java.util.Map;

/**
 * Cluster Connector(s) For Clusters
 *
 * @author <a href="mailto:iskp.me@gmail.com">Elve.Xu</a>
 * @version ${project.version} - 12/11/2018.
 * @see MasterConnector#pullClusterNodesList(NettyRemotingSocketClient) dynamic connector list
 */
public class ClusterConnector {

  private Map<String, RemotingSocketClient> clusterConnectorCache = Maps.newHashMap();
  private Map<String, NettyClientConfig> clusterConfigCache = Maps.newHashMap();

  /**
   * 链接通讯服务器节点
   *
   * @param clusterAddr 通讯服务器地址
   */
  void connectCluster(String clusterAddr) {
    // TODO 链接通讯服务器
  }
}
