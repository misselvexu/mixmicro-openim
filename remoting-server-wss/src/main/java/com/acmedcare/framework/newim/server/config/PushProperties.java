package com.acmedcare.framework.newim.server.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;

/**
 * Push Server Config
 *
 * @author <a href="mailto:iskp.me@gmail.com">Elve.Xu</a>
 * @version ${project.version} - 09/11/2018.
 */
@Getter
@Setter
@Configuration
@ConfigurationProperties(prefix = "push")
@PropertySource(value = "classpath:push.properties")
public class PushProperties {

  /** Push Server Port, Default: 13733 */
  private int port = 13733;

  private long heartbeat = 10000;
}
