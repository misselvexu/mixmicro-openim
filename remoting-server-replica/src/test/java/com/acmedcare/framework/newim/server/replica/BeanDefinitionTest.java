package com.acmedcare.framework.newim.server.replica;

import com.acmedcare.framework.newim.server.replica.NodeReplicaProperties.ReplicaProperties;
import org.junit.Test;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.beans.factory.support.AbstractBeanDefinition;
import org.springframework.beans.factory.support.BeanDefinitionBuilder;

/**
 * BeanDefinitionTest
 *
 * @author <a href="mailto:iskp.me@gmail.com">Elve.Xu</a>
 * @version ${project.version} - 2018-12-26.
 */
public class BeanDefinitionTest {

  @Test
  public void testBeanDefinitionBuilder() {

    BeanDefinitionBuilder builder =
        BeanDefinitionBuilder.rootBeanDefinition(NodeReplicaConnectorFactory.class);

    AbstractBeanDefinition beanDefinition = builder.getBeanDefinition();

    beanDefinition.setAttribute("replicaProperties", new ReplicaProperties());
    beanDefinition.setScope(ConfigurableBeanFactory.SCOPE_PROTOTYPE); // PROTOTYPE



    System.out.println();
  }
}
