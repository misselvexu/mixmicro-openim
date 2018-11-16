package com.acmedcare.framework.newim.server.core.connector;

import static com.acmedcare.framework.newim.server.ClusterLogger.clusterReplicaLog;

import com.acmedcare.framework.newim.server.config.IMProperties;
import com.acmedcare.tiffany.framework.remoting.ChannelEventListener;
import com.acmedcare.tiffany.framework.remoting.netty.NettyClientConfig;
import com.acmedcare.tiffany.framework.remoting.netty.NettyRemotingSocketClient;
import com.google.common.collect.Lists;
import io.netty.channel.Channel;
import java.util.List;

/**
 * Cluster Replica Connector
 *
 * @author <a href="mailto:iskp.me@gmail.com">Elve.Xu</a>
 * @version ${project.version} - 16/11/2018.
 */
public class ClusterReplicaConnector {

  private final IMProperties properties;
  private final NettyClientConfig nettyClientConfig;
  /** Replica Server List */
  private List<String> replicaServerList = Lists.newArrayList();

  private NettyRemotingSocketClient nettyRemotingSocketClient;

  public ClusterReplicaConnector(IMProperties properties) {
    this.properties = properties;
    this.nettyClientConfig = new NettyClientConfig();
    this.nettyClientConfig.setEnableHeartbeat(false);
    this.nettyClientConfig.setClientChannelMaxIdleTimeSeconds(40);
  }

  public void start() {
    if (nettyRemotingSocketClient == null) {
      nettyRemotingSocketClient =
          new NettyRemotingSocketClient(
              nettyClientConfig,
              new ChannelEventListener() {
                @Override
                public void onChannelConnect(String remoteAddr, Channel channel) {
                  clusterReplicaLog.info("Cluster Replica Client[{}] is connected", remoteAddr);
                }

                @Override
                public void onChannelClose(String remoteAddr, Channel channel) {
                  clusterReplicaLog.info("Cluster Replica Client[{}] is closed", remoteAddr);
                }

                @Override
                public void onChannelException(String remoteAddr, Channel channel) {
                  clusterReplicaLog.info(
                      "Cluster Replica Client[{}] is exception ,closing ..", remoteAddr);
                  try {
                    channel.close();
                  } catch (Exception ignore) {
                  }
                }

                @Override
                public void onChannelIdle(String remoteAddr, Channel channel) {
                  clusterReplicaLog.info("Cluster Replica Client[{}] is idle", remoteAddr);
                }
              });
    }

    // update replica address list
    nettyRemotingSocketClient.updateNameServerAddressList(replicaServerList);

    // register processor


    //
    nettyRemotingSocketClient.start();
  }
}
