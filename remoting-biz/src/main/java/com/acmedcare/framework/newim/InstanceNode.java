package com.acmedcare.framework.newim;

import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.io.Serializable;
import java.util.Objects;

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
  private String exportHost;
  /** @deprecated use {@link InstanceType} instead of */
  private NodeType nodeType;

  private InstanceType instanceType;

  private String zone;

  @Builder
  public InstanceNode(
      String host, NodeType nodeType, String name, InstanceType instanceType, String zone) {
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
    if (this.zone == null) {
      this.zone = "default";
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
    /** Master */
    MASTER,

    /** Deliverer Server */
    DELIVERER,

    DEFAULT,

    DEFAULT_REPLICA,

    MQ_SERVER
  }
}
