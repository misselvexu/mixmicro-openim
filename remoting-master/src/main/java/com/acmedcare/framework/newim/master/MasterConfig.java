package com.acmedcare.framework.newim.master;

import com.google.common.collect.Lists;
import java.io.Serializable;
import java.util.List;
import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.EnvironmentAware;
import org.springframework.context.annotation.Configuration;
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
@ConfigurationProperties(
    prefix = "master",
    ignoreInvalidFields = true,
    value = "classpath:master.properties")
public class MasterConfig implements Serializable, EnvironmentAware {

  private static final long serialVersionUID = -6963594195541525813L;

  // =============Master Config Start=================================

  /** Master Server <code>port</code> Properties */
  private int port = 13111;

  /**
   * Master Cluster Nodes List
   *
   * <p>ip:port
   */
  private List<Cluster> clusters = Lists.newArrayList();

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

  @Getter
  @Setter
  @Configuration
  @ConfigurationProperties(
      prefix = "master.cluster",
      ignoreInvalidFields = true,
      value = "classpath:master.properties")
  public static class Cluster implements Serializable {

    private static final long serialVersionUID = -602633671198547053L;

    /** master nodes list */
    private List<String> nodes = Lists.newArrayList();

    /**
     * Master clusters communicate heartbeat period
     *
     * <p>Unit: ts ,Default : 10s
     */
    private long heartbeat = 10000;
  }
}
