package com.acmedcare.framework.newim;

import com.acmedcare.framework.boot.snowflake.EnableSnowflake;
import com.acmedcare.framework.newim.server.Context;
import com.acmedcare.framework.newim.server.replica.NodeReplicaProperties;
import com.acmedcare.framework.newim.server.replica.NodeReplicaService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;

/**
 * Spring Boot Test Application
 *
 * @author <a href="mailto:iskp.me@gmail.com">Elve.Xu</a>
 * @version ${project.version} - 22/11/2018.
 */
@SpringBootApplication
@EnableSnowflake(workerId = "1")
public class TestApplication {

  public static void main(String[] args) {
    SpringApplication.run(TestApplication.class, args);
  }

  @Bean
  DefaultNodeReplicaService defaultNodeReplicaService(){
    return new DefaultNodeReplicaService();
  }

  public static class DefaultNodeReplicaService implements NodeReplicaService {

    @Autowired private NodeReplicaProperties nodeReplicaProperties;

    @Override
    public Context context() {
      return null;
    }

    @Override
    public InstanceType type() {
      return InstanceType.MQ_SERVER;
    }
  }
}
