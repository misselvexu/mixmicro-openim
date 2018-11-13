package com.acmedcare.framework.newim.server;

import com.acmedcare.framework.boot.snowflake.EnableSnowflake;
import com.acmedcare.framework.boot.snowflake.Snowflake;
import com.acmedcare.framework.boot.web.socket.standard.ServerEndpointExporter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.WebApplicationType;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;

/**
 * Remoting Wss Server Application
 *
 * @author <a href="mailto:iskp.me@gmail.com">Elve.Xu</a>
 * @version ${project.version} - 08/11/2018.
 */
@SpringBootApplication
@ComponentScan("com.acmedcare.framework.newim")
public class RemotingWssServer {

  /** Spring Context Instance of {@link org.springframework.context.ApplicationContext} */
  private static ConfigurableApplicationContext context;

  public static void main(String[] args) {
    context =
        new SpringApplicationBuilder()
            .sources(RemotingWssServer.class)
            .web(WebApplicationType.SERVLET)
            .run(args); //
  }

  @Configuration
  public static class WebSocketConfiguration {
    @Bean
    public ServerEndpointExporter serverEndpointExporter() {
      return new ServerEndpointExporter();
    }
  }

  @Configuration
  @EnableSnowflake(
      dataCenterId = "${snowflake.data-center-id:1}",
      workerId = "${snowflake.worker-id:1}")
  public static class Ids {
    public static Snowflake snowflake;
    @Autowired
    public void setSnowflake(Snowflake snowflake) {
      Ids.snowflake = snowflake;
    }
  }
}
