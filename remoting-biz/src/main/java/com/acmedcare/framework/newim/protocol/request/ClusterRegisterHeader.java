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

  @CFNotNull private String nodeServerAddress;
  @CFNotNull private String nodeServerType = NodeType.DEFAULT.name();
  private String nodeServerExportHost = "";
  private String remotingNodeReplicaAddress = "";
  private boolean hasWssEndpoints = false;
  private String zone = "default";

  private InstanceType decodeInstanceType() {
    return InstanceType.valueOf(this.nodeServerType);
  }

  public InstanceNode buildInstance() {
    InstanceNode node = InstanceNode.builder()
        .address(this.nodeServerAddress)
        .instanceType(decodeInstanceType())
        .zone(this.zone)
        .build();

    node.setExportAddress(this.nodeServerExportHost);

    return node;
  }

  @Override
  public void checkFields() throws RemotingCommandException {}
}
