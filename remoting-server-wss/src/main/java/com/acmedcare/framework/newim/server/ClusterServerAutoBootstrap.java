package com.acmedcare.framework.newim.server;

import com.acmedcare.framework.newim.server.config.IMProperties;
import com.acmedcare.framework.newim.server.core.IMSession;
import com.acmedcare.framework.newim.server.core.NewIMServerBootstrap;
import com.acmedcare.framework.newim.server.core.connector.ClusterReplicaConnector;
import com.acmedcare.framework.newim.server.core.connector.MasterConnector;
import com.acmedcare.framework.newim.server.service.GroupService;
import com.acmedcare.framework.newim.server.service.MessageService;
import com.acmedcare.framework.newim.server.service.RemotingAuthService;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.EnableLoadTimeWeaving;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;

/**
 * Cluster Server Auto Startup Configuration
 *
 * @author <a href="mailto:iskp.me@gmail.com">Elve.Xu</a>
 * @version ${project.version} - 19/11/2018.
 */
@Configuration
public class ClusterServerAutoBootstrap {

  @Bean
  @Order(Ordered.HIGHEST_PRECEDENCE)
  public IMSession imSession() {
    return new IMSession();
  }

  @Bean
  public NewIMServerBootstrap mainServer(
      IMProperties imProperties,
      RemotingAuthService remotingAuthService,
      MessageService messageService,
      GroupService groupService,
      IMSession imSession) {
    return new NewIMServerBootstrap(
        imProperties, remotingAuthService, messageService, groupService, imSession);
  }

  @Bean
  public ClusterReplicaConnector clusterReplicaConnector(
      IMProperties imProperties, IMSession imSession) {
    return new ClusterReplicaConnector(imProperties, imSession);
  }

  @Bean
  public MasterConnector masterConnector(IMProperties imProperties, IMSession imSession) {
    return new MasterConnector(imProperties, imSession);
  }
}
