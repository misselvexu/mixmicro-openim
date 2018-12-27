package com.acmedcare.framework.newim.server.replica;

import com.acmedcare.framework.newim.InstanceType;
import com.acmedcare.framework.newim.server.replica.NodeReplicaProperties.ReplicaProperties;
import com.acmedcare.framework.newim.spi.util.Assert;
import com.google.common.collect.Maps;
import java.util.Map;
import lombok.Builder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.beans.factory.BeanFactoryAware;
import org.springframework.beans.factory.InitializingBean;

/**
 * NodeReplicaConnectorFactory
 *
 * @author <a href="mailto:iskp.me@gmail.com">Elve.Xu</a>
 * @version ${project.version} - 2018-12-26.
 */
public class NodeReplicaConnectorFactory implements BeanFactoryAware, InitializingBean {

  private static final Logger logger = LoggerFactory.getLogger(NodeReplicaConnectorFactory.class);
  /**
   * {@link NodeReplicaConnector} Instances Cache
   *
   * @see InstanceType
   */
  private static Map<InstanceType, NodeReplicaConnector> nodeReplicaConnectors =
      Maps.newConcurrentMap();

  private NodeReplicaProperties nodeReplicaProperties;

  /** Bean Factory Instance */
  private BeanFactory beanFactory;

  @Override
  public void setBeanFactory(BeanFactory beanFactory) throws BeansException {
    this.beanFactory = beanFactory;
  }

  @Override
  public void afterPropertiesSet() throws Exception {
    this.nodeReplicaProperties = beanFactory.getBean(NodeReplicaProperties.class);
    logger.info(
        "[REPLICA-FACTORY] found nodeReplicaProperties instance:{} from bean factory",
        this.nodeReplicaProperties);

    Assert.notNull(this.nodeReplicaProperties, "");

    // init
    this.nodeReplicaProperties
        .getReplicas()
        .forEach(
            (type, replicaProperties) -> {
              InstanceType instanceType = null;
              try {
                instanceType = InstanceType.valueOf(type);
              } catch (Exception e) {
                logger.warn("[REPLICA-FACTORY] invalid replica key type :{} ", type);
              }

              Assert.notNull(instanceType, "[REPLICA-FACTORY] instanceType not be null.");

              NodeReplicaConnector nodeReplicaConnector =
                  NodeReplicaConnector.builder().replicaProperties(replicaProperties).build();

              // TODO startup

              nodeReplicaConnectors.put(instanceType, nodeReplicaConnector);
            });
  }

  /** {@link NodeReplicaConnector }Bean Defined */
  public static class NodeReplicaConnector {

    private final ReplicaProperties replicaProperties;

    @Builder
    public NodeReplicaConnector(ReplicaProperties replicaProperties) {
      this.replicaProperties = replicaProperties;
    }
  }
}
