package com.acmedcare.framework.newim.server.master.connector.event;

import com.acmedcare.framework.kits.event.Event;
import java.util.Set;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * PullClusterEvent
 *
 * @author <a href="mailto:iskp.me@gmail.com">Elve.Xu</a>
 * @version ${project.version} - 2018-12-17.
 */
@Getter
@Setter
@NoArgsConstructor
public class PullClusterEvent implements Event {

  /**
   * Master Return Cluster Replicas Set
   *
   * <p>Nullable
   */
  private Set<String> clusterReplicas;

  public PullClusterEvent(Set<String> clusterReplicas) {
    this.clusterReplicas = clusterReplicas;
  }
}
