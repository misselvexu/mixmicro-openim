/*
 * Copyright 1999-2018 Acmedcare+ Holding Ltd.
 * Licensed under the Apache License, Version 2.0 (the "License");
 */

package com.acmedcare.framework.newim.deliver.connector.client;

import com.acmedcare.framework.newim.deliver.connector.client.executor.DelivererMessageExecutor;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;

/**
 * {@link DelivererClientMarkerConfiguration}
 *
 * @author <a href="mailto:iskp.me@gmail.com">Elve.Xu</a>
 * @version ${project.version} - 2019-07-29.
 */
@EnableConfigurationProperties(DelivererClientProperties.class)
public class DelivererClientMarkerConfiguration {

  public static final String DELIVERER_CLIENT_INITIALIZER_BEAN_NAME = "clientInitializer";

  public static final String POST_QUEUE_DELIVERER_MESSAGE_EXECUTOR_BEAN_NAME = "delivererMessagePostQueueExecutor";

  @Bean(
      initMethod = "init",
      destroyMethod = "shutdown",
      name = DELIVERER_CLIENT_INITIALIZER_BEAN_NAME)
  @ConditionalOnMissingBean(DelivererClientInitializer.class)
  DelivererClientInitializer clientInitializer(DelivererClientProperties properties) {
    return new DelivererClientInitializer(properties);
  }

  @Bean(
      initMethod = "init",
      destroyMethod = "destroy",
      name = POST_QUEUE_DELIVERER_MESSAGE_EXECUTOR_BEAN_NAME)
  @ConditionalOnMissingBean(DelivererMessageExecutor.class)
  DelivererMessageExecutor delivererMessagePostQueueExecutor(DelivererClientProperties properties) {
    return new DelivererMessageExecutor(properties);
  }
}
