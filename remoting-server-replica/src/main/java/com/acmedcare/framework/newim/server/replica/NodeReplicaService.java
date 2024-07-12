package com.acmedcare.framework.newim.server.replica;

import com.acmedcare.framework.newim.InstanceType;
import com.acmedcare.framework.newim.Message;
import com.acmedcare.framework.newim.RemotingEvent;
import com.acmedcare.framework.newim.server.Context;
import com.google.common.collect.Lists;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * NodeReplicaService
 *
 * @author <a href="mailto:iskp.me@gmail.com">Elve.Xu</a>
 * @version ${project.version} - 2018-12-25.
 */
public interface NodeReplicaService {

  Logger logger = LoggerFactory.getLogger(NodeReplicaService.class);

  /**
   * return current context
   *
   * @return context
   */
  Context context();

  /**
   * Instance Type
   *
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
    // default implements
    return Lists.newArrayList();
  }

  /**
   * When Received Replica Forward Message Event, then invoke this method .
   *
   * @param message message instance
   */
  default void onReceivedMessage(Message message) {
    logger.info("Rvd Replica Message : {}", message.toString());
  }

  /**
   * When Received Replica Forward Event, then invoke this method .
   *
   * @param remotingEvent remoting event
   */
  default void onReceivedEvent(RemotingEvent remotingEvent) {
    logger.info("Rvd Replica Event : {}", remotingEvent.getEvent());
  }
}
