package com.acmedcare.microservices.im.core;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * Remoting Server Config
 *
 * @author <a href="mailto:iskp.me@gmail.com">Elve.Xu</a>
 * @version v1.0 - 06/07/2018.
 */
@Component
@ConfigurationProperties(prefix = "remoting")
public class ServerConfig {

  @Getter @Setter private int port;
}
