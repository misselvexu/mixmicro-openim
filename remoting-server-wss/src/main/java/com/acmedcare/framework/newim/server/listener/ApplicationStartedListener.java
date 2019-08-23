/*
 * Copyright 1999-2018 Acmedcare+ Holding Ltd.
 * Licensed under the Apache License, Version 2.0 (the "License");
 */

package com.acmedcare.framework.newim.server.listener;

import com.acmedcare.framework.kits.Assert;
import com.acmedcare.framework.newim.deliver.connector.client.executor.DelivererMessageExecutor;
import com.acmedcare.framework.newim.server.core.IMSession;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationListener;

import static com.acmedcare.framework.newim.deliver.connector.client.DelivererClientMarkerConfiguration.POST_QUEUE_DELIVERER_MESSAGE_EXECUTOR_BEAN_NAME;

/**
 * {@link ApplicationStartedListener}
 *
 * @author <a href="mailto:iskp.me@gmail.com">Elve.Xu</a>
 * @version ${project.version} - 2019-08-22.
 */
public class ApplicationStartedListener implements ApplicationListener<ApplicationReadyEvent> {

  private static final Logger log = LoggerFactory.getLogger(ApplicationStartedListener.class);

  /**
   * Handle an application event.
   *
   * @param event the event to respond to
   */
  @Override
  public void onApplicationEvent(ApplicationReadyEvent event) {

    log.info("[==] Remoting Server is Started");

    ApplicationContext applicationContext = event.getApplicationContext();

    log.info("[==] Ready to inject deliverer server instance bean .");

    IMSession imSession = applicationContext.getBean(IMSession.class);

    Assert.isTrue(applicationContext.containsBean(POST_QUEUE_DELIVERER_MESSAGE_EXECUTOR_BEAN_NAME));
    DelivererMessageExecutor delivererMessageExecutor =
        applicationContext.getBean(
            POST_QUEUE_DELIVERER_MESSAGE_EXECUTOR_BEAN_NAME, DelivererMessageExecutor.class);

    imSession.registerDelivererMessageExecutor(delivererMessageExecutor);

    log.info("[==] deliverer message executor inject succeed. ");
  }
}
