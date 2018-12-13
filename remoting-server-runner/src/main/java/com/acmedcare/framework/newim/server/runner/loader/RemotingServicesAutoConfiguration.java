package com.acmedcare.framework.newim.server.runner.loader;

import com.acmedcare.framework.newim.server.Server;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.beans.factory.BeanFactoryAware;
import org.springframework.beans.factory.InitializingBean;

/**
 * Remoting Services Auto Configuration
 *
 * @author <a href="mailto:iskp.me@gmail.com">Elve.Xu</a>
 * @version ${project.version} - 2018-12-13.
 */
public class RemotingServicesAutoConfiguration implements InitializingBean, BeanFactoryAware {

  private static final Logger logger =
      LoggerFactory.getLogger(RemotingServicesAutoConfiguration.class);

  private BeanFactory beanFactory;

  @Override
  public void afterPropertiesSet() throws Exception {
    logger.info("[Remoting] Starting refresh server services factory ...");
    ServerServiceFactory.refresh(beanFactory);
    logger.info("[Remoting] server services factory load succeed.");

    ServerServiceFactory.instances().forEach(Server::startup);
  }

  @Override
  public void setBeanFactory(BeanFactory beanFactory) throws BeansException {
    this.beanFactory = beanFactory;
  }
}
