package com.acmedcare.framework.newim.server.replica.spring.context;

import static org.springframework.beans.factory.support.AbstractBeanDefinition.AUTOWIRE_BY_TYPE;

import com.acmedcare.framework.newim.server.replica.NodeReplicaBeanFactory;
import java.util.Set;
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
  private final Set<String> packagesToScan;
  private Environment environment;
  private ResourceLoader resourceLoader;
  private ClassLoader classLoader;

  public ReplicaConnectorFactoryBeanDefinitionRegistryPostProcessor(Set<String> packagesToScan) {
    this.packagesToScan = packagesToScan;
  }

  @Override
  public void setBeanClassLoader(ClassLoader classLoader) {
    this.classLoader = classLoader;
  }

  @Override
  public void postProcessBeanDefinitionRegistry(BeanDefinitionRegistry registry)
      throws BeansException {

    BeanDefinitionBuilder builder =
        BeanDefinitionBuilder.rootBeanDefinition(NodeReplicaBeanFactory.class);
    AbstractBeanDefinition beanDefinition = builder.getBeanDefinition();
    beanDefinition.setScope(ConfigurableBeanFactory.SCOPE_SINGLETON);
    beanDefinition.setAutowireMode(AUTOWIRE_BY_TYPE);
    registry.registerBeanDefinition("nodeReplicaBeanFactory", beanDefinition);

    logger.info(
        "[REPLICA-REGISTER] Class: {} ,Bean:{} is register-ed",
        NodeReplicaBeanFactory.class,
        NodeReplicaBeanFactory.class.getSimpleName());

    //    ReplicaServiceClassPathBeanDefinitionScanner scanner =
    //        new ReplicaServiceClassPathBeanDefinitionScanner(registry, environment,
    // resourceLoader);
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
