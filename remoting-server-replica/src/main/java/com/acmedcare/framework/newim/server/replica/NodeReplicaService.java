package com.acmedcare.framework.newim.server.replica;

import java.util.List;

/**
 * NodeReplicaService
 *
 * @author <a href="mailto:iskp.me@gmail.com">Elve.Xu</a>
 * @version ${project.version} - 2018-12-25.
 */
public interface NodeReplicaService {

  /**
   * Get Node Replica List ,This method will be invoked schedule period
   *
   * @return a list of instance {@link NodeInstance}
   * @throws NodeReplicaException exception
   * @see
   *     com.acmedcare.framework.newim.server.replica.NodeReplicaProperties.ReplicaProperties#getInstancesRefreshPeriod()
   *     set period time
   */
  List<NodeInstance> loadNodeInstances() throws NodeReplicaException;
}
