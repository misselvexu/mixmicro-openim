package com.acmedcare.framework.newim.server.replica;

import com.acmedcare.framework.newim.InstanceType;
import com.acmedcare.framework.newim.server.Context;
import com.google.common.collect.Lists;
import java.util.List;

/**
 * NodeReplicaService
 *
 * @author <a href="mailto:iskp.me@gmail.com">Elve.Xu</a>
 * @version ${project.version} - 2018-12-25.
 */
public interface NodeReplicaService {

  /**
   * return current context
   *
   * @return context
   */
  Context context();

  /**
   * Instance Type
   * @return type
   */
  InstanceType type();

  /**
   * Get Node Replica List ,This method will be invoked schedule period
   *
   * @return a list of instance {@link NodeReplicaInstance}
   * @throws NodeReplicaException exception
   * @see
   *     com.acmedcare.framework.newim.server.replica.NodeReplicaProperties.ReplicaProperties#getInstancesRefreshPeriod()
   *     set period time
   */
  default List<NodeReplicaInstance> loadNodeInstances() throws NodeReplicaException {
    return Lists.newArrayList();
  }
}
