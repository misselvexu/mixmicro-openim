/*
 * Copyright 1999-2018 Acmedcare+ Holding Ltd.
 * Licensed under the Apache License, Version 2.0 (the "License");
 */

package com.acmedcare.framework.newim.master.connector;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * MasterConnectorAutoConfiguration
 *
 * @author <a href="mailto:iskp.me@gmail.com">Elve.Xu</a>
 * @version ${project.version} - 2018-12-17.
 */
@Configuration
@ConditionalOnProperty(prefix = "remoting.server.master", value = "enabled", havingValue = "true")
@EnableConfigurationProperties(MasterConnectorProperties.class)
public class DelivererMasterConnectorAutoConfiguration {

  public static final String DELIVERER_MASTER_CONNECTOR_BEAN_NAME = "delivererMasterConnector";

  @Bean
  DelivererMasterConnectorContext delivererMasterConnectorContext() {
    return new DelivererMasterConnectorContext();
  }

  @Bean(initMethod = "init", destroyMethod = "destroy", name = DELIVERER_MASTER_CONNECTOR_BEAN_NAME)
  public DelivererMasterConnector delivererMasterConnector(
      MasterConnectorProperties properties, DelivererMasterConnectorContext context) {
    return new DelivererMasterConnector(properties, context);
  }
}
