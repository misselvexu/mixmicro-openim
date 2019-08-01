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

  @CFNotNull private String nodeServerHost;
  @CFNotNull private String nodeServerExportHost;
  @CFNotNull private String nodeServerType = NodeType.DEFAULT.name();
  @CFNotNull private String nodeServerAddress;
  @CFNotNull private boolean hasWssEndpoints = false;
  private String zone = "default";

  private InstanceType decodeInstanceType() {
    return InstanceType.valueOf(this.nodeServerType);
  }

  public InstanceNode defaultInstance() {
    InstanceNode node = InstanceNode.builder()
        .host(this.nodeServerHost)
        .instanceType(decodeInstanceType())
        .zone(this.zone)
        .build();

    node.setExportHost(this.nodeServerExportHost);

    return node;
  }

  @Override
  public void checkFields() throws RemotingCommandException {}
}
