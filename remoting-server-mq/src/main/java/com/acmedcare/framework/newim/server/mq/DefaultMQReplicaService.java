package com.acmedcare.framework.newim.server.mq;

import com.acmedcare.framework.newim.InstanceType;
import com.acmedcare.framework.newim.Message;
import com.acmedcare.framework.newim.Message.MQMessage;
import com.acmedcare.framework.newim.RemotingEvent;
import com.acmedcare.framework.newim.server.Context;
import com.acmedcare.framework.newim.server.mq.event.AcmedcareEvent;
import com.acmedcare.framework.newim.server.mq.service.MQService;
import com.acmedcare.framework.newim.server.replica.NodeReplicaException;
import com.acmedcare.framework.newim.server.replica.NodeReplicaInstance;
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
  private MQService mqService;

  public DefaultMQReplicaService() {}

  public DefaultMQReplicaService(MQService mqService) {
    this.mqService = mqService;
  }

  /**
   * return current context
   *
   * @return context
   */
  @Override
  public Context context() {
    return context;
  }

  @Override
  public InstanceType type() {
    return InstanceType.MQ_SERVER;
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
      instances.add(NodeReplicaInstance.builder().nodeAddress(replicaAddress).build());
    }
    return instances;
  }

  void setParentContext(MQContext context) {
    this.context = context;
  }

  @Override
  public void onReceivedMessage(Message message) {
    if (message instanceof MQMessage) {
      MQMessage mqMessage = (MQMessage) message;
      mqService.doBroadcastTopicMessage(context, mqMessage);
    }
  }

  @Override
  public void onReceivedEvent(RemotingEvent remotingEvent) {
    logger.info("Rvd Replica Event : {}", remotingEvent.getEvent());
    try {
      AcmedcareEvent.BizEvent bizEvent = AcmedcareEvent.BizEvent.valueOf(remotingEvent.getEvent());

      context.broadcastEvent(
          new AcmedcareEvent() {
            @Override
            public Event eventType() {
              return bizEvent;
            }

            @Override
            public byte[] data() {
              return remotingEvent.getPayload();
            }
          });

    } catch (Exception e) {
      logger.warn("MQ replica service process failed", e);
    }
  }
}
