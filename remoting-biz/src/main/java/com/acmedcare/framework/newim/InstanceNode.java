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
  /** @deprecated use {@link InstanceType} instead of */
  private NodeType nodeType;

  private InstanceType instanceType;

  @Builder
  public InstanceNode(String host, NodeType nodeType, String name, InstanceType instanceType) {
    this.host = host;
    this.nodeType = nodeType;
    this.name = name;
    this.instanceType = instanceType;
    if (this.nodeType == null) {
      this.nodeType = NodeType.DEFAULT;
    }
    if (instanceType == null) {
      this.instanceType = InstanceType.DEFAULT;
    }
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
    return Objects.equals(getHost(), node.getHost())
        && (getNodeType() == node.getNodeType() || getInstanceType() == node.getInstanceType());
  }

  @Override
  public int hashCode() {
    return Objects.hash(getHost(), getNodeType(), getInstanceType());
  }

  /** 节点类型 */
  public enum NodeType {
    MASTER,

    DEFAULT,

    /** @deprecated use {@link #DEFAULT} instead of */
    CLUSTER,

    /** @deprecated use {@link #DEFAULT_REPLICA} instead of */
    REPLICA,
    DEFAULT_REPLICA,
    WSS,
    CLIENT
  }
}
