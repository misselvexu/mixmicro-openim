/*
 * Copyright 1999-2018 Acmedcare+ Holding Ltd.
 * Licensed under the Apache License, Version 2.0 (the "License");
 */

package com.acmedcare.framework.newim.deliver.api;

import com.acmedcare.framework.newim.deliver.api.exception.InitializerException;
import com.alibaba.fastjson.JSON;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.context.ApplicationEventPublisherAware;

/**
 * {@link DelivererInitializer}
 *
 * @author <a href="mailto:iskp.me@gmail.com">Elve.Xu</a>
 * @version ${project.version} - 2019-07-29.
 */
public abstract class DelivererInitializer implements ApplicationEventPublisherAware {

  private static final Logger log = LoggerFactory.getLogger(DelivererInitializer.class);

  protected final DefaultDelivererProperties properties;

  protected ApplicationEventPublisher publisher;

  protected DelivererInitializer(DefaultDelivererProperties properties) {
    this.properties = properties;
    log.debug(
        "[==] Deliverer Initializer:{} Properties is : {}",
        properties.getClass(),
        JSON.toJSONString(properties));
  }

  /**
   * Do Initialize Progress
   *
   * @throws InitializerException maybe thrown exception {@link InitializerException}
   */
  public abstract void init() throws InitializerException;

  /**
   * Startup Context When Application Context is Ready.
   *
   * @throws InitializerException maybe thrown exception {@link InitializerException}
   */
  public abstract void startup() throws InitializerException;

  /**
   * Shutdown Deliverer Context When Class Context is Ready to Shutdown.
   *
   * @throws InitializerException maybe thrown exception {@link InitializerException}
   */
  public abstract void shutdown() throws InitializerException;

  /**
   * Set the ApplicationEventPublisher that this object runs in.
   *
   * <p>Invoked after population of normal bean properties but before an init callback like
   * InitializingBean's afterPropertiesSet or a custom init-method. Invoked before
   * ApplicationContextAware's setApplicationContext.
   *
   * @param applicationEventPublisher event publisher to be used by this object
   */
  @Override
  public void setApplicationEventPublisher(ApplicationEventPublisher applicationEventPublisher) {
    this.publisher = applicationEventPublisher;
  }
}
