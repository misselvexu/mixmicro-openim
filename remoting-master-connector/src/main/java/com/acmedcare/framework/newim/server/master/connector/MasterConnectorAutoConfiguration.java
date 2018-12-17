package com.acmedcare.framework.newim.server.master.connector;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;

/**
 * MasterConnectorAutoConfiguration
 *
 * @author <a href="mailto:iskp.me@gmail.com">Elve.Xu</a>
 * @version ${project.version} - 2018-12-17.
 */
@Configuration
public class MasterConnectorAutoConfiguration {

  @ConditionalOnProperty(prefix = "remoting.server.master", value = "enabled", havingValue = "true")
  @Bean(initMethod = "init", destroyMethod = "destroy")
  @Primary
  public MasterConnector masterConnector(MasterConnectorProperties properties) {
    return new MasterConnector(properties);
  }

  @Configuration
  @ConditionalOnProperty(prefix = "remoting.server.master", value = "enabled", havingValue = "true")
  @EnableConfigurationProperties(MasterConnectorProperties.class)
  public static class MasterPropertiesConfiguration {}
}
