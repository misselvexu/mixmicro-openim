package com.acmedcare.framework.newim.server.runner.loader;

import java.util.concurrent.atomic.AtomicBoolean;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.ApplicationListener;

/**
 * RemotingServiceStartupListener
 *
 * @author <a href="mailto:iskp.me@gmail.com">Elve.Xu</a>
 * @version ${project.version} - 2018-12-14.
 */
public class RemotingServiceStartupListener implements ApplicationListener<ApplicationReadyEvent> {

  private static final Logger logger = LoggerFactory.getLogger(RemotingServiceStartupListener.class);
  private static volatile AtomicBoolean loaded = new AtomicBoolean(false);

  @Override
  public void onApplicationEvent(ApplicationReadyEvent event) {

    final BeanFactory beanFactory = event.getApplicationContext().getBeanFactory();
    if (loaded.compareAndSet(false, true)) {

      logger.info("======>>> ===============================================================");
      logger.info("[Remoting] Starting refresh server services factory ...");
      ServerServiceFactory.refresh(beanFactory);
      logger.info("[Remoting] server services factory load succeed.");

      ServerServiceFactory.instances()
          .forEach(
              server -> {
                try {
                  // startup server
                  server.startup();

                  // register shutdown hook
                  Runtime.getRuntime().addShutdownHook(new Thread(server::shutdown));
                } catch (Exception ignore) {}
              });
    }
  }
}
