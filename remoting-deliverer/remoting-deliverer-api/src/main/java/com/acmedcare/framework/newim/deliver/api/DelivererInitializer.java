/*
 * Copyright 1999-2018 Acmedcare+ Holding Ltd.
 * Licensed under the Apache License, Version 2.0 (the "License");
 */

package com.acmedcare.framework.newim.deliver.api;

import com.acmedcare.framework.newim.deliver.api.exception.InitializerException;
import com.alibaba.fastjson.JSON;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * {@link DelivererInitializer}
 *
 * @author <a href="mailto:iskp.me@gmail.com">Elve.Xu</a>
 * @version ${project.version} - 2019-07-29.
 */
public abstract class DelivererInitializer {

  private static final Logger log = LoggerFactory.getLogger(DelivererInitializer.class);

  private final DefaultDelivererProperties properties;

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
}
