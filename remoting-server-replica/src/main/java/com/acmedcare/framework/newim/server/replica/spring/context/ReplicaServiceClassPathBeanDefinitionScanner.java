package com.acmedcare.framework.newim.server.replica.spring.context;

import static org.springframework.context.annotation.AnnotationConfigUtils.registerAnnotationConfigProcessors;

import java.util.Set;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.beans.factory.config.BeanDefinitionHolder;
import org.springframework.beans.factory.support.BeanDefinitionRegistry;
import org.springframework.context.annotation.ClassPathBeanDefinitionScanner;
import org.springframework.core.env.Environment;
import org.springframework.core.io.ResourceLoader;

/**
 * ReplicaServiceClassPathBeanDefinitionScanner
 *
 * @author <a href="mailto:iskp.me@gmail.com">Elve.Xu</a>
 * @version ${project.version} - 2018-12-27.
 */
@Deprecated
public class ReplicaServiceClassPathBeanDefinitionScanner extends ClassPathBeanDefinitionScanner {

  public ReplicaServiceClassPathBeanDefinitionScanner(
      BeanDefinitionRegistry registry,
      boolean useDefaultFilters,
      Environment environment,
      ResourceLoader resourceLoader) {
    super(registry, useDefaultFilters);

    setEnvironment(environment);

    setResourceLoader(resourceLoader);

    registerAnnotationConfigProcessors(registry);
  }

  public ReplicaServiceClassPathBeanDefinitionScanner(
      BeanDefinitionRegistry registry, Environment environment, ResourceLoader resourceLoader) {
    this(registry, false, environment, resourceLoader);
  }

  @Override
  protected Set<BeanDefinitionHolder> doScan(String... basePackages) {
    return super.doScan(basePackages);
  }

  @Override
  protected boolean checkCandidate(String beanName, BeanDefinition beanDefinition)
      throws IllegalStateException {
    return super.checkCandidate(beanName, beanDefinition);
  }
}
