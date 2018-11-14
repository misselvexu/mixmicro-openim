package com.acmedcare.framework.newim;

import java.io.Serializable;
import java.util.Objects;
import lombok.Getter;
import lombok.Setter;

/**
 * Node
 *
 * @author <a href="mailto:iskp.me@gmail.com">Elve.Xu</a>
 * @version ${project.version} - 14/11/2018.
 */
@Getter
@Setter
public class InstanceNode implements Serializable {

  private static final long serialVersionUID = -4560765383064351784L;
  private String host;
  private NodeType nodeType;

  public InstanceNode(String host, NodeType nodeType) {
    this.host = host;
    this.nodeType = nodeType;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (!(o instanceof InstanceNode)) {
      return false;
    }
    InstanceNode that = (InstanceNode) o;
    return Objects.equals(getHost(), that.getHost()) && getNodeType() == that.getNodeType();
  }

  @Override
  public int hashCode() {
    return Objects.hash(getHost(), getNodeType());
  }

  /** 节点类型 */
  public enum NodeType {
    MASTER,
    CLUSTER,
    CLIENT
  }
}
