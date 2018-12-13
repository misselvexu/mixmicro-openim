package com.acmedcare.framework.newim.server.runner;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * Remoting Server Runner
 *
 * @author <a href="mailto:iskp.me@gmail.com">Elve.Xu</a>
 * @version ${project.version} - 2018-12-12.
 */
@SpringBootApplication
public class RemotingRunner {

  public static void main(String[] args) {
    SpringApplication.run(RemotingRunner.class, args);
  }
}
