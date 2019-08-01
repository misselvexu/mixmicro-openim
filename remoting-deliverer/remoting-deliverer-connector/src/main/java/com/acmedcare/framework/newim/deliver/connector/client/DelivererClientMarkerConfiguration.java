/*
 * Copyright 1999-2018 Acmedcare+ Holding Ltd.
 * Licensed under the Apache License, Version 2.0 (the "License");
 */

package com.acmedcare.framework.newim.deliver.connector.client;

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

  @Bean(initMethod = "init", destroyMethod = "shutdown")
  @ConditionalOnMissingBean(DelivererClientInitializer.class)
  DelivererClientInitializer clientInitializer(DelivererClientProperties properties) {
    return new DelivererClientInitializer(properties);
  }
}
