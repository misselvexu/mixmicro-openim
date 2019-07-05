package com.acmedcare.framework.newim;

import com.acmedcare.framework.boot.snowflake.EnableSnowflake;
import org.springframework.boot.WebApplicationType;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.context.ConfigurableApplicationContext;

/**
 * Master Server Of NewIM System
 *
 * @author <a href="mailto:iskp.me@gmail.com">Elve.Xu</a>
 * @version ${project.version} - 05/11/2018.
 */
@SpringBootApplication
@EnableSnowflake(workerId = "${snowflake.work-id:1}")
public class MasterBootstrap {

  /** Spring Context Instance of {@link org.springframework.context.ApplicationContext} */
  private static ConfigurableApplicationContext context;

  /**
   * Application Main Method
   *
   * @param args boot args
   */
  public static void main(String[] args) {
    context =
        new SpringApplicationBuilder()
            .sources(MasterBootstrap.class)
            .properties("--spring.profiles.active=default")
            .web(WebApplicationType.SERVLET)
            .run(args);
  }
}
