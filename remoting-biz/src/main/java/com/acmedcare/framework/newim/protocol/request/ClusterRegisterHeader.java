package com.acmedcare.framework.newim.protocol.request;

import com.acmedcare.framework.newim.InstanceNode;
import com.acmedcare.framework.newim.InstanceNode.NodeType;
import com.acmedcare.framework.newim.InstanceType;
import com.acmedcare.tiffany.framework.remoting.CommandCustomHeader;
import com.acmedcare.tiffany.framework.remoting.annotation.CFNotNull;
import com.acmedcare.tiffany.framework.remoting.exception.RemotingCommandException;
import lombok.Getter;
import lombok.Setter;

/**
 * Cluster Register Header
 *
 * @author <a href="mailto:iskp.me@gmail.com">Elve.Xu</a>
 * @version ${project.version} - 15/11/2018.
 */
@Getter
@Setter
public class ClusterRegisterHeader implements CommandCustomHeader {

  @CFNotNull private String clusterServerHost; //
  @CFNotNull private String clusterServerType = NodeType.DEFAULT.name(); // default type -> cluster
  @CFNotNull private String clusterReplicaAddress; // 节点replica-> host:port
  @CFNotNull private boolean hasWssEndpoints = false;
  private String zone = "default";

  public InstanceType decodeInstanceType() {
    return InstanceType.valueOf(this.clusterServerType);
  }

  public InstanceNode defaultInstance() {
    return InstanceNode.builder()
        .host(clusterServerHost)
        .instanceType(decodeInstanceType())
        .zone("default")
        .build();
  }

  public InstanceNode defaultReplica() {
    return InstanceNode.builder()
        .host(clusterReplicaAddress)
        .nodeType(NodeType.DEFAULT_REPLICA)
        .zone("default")
        .build();
  }

  @Override
  public void checkFields() throws RemotingCommandException {}
}
