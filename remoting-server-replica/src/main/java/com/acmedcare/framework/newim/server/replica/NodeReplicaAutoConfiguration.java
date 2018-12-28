package com.acmedcare.framework.newim.server.replica;

import static java.util.Collections.emptySet;

import com.acmedcare.framework.newim.server.replica.spring.context.ReplicaConnectorFactoryBeanDefinitionRegistryPostProcessor;
import java.util.Set;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.Environment;

/**
 * NodeReplicaAutoConfiguration
 *
 * @author <a href="mailto:iskp.me@gmail.com">Elve.Xu</a>
 * @version ${project.version} - 2018-12-25.
 */
@Configuration
@ConditionalOnBean(NodeReplicaService.class)
@EnableConfigurationProperties(NodeReplicaProperties.class)
public class NodeReplicaAutoConfiguration {

  @Deprecated
  private static final String BASE_PACKAGES_PROPERTIES_NAME =
      NodeReplicaProperties.NODE_REPLICA_PREFIX + "." + "base-packages";

  @Bean
  public ReplicaConnectorFactoryBeanDefinitionRegistryPostProcessor processor(
      Environment environment) {
    Set<String> packagesToScan = environment.getProperty(BASE_PACKAGES_PROPERTIES_NAME, Set.class, emptySet());
    return new ReplicaConnectorFactoryBeanDefinitionRegistryPostProcessor(packagesToScan);
  }
}
