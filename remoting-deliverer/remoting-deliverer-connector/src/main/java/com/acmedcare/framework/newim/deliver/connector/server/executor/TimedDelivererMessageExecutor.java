/*
 * Copyright 1999-2018 Acmedcare+ Holding Ltd.
 * Licensed under the Apache License, Version 2.0 (the "License");
 */

package com.acmedcare.framework.newim.deliver.connector.server.executor;

import com.acmedcare.framework.newim.deliver.services.DelivererService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * {@link TimedDelivererMessageExecutor}
 *
 * @author <a href="mailto:iskp.me@gmail.com">Elve.Xu</a>
 * @version ${project.version} - 2019-08-14.
 */
public final class TimedDelivererMessageExecutor {

  private static final Logger log = LoggerFactory.getLogger(TimedDelivererMessageExecutor.class);

  /** Instance of {@link DelivererService} */
  private final DelivererService delivererService;

  public TimedDelivererMessageExecutor(DelivererService delivererService) {
    this.delivererService = delivererService;
  }

  // ===== init & destroy methods =====

  public void init() {
    log.info("[==] Startup Timed-Deliverer executor ");
    // todo
  }

  public void destroy() {
    log.info("[==] Shutdown Timed-Deliverer executor ");
    // todo
  }
}
