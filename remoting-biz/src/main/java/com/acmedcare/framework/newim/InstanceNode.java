package com.acmedcare.framework.newim;

import java.io.Serializable;
import java.util.Objects;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Node
 *
 * @author <a href="mailto:iskp.me@gmail.com">Elve.Xu</a>
 * @version ${project.version} - 14/11/2018.
 */
@Getter
@Setter
@NoArgsConstructor
public class InstanceNode implements Serializable {

  private static final long serialVersionUID = -4560765383064351784L;
  private String name;
  private String host;
  private NodeType nodeType;

  @Builder
  public InstanceNode(String host, NodeType nodeType, String name) {
    this.host = host;
    this.nodeType = nodeType;
    this.name = name;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (!(o instanceof InstanceNode)) {
      return false;
    }
    InstanceNode node = (InstanceNode) o;
    return Objects.equals(getHost(), node.getHost()) && getNodeType() == node.getNodeType();
  }

  @Override
  public int hashCode() {
    return Objects.hash(getHost(), getNodeType());
  }

  /** 节点类型 */
  public enum NodeType {
    MASTER,
    CLUSTER,
    WSS,
    CLIENT
  }
}
