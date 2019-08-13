/*
 * Copyright 1999-2018 Acmedcare+ Holding Ltd.
 * Licensed under the Apache License, Version 2.0 (the "License");
 */

package com.acmedcare.framework.newim.deliver.context.processor;

import com.acmedcare.framework.kits.Assert;
import com.acmedcare.framework.newim.deliver.context.ConnectorContext;
import com.acmedcare.framework.newim.deliver.services.DelivererService;
import com.acmedcare.tiffany.framework.remoting.netty.NettyRequestProcessor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * {@link AbstractProcessor}
 *
 * @author <a href="mailto:iskp.me@gmail.com">Elve.Xu</a>
 * @version ${project.version} - 2019-08-13.
 */
public abstract class AbstractProcessor implements NettyRequestProcessor {

  /**
   * Processor Log Instance Defined
   *
   * <p>
   */
  protected static final Logger processorLog = LoggerFactory.getLogger("com.acmedcare.framework.newim.deliver.context.processor");

  /**
   * Protected Constructor
   *
   * <p>
   */
  protected AbstractProcessor() {
    this.delivererService = ConnectorContext.context().getBean(DelivererService.class);
    Assert.notNull(delivererService, "Deliverer service instance handler must not be null .");
  }

  /**
   * Reject Request Rules
   *
   * @return true/false
   */
  @Override
  public boolean rejectRequest() {
    return false;
  }

  // ====== Deliverer Framework Service Instance Defined =======

  /**
   * Deliverer Handler Service
   *
   * <p>
   */
  protected final DelivererService delivererService;
}
