/*
 * Copyright 1999-2018 Acmedcare+ Holding Ltd.
 * Licensed under the Apache License, Version 2.0 (the "License");
 */

package com.acmedcare.framework.newim.deliver.connector.server;

import com.acmedcare.framework.newim.deliver.api.DefaultDelivererProperties;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * {@link DelivererServerProperties}
 *
 * @author <a href="mailto:iskp.me@gmail.com">Elve.Xu</a>
 * @version ${project.version} - 2019-07-29.
 */
@Getter
@Setter
@NoArgsConstructor
@ConfigurationProperties(prefix = "remoting.deliverer.server")
public class DelivererServerProperties extends DefaultDelivererProperties {

  /**
   * Deliverer Connector Server Host Address, Default: 0.0.0.0
   */
  private String host = "0.0.0.0";

  /**
   * Deliverer Connector Server Port , Default: 14110
   */
  private int port = 14110;

}
