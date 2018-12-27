package com.acmedcare.framework.newim.test;

import com.acmedcare.framework.newim.server.Context;
import com.acmedcare.framework.newim.server.replica.NodeReplicaProperties;
import com.acmedcare.framework.newim.server.replica.NodeReplicaService;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;

/**
 * Application
 *
 * @author <a href="mailto:iskp.me@gmail.com">Elve.Xu</a>
 * @version ${project.version} - 2018-12-27.
 */
@SpringBootApplication
public class Application {
  public static void main(String[] args) {
    SpringApplication.run(Application.class, args);
  }

  @Bean
  DefaultNodeReplicaService defaultNodeReplicaService(NodeReplicaProperties nodeReplicaProperties) {
    return new DefaultNodeReplicaService(nodeReplicaProperties);
  }

  public static class DefaultNodeReplicaService implements NodeReplicaService {

    private NodeReplicaProperties nodeReplicaProperties;

    public DefaultNodeReplicaService(NodeReplicaProperties nodeReplicaProperties) {
      this.nodeReplicaProperties = nodeReplicaProperties;
    }

    @Override
    public Context context() {
      return null;
    }
  }
}
