package com.acmedcare.framework.newim;

import com.acmedcare.framework.boot.snowflake.EnableSnowflake;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * Spring Boot Test Application
 *
 * @author <a href="mailto:iskp.me@gmail.com">Elve.Xu</a>
 * @version ${project.version} - 22/11/2018.
 */
@SpringBootApplication
@EnableSnowflake(dataCenterId = "1", workerId = "1")
public class TestApplication {

  public static void main(String[] args) {
    SpringApplication.run(TestApplication.class, args);
  }
}
