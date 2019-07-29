/*
 * Copyright 1999-2018 Acmedcare+ Holding Ltd.
 * Licensed under the Apache License, Version 2.0 (the "License");
 */

package com.acmedcare.framework.newim.deliver.connector.server;

import com.acmedcare.framework.newim.deliver.api.DefaultDelivererProperties;
import com.acmedcare.framework.newim.deliver.api.DelivererInitializer;
import com.acmedcare.framework.newim.deliver.api.exception.InitializerException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * {@link DelivererServerInitializer}
 *
 * @author <a href="mailto:iskp.me@gmail.com">Elve.Xu</a>
 * @version ${project.version} - 2019-07-29.
 */
public class DelivererServerInitializer extends DelivererInitializer {

  private static final Logger log = LoggerFactory.getLogger(DelivererServerInitializer.class);

  protected DelivererServerInitializer(DefaultDelivererProperties properties) {
    super(properties);
  }

  @Override
  public void init() throws InitializerException {

  }

  @Override
  public void startup() throws InitializerException {

  }

  @Override
  public void shutdown() throws InitializerException {

  }
}
