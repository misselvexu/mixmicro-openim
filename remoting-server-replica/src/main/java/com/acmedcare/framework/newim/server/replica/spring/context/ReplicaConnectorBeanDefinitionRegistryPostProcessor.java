package com.acmedcare.framework.newim.server.replica.spring.context;

import com.acmedcare.framework.newim.InstanceType;
import com.acmedcare.framework.newim.server.replica.NodeReplicaConnector;
import com.acmedcare.framework.newim.server.replica.NodeReplicaProperties;
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
 * ReplicaConnectorBeanDefinitionRegistryPostProcessor
 *
 * @author <a href="mailto:iskp.me@gmail.com">Elve.Xu</a>
 * @version ${project.version} - 2018-12-26.
 */
public class ReplicaConnectorBeanDefinitionRegistryPostProcessor
    implements BeanDefinitionRegistryPostProcessor,
        EnvironmentAware,
        ResourceLoaderAware,
        BeanClassLoaderAware {

  private static final String SEPARATOR = ":";
  private static final String DEFAULT_CONFIG_PROPERTIES_NAME = "replicaProperties";
  private final NodeReplicaProperties nodeReplicaProperties;
  private final Logger logger = LoggerFactory.getLogger(getClass());

  private Environment environment;

  private ResourceLoader resourceLoader;

  private ClassLoader classLoader;

  public ReplicaConnectorBeanDefinitionRegistryPostProcessor(
      NodeReplicaProperties nodeReplicaProperties) {
    this.nodeReplicaProperties = nodeReplicaProperties;
  }

  @Override
  public void setBeanClassLoader(ClassLoader classLoader) {
    this.classLoader = classLoader;
  }

  @Override
  public void postProcessBeanDefinitionRegistry(BeanDefinitionRegistry registry)
      throws BeansException {

    if (nodeReplicaProperties != null
        && nodeReplicaProperties.getReplicas() != null
        && !nodeReplicaProperties.getReplicas().isEmpty()) {

      nodeReplicaProperties
          .getReplicas()
          .forEach(
              (instanceType, replicaProperties) -> {
                if (!replicaProperties.isEnabled()) {
                  return;
                }
                BeanDefinitionBuilder builder =
                    BeanDefinitionBuilder.rootBeanDefinition(NodeReplicaConnector.class);
                AbstractBeanDefinition beanDefinition = builder.getBeanDefinition();
                beanDefinition.setAttribute(DEFAULT_CONFIG_PROPERTIES_NAME, replicaProperties);
                beanDefinition.setScope(ConfigurableBeanFactory.SCOPE_PROTOTYPE); // PROTOTYPE

                String beanName =
                    generateReplicaConnectorBeanName(
                        environment, instanceType, NodeReplicaConnector.class);

                registry.registerBeanDefinition(beanName, beanDefinition);
              });
    }
  }

  private String generateReplicaConnectorBeanName(
      Environment environment, InstanceType instanceType, Class<?> clazz) {
    StringBuilder builder = new StringBuilder();
    builder.append(instanceType.name()).append(SEPARATOR).append(clazz.getSimpleName());
    return environment.resolvePlaceholders(builder.toString());
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
