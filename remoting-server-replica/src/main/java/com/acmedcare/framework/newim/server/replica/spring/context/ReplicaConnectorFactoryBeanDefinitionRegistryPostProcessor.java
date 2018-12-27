package com.acmedcare.framework.newim.server.replica.spring.context;

import com.acmedcare.framework.newim.server.replica.NodeReplicaConnectorFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.BeanClassLoaderAware;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;
import org.springframework.beans.factory.support.AbstractBeanDefinition;
import org.springframework.beans.factory.support.BeanDefinitionBuilder;
import org.springframework.beans.factory.support.BeanDefinitionRegistry;
import org.springframework.beans.factory.support.BeanDefinitionRegistryPostProcessor;
import org.springframework.context.EnvironmentAware;
import org.springframework.context.ResourceLoaderAware;
import org.springframework.core.env.Environment;
import org.springframework.core.io.ResourceLoader;

/**
 * ReplicaConnectorFactoryBeanDefinitionRegistryPostProcessor
 *
 * @author <a href="mailto:iskp.me@gmail.com">Elve.Xu</a>
 * @version ${project.version} - 2018-12-26.
 */
public class ReplicaConnectorFactoryBeanDefinitionRegistryPostProcessor
    implements BeanDefinitionRegistryPostProcessor,
        EnvironmentAware,
        ResourceLoaderAware,
        BeanClassLoaderAware {

  private final Logger logger = LoggerFactory.getLogger(getClass());
  private Environment environment;
  private ResourceLoader resourceLoader;

  private ClassLoader classLoader;

  @Override
  public void setBeanClassLoader(ClassLoader classLoader) {
    this.classLoader = classLoader;
  }

  @Override
  public void postProcessBeanDefinitionRegistry(BeanDefinitionRegistry registry)
      throws BeansException {

    BeanDefinitionBuilder builder =
        BeanDefinitionBuilder.rootBeanDefinition(NodeReplicaConnectorFactory.class);
    AbstractBeanDefinition beanDefinition = builder.getBeanDefinition();
    beanDefinition.setScope(ConfigurableBeanFactory.SCOPE_SINGLETON); // PROTOTYPE

    registry.registerBeanDefinition(
        NodeReplicaConnectorFactory.class.getSimpleName(), beanDefinition);

    logger.info(
        "[REPLICA-REGISTER] Class: {} ,Bean:{} is register-ed",
        NodeReplicaConnectorFactory.class,
        NodeReplicaConnectorFactory.class.getSimpleName());
  }

  @Override
  public void postProcessBeanFactory(ConfigurableListableBeanFactory beanFactory)
      throws BeansException {
    // ignore
  }

  @Override
  public void setEnvironment(Environment environment) {
    this.environment = environment;
  }

  @Override
  public void setResourceLoader(ResourceLoader resourceLoader) {
    this.resourceLoader = resourceLoader;
  }
}
