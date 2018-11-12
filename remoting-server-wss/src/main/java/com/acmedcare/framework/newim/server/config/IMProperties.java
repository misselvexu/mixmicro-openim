package com.acmedcare.framework.newim.server.config;

import com.google.common.collect.Lists;
import java.util.List;
import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;

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
public class IMProperties {

  /** IM Server Port, Default: 23111 */
  private int port = 23111;

  /** 通讯节点端口 */
  private int clusterPort = 33111;

  /** Master Server Nodes List */
  private List<String> masterNodes = Lists.newArrayList();

  /** MasterServer 心跳间隔(s) */
  private long masterHeartbeat = 20;

  /** 空闲时间(s) */
  private long masterClientIdleTime = 60;
}
