package com.acmedcare.framework.newim.master;

import com.acmedcare.framework.kits.StringUtils;
import lombok.Getter;
import lombok.Setter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.EnvironmentAware;
import org.springframework.context.annotation.PropertySource;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Component;

import java.io.Serializable;

import static com.acmedcare.framework.newim.master.common.SystemKits.LOCAL_IP;

/**
 * Master Config
 *
 * @author <a href="mailto:iskp.me@gmail.com">Elve.Xu</a>
 * @version ${project.version} - 05/11/2018.
 */
@Getter
@Setter
@Component
@ConfigurationProperties(prefix = "master", ignoreInvalidFields = true)
@PropertySource(
    value = {
      "classpath:master-default.properties",
      "classpath:master-${spring.profiles.active:default}.properties"
    })
public class MasterConfig implements Serializable, EnvironmentAware, InitializingBean {

  private static final Logger log = LoggerFactory.getLogger(MasterConfig.class);

  private static final long serialVersionUID = -6963594195541525813L;

  // =============Master Config Start=================================

  /** Master Server <code>port</code> Properties */
  private int port = 13111;

  private String host;

  /** 分区 Zone */
  private String zone = "default";

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

  @Override
  public String toString() {
    return "MasterConfig{" + "port=" + port + ", host='" + host + '\'' + '}';
  }

  @Override
  public void afterPropertiesSet() throws Exception {
    if (StringUtils.isBlank(this.host)) {
      this.host = LOCAL_IP;
      if (StringUtils.isBlank(this.host)) {
        this.host = environment.getProperty("server.address");
      }

      log.info("master node ip :{}", this.host);
    }
  }
}
