package com.acmedcare.framework.newim.server.replica;

import com.acmedcare.framework.newim.server.replica.spring.context.ReplicaConnectorFactoryBeanDefinitionRegistryPostProcessor;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

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

  @Bean
  public ReplicaConnectorFactoryBeanDefinitionRegistryPostProcessor processor() {
    return new ReplicaConnectorFactoryBeanDefinitionRegistryPostProcessor();
  }
}
