package com.acmedcare.framework.newim.test;

import com.acmedcare.framework.newim.InstanceType;
import com.acmedcare.framework.newim.server.Context;
import com.acmedcare.framework.newim.server.replica.NodeReplicaBeanFactory;
import com.acmedcare.framework.newim.server.replica.NodeReplicaProperties;
import com.acmedcare.framework.newim.server.replica.NodeReplicaService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.web.bind.annotation.RestController;

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

  @Bean
  DefaultNodeReplicaService2 defaultNodeReplicaService2(
      NodeReplicaProperties nodeReplicaProperties) {
    return new DefaultNodeReplicaService2(nodeReplicaProperties);
  }

  @RestController
  protected static class Controller {

    private final NodeReplicaBeanFactory nodeReplicaBeanFactory;

    @Autowired
    public Controller(NodeReplicaBeanFactory nodeReplicaBeanFactory) {
      this.nodeReplicaBeanFactory = nodeReplicaBeanFactory;
    }
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

    @Override
    public InstanceType type() {
      return InstanceType.MQ_SERVER;
    }
  }

  public static class DefaultNodeReplicaService2 implements NodeReplicaService {

    private NodeReplicaProperties nodeReplicaProperties;

    public DefaultNodeReplicaService2(NodeReplicaProperties nodeReplicaProperties) {
      this.nodeReplicaProperties = nodeReplicaProperties;
    }

    @Override
    public Context context() {
      return null;
    }

    @Override
    public InstanceType type() {
      return InstanceType.DEFAULT;
    }
  }
}
