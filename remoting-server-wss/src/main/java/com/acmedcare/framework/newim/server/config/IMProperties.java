package com.acmedcare.framework.newim.server.config;

import static com.acmedcare.framework.newim.server.config.WssConstants.WSS_PORT_KEY;

import com.acmedcare.framework.newim.protocol.request.ClusterRegisterBody.WssInstance;
import com.google.common.collect.Lists;
import java.util.List;
import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.EnvironmentAware;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;
import org.springframework.core.env.Environment;

/**
 * IM Server Config
 *
 * @author <a href="mailto:iskp.me@gmail.com">Elve.Xu</a>
 * @version ${project.version} - 09/11/2018.
 */
@Getter
@Setter
@Configuration
@ConfigurationProperties(prefix = "im")
@PropertySource(value = "classpath:im.properties")
public class IMProperties implements EnvironmentAware {

  /** IM Server Port, Default: 23111 */
  private int port = 23111;

  private String host = "127.0.0.1";

  private int clusterPort = 33111;
  private long clusterHeartbeat;

  /** Master Server Nodes List */
  private List<String> masterNodes = Lists.newArrayList();

  /** MasterServer 心跳间隔(s) */
  private long masterHeartbeat = 20;

  /** 空闲时间(s) */
  private long masterClientIdleTime = 60;

  private Environment environment;

  /**
   * Set the {@code Environment} that this component runs in.
   *
   * @param environment env
   */
  @Override
  public void setEnvironment(Environment environment) {
    this.environment = environment;
  }

  /**
   * Return the property value associated with the given key, or {@code defaultValue} if the key
   * cannot be resolved.
   *
   * @param key the property name to resolve
   * @param targetType the expected type of the property value
   * @param defaultValue the default value to return if no value is found
   * @see Environment#getRequiredProperty(String, Class)
   */
  public <T> T getProperties(String key, Class<T> targetType, T defaultValue) {
    if (environment.containsProperty(key)) {
      return environment.getProperty(key, targetType, defaultValue);
    }
    return null;
  }

  public List<WssInstance> loadWssEndpoints() {
    List<WssInstance> instances = Lists.newArrayList();

    String[] endpoints = getProperties(WssConstants.WSS_ENDPOINTS, String.class, "").split(",");
    for (String endpoint : endpoints) {
      WssInstance wssInstance = new WssInstance();
      wssInstance.setWssName(endpoint);
      wssInstance.setWssHost(this.host);
      wssInstance.setWssPort(
          getProperties(String.format(WSS_PORT_KEY, endpoint), Integer.class, 8888));
      instances.add(wssInstance);
    }
    return instances;
  }
}
