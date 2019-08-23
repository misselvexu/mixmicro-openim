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
public class DefaultMasterConnectorAutoConfiguration {

  @Bean(initMethod = "init", destroyMethod = "destroy", name = "defaultMasterConnector")
  public DefaultMasterConnector defaultMasterConnector(MasterConnectorProperties properties) {
    return new DefaultMasterConnector(properties);
  }
}
