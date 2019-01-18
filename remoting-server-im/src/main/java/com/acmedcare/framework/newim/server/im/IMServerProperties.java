package com.acmedcare.framework.newim.server.im;

import com.acmedcare.framework.newim.server.Server.ServerProperties;
import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * IM Server Properties
 *
 * @author <a href="mailto:iskp.me@gmail.com">Elve.Xu</a>
 * @version ${project.version} - 2018-12-13.
 */
@Getter
@Setter
@ConfigurationProperties(prefix = "remoting.server.im")
public class IMServerProperties extends ServerProperties {

}
