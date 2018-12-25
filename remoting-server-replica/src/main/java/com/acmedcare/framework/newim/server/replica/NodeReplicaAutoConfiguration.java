package com.acmedcare.framework.newim.server.replica;

import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
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

  @Configuration
  @ConditionalOnProperty(
      prefix = "remoting.server.replica",
      value = "enabled",
      havingValue = "true")
  @EnableConfigurationProperties(NodeReplicaProperties.class)
  public static class NodeReplicaPropertiesConfiguration {}
}
