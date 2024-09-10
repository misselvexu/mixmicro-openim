/*
 * Copyright 1999-2018 Acmedcare+ Holding Ltd.
 * Licensed under the Apache License, Version 2.0 (the "License");
 */

package com.acmedcare.framework.newim.deliver.connector.server;

import com.acmedcare.framework.newim.deliver.connector.server.executor.TimedDelivererMessageExecutor;
import com.acmedcare.framework.newim.deliver.services.DelivererService;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;

/**
 * {@link DelivererServerMarkerConfiguration}
 *
 * @author <a href="mailto:iskp.me@gmail.com">Elve.Xu</a>
 * @version ${project.version} - 2019-07-29.
 */
@EnableConfigurationProperties(DelivererServerProperties.class)
public class DelivererServerMarkerConfiguration {

  public static final String DELIVERER_SERVER_INITIALIZER_BEAN_NAME = "serverInitializer";

  public static final String TIMED_DELIVERER_MESSAGE_EXECUTOR_BEAN_NAME = "timedDelivererMessageExecutor";

  public static final String DELIVERER_SERVER_TIMED_ENABLED = "remoting.deliverer.server.timer";

  // =====

  @Bean(initMethod = "init",
      destroyMethod = "shutdown",
      name = DELIVERER_SERVER_INITIALIZER_BEAN_NAME)
  @ConditionalOnMissingBean(DelivererServerInitializer.class)
  DelivererServerInitializer serverInitializer(DelivererServerProperties properties) {
    return new DelivererServerInitializer(properties);
  }

  @Bean(initMethod = "init",
      destroyMethod = "destroy",
      name = TIMED_DELIVERER_MESSAGE_EXECUTOR_BEAN_NAME)
  @ConditionalOnProperty(
      prefix = DELIVERER_SERVER_TIMED_ENABLED,
      name = "enabled",
      havingValue = "true",
      matchIfMissing = true)
  @ConditionalOnMissingBean(TimedDelivererMessageExecutor.class)
  TimedDelivererMessageExecutor timedDelivererMessageExecutor(
      DelivererService delivererService, DelivererServerProperties properties) {
    return new TimedDelivererMessageExecutor(delivererService, properties);
  }
}
