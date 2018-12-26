package com.acmedcare.framework.newim.server.mq;

import com.acmedcare.framework.newim.server.Context;
import com.acmedcare.framework.newim.server.replica.NodeReplicaInstance;
import com.acmedcare.framework.newim.server.replica.NodeReplicaException;
import com.acmedcare.framework.newim.server.replica.NodeReplicaProperties.ReplicaProperties;
import com.acmedcare.framework.newim.server.replica.NodeReplicaService;
import com.google.common.collect.Lists;
import java.util.List;
import java.util.Set;

/**
 * DefaultMQReplicaService
 *
 * @author <a href="mailto:iskp.me@gmail.com">Elve.Xu</a>
 * @version ${project.version} - 2018-12-25.
 */
public class DefaultMQReplicaService implements NodeReplicaService {

  private MQContext context;
  /**
   * return current context
   *
   * @return context
   */
  @Override
  public Context context() {
    return context;
  }

  /**
   * Get Node Replica List ,This method will be invoked schedule period
   *
   * @return a list of instance {@link NodeReplicaInstance}
   * @throws NodeReplicaException exception
   * @see ReplicaProperties#getInstancesRefreshPeriod() set period time
   */
  @Override
  public List<NodeReplicaInstance> loadNodeInstances() throws NodeReplicaException {

    Set<String> replicaAddresses = context.getReplicas();
    List<NodeReplicaInstance> instances = Lists.newArrayList();
    for (String replicaAddress : replicaAddresses) {
      // remove self
      instances.add(NodeReplicaInstance.builder().nodeAddress(replicaAddress).build());
    }
    return instances;
  }

  void setParentContext(MQContext context) {
    this.context = context;
  }
}
