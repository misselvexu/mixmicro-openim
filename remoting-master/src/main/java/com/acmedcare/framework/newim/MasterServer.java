package com.acmedcare.framework.newim;

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
public class MasterServer {

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
            .sources(MasterServer.class)
            .web(WebApplicationType.SERVLET)
            .run(args);
  }
}
