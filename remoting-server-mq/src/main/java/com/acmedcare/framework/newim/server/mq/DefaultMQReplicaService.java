package com.acmedcare.framework.newim.server.mq;

import com.acmedcare.framework.newim.server.replica.NodeInstance;
import com.acmedcare.framework.newim.server.replica.NodeReplicaException;
import com.acmedcare.framework.newim.server.replica.NodeReplicaProperties.ReplicaProperties;
import com.acmedcare.framework.newim.server.replica.NodeReplicaService;
import java.util.List;

/**
 * DefaultMQReplicaService
 *
 * @author <a href="mailto:iskp.me@gmail.com">Elve.Xu</a>
 * @version ${project.version} - 2018-12-25.
 */
public class DefaultMQReplicaService implements NodeReplicaService {

  /**
   * Get Node Replica List ,This method will be invoked schedule period
   *
   * @return a list of instance {@link NodeInstance}
   * @throws NodeReplicaException exception
   * @see ReplicaProperties#getInstancesRefreshPeriod() set period time
   */
  @Override
  public List<NodeInstance> loadNodeInstances() throws NodeReplicaException {

    //

    return null;
  }
}
