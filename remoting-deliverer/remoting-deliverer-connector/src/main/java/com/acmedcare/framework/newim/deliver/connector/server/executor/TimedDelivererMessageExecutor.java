/*
 * Copyright 1999-2018 Acmedcare+ Holding Ltd.
 * Licensed under the Apache License, Version 2.0 (the "License");
 */

package com.acmedcare.framework.newim.deliver.connector.server.executor;

import com.acmedcare.framework.kits.thread.DefaultThreadFactory;
import com.acmedcare.framework.kits.thread.ThreadKit;
import com.acmedcare.framework.newim.deliver.api.bean.DelivererMessageBean;
import com.acmedcare.framework.newim.deliver.connector.server.DelivererServerProperties;
import com.acmedcare.framework.newim.deliver.context.ConnectorContext;
import com.acmedcare.framework.newim.deliver.services.DelivererService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.Random;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

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

  /** Instance of {@link DelivererServerProperties} */
  private final DelivererServerProperties properties;

  private ScheduledExecutorService scheduledExecutorService;

  private AtomicBoolean initialized = new AtomicBoolean(false);

  private Random disorganizeRandom = new Random();

  public TimedDelivererMessageExecutor(
      DelivererService delivererService, DelivererServerProperties properties) {
    this.delivererService = delivererService;
    this.properties = properties;
  }

  // ===== init & destroy methods =====

  public void init() {
    log.info("[==] Startup Timed-Deliverer executor ");
    if (initialized.compareAndSet(false, true)) {
      scheduledExecutorService =
          new ScheduledThreadPoolExecutor(
              this.properties.getTimer().getThreadCoreSize(),
              new DefaultThreadFactory("IM-TIMED-DELIVERER-EXECUTOR-"));
    }

    for (int i = 0; i < this.properties.getTimer().getThreadCoreSize(); i++) {
      scheduledExecutorService.scheduleWithFixedDelay(
          this::execute,
          this.properties.getTimer().getThreadInitDelay(),
          this.properties.getTimer().getThreadDelay(),
          TimeUnit.MILLISECONDS);
    }
  }

  // execute method start =====

  private void execute() {

    try {

      if(!ConnectorContext.context().isDelivererContextAvailable()){
        return;
      }

      if (this.properties.getTimer().isDisorganizeEnabled()) {
        ThreadKit.sleep(
            disorganizeRandom.nextInt(this.properties.getTimer().getDisorganizeInterval()),
            TimeUnit.MILLISECONDS);
      }

      List<DelivererMessageBean> delivererMessages = this.delivererService.fetchDelivererMessages(this.properties.getTimer().getBatchRow());

      log.info("[==] Timed deliverer executor fetch message list size: {}" ,delivererMessages.size());

      ConnectorContext.context().positiveDelivererMessages(delivererMessages);

    } catch (Exception e) {
      log.warn("[==] Timed deliverer executor execute exception, ignore .", e);
    }
  }

  // --EOF-- execute method end =====

  public void destroy() {
    log.info("[==] Shutdown Timed-Deliverer executor ");
    if (initialized.compareAndSet(true, false)) {
      ThreadKit.gracefulShutdown(scheduledExecutorService, 5, 5, TimeUnit.SECONDS);
    }
  }
}
