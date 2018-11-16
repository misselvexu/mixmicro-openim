package com.acmedcare.framework.newim.server.processor;

import com.acmedcare.framework.newim.server.core.ClusterReplicaSession;
import com.acmedcare.tiffany.framework.remoting.protocol.RemotingCommand;
import io.netty.channel.ChannelHandlerContext;

/**
 * Cluster Register Request Processor
 *
 * @author <a href="mailto:iskp.me@gmail.com">Elve.Xu</a>
 * @version ${project.version} - 13/11/2018.
 */
public class ClusterReplicaRegisterRequestProcessor extends AbstractClusterRequestProcessor {

  private final ClusterReplicaSession clusterReplicaSession;

  public ClusterReplicaRegisterRequestProcessor(ClusterReplicaSession clusterReplicaSession) {
    this.clusterReplicaSession = clusterReplicaSession;
  }

  @Override
  public RemotingCommand processRequest(
      ChannelHandlerContext channelHandlerContext, RemotingCommand remotingCommand)
      throws Exception {

    // TODO 处理通讯服务器Replica链接注册

    return null;
  }
}
