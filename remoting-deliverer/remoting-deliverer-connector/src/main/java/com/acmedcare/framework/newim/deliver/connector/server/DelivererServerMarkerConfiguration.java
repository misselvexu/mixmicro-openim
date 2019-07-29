/*
 * Copyright 1999-2018 Acmedcare+ Holding Ltd.
 * Licensed under the Apache License, Version 2.0 (the "License");
 */

package com.acmedcare.framework.newim.deliver.connector.server;

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

  @Bean
  DelivererServerInitializer serverInitializer(DelivererServerProperties properties) {
    System.out.println("serverInitializer");
    return new DelivererServerInitializer(properties);
  }
}
