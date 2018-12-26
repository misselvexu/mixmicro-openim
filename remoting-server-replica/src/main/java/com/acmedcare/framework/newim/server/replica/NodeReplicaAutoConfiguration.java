package com.acmedcare.framework.newim.server.replica;

import com.acmedcare.framework.newim.server.replica.spring.context.ReplicaConnectorBeanDefinitionRegistryPostProcessor;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.context.properties.source.ConfigurationPropertySources;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * NodeReplicaAutoConfiguration
 *
 * @author <a href="mailto:iskp.me@gmail.com">Elve.Xu</a>
 * @version ${project.version} - 2018-12-25.
 */
@Configuration
@ConditionalOnBean({NodeReplicaService.class})
public class NodeReplicaAutoConfiguration {

  @Bean
  @ConditionalOnClass(ConfigurationPropertySources.class)
  @ConditionalOnBean({NodeReplicaProperties.class})
  public ReplicaConnectorBeanDefinitionRegistryPostProcessor processor(
      NodeReplicaProperties properties) {
    return new ReplicaConnectorBeanDefinitionRegistryPostProcessor(properties);
  }

  @Configuration
  @EnableConfigurationProperties(NodeReplicaProperties.class)
  public static class NodeReplicaPropertiesConfiguration {}
}
