package com.acmedcare.framework.newim.master;

import java.io.Serializable;
import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.EnvironmentAware;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;
import org.springframework.core.env.Environment;

/**
 * Master Config
 *
 * @author <a href="mailto:iskp.me@gmail.com">Elve.Xu</a>
 * @version ${project.version} - 05/11/2018.
 */
@Getter
@Setter
@Configuration
@ConfigurationProperties(prefix = "master", ignoreInvalidFields = true)
@PropertySource(value = "classpath:master.properties")
public class MasterConfig implements Serializable, EnvironmentAware {

  private static final long serialVersionUID = -6963594195541525813L;

  // =============Master Config Start=================================

  /** Master Server <code>port</code> Properties */
  private int port = 13111;

  private String host = "127.0.0.1";

  /** Spring Application Context Environment */
  private Environment environment;

  // =============Master Config End  =================================

  /**
   * Set the {@code Environment} that this component runs in.
   *
   * @param environment env param
   */
  @Override
  public void setEnvironment(Environment environment) {
    this.environment = environment;
  }
}
