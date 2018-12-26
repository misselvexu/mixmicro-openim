package com.acmedcare.framework.newim.server.replica;

import java.io.Serializable;
import java.util.Objects;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

/**
 * NodeReplicaInstance
 *
 * @author <a href="mailto:iskp.me@gmail.com">Elve.Xu</a>
 * @version ${project.version} - 2018-12-25.
 */
@Getter
@Setter
@Builder
public class NodeReplicaInstance implements Serializable {

  /** Replica node Address */
  private String nodeAddress;

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (!(o instanceof NodeReplicaInstance)) {
      return false;
    }
    NodeReplicaInstance that = (NodeReplicaInstance) o;
    return getNodeAddress().equals(that.getNodeAddress());
  }

  @Override
  public int hashCode() {
    return Objects.hash(getNodeAddress());
  }
}
