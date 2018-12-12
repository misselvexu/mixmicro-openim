package com.acmedcare.framework.newim.server.runner.listener;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicBoolean;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.WebApplicationType;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.ApplicationListener;

/**
 * Awaiting NonWebApplication Listener
 *
 * @author <a href="mailto:iskp.me@gmail.com">Elve.Xu</a>
 * @version ${project.version} - 2018-12-12.
 */
public class AwaitingNonWebApplicationListener
    implements ApplicationListener<ApplicationReadyEvent> {
  private static final Logger logger =
      LoggerFactory.getLogger(AwaitingNonWebApplicationListener.class);

  private static final ExecutorService executorService = Executors.newSingleThreadExecutor();

  private static final AtomicBoolean shutdownHookRegistered = new AtomicBoolean(false);

  private static final AtomicBoolean awaited = new AtomicBoolean(false);

  @Override
  public void onApplicationEvent(ApplicationReadyEvent event) {

    final SpringApplication springApplication = event.getSpringApplication();

    if (!WebApplicationType.NONE.equals(springApplication.getWebApplicationType())) {
      return;
    }

    executorService.execute(
        new Runnable() {
          @Override
          public void run() {

            synchronized (springApplication) {
              if (logger.isInfoEnabled()) {
                logger.info(" [Remoting] Current Spring Boot Application is await...");
              }
              while (!awaited.get()) {
                try {
                  springApplication.wait();
                } catch (InterruptedException e) {
                  Thread.currentThread().interrupt();
                }
              }
            }
          }
        });

    // register ShutdownHook
    if (shutdownHookRegistered.compareAndSet(false, true)) {
      registerShutdownHook(
          new Thread(
              new Runnable() {
                @Override
                public void run() {
                  synchronized (springApplication) {
                    if (awaited.compareAndSet(false, true)) {
                      springApplication.notifyAll();
                      if (logger.isInfoEnabled()) {
                        logger.info(
                            " [Remoting] Current Spring Boot Application is about to shutdown...");
                      }
                      // Shutdown executorService
                      executorService.shutdown();
                    }
                  }
                }
              }));
    }
  }

  private void registerShutdownHook(Thread thread) {
    Runtime.getRuntime().addShutdownHook(thread);
  }
}
