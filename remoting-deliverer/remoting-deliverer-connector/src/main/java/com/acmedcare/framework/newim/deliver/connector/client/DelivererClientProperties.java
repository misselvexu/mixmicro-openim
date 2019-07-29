/*
 * Copyright 1999-2018 Acmedcare+ Holding Ltd.
 * Licensed under the Apache License, Version 2.0 (the "License");
 */

package com.acmedcare.framework.newim.deliver.connector.client;

import com.acmedcare.framework.newim.deliver.api.DefaultDelivererProperties;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;

import java.util.List;

/**
 * {@link DelivererClientProperties}
 *
 * @author <a href="mailto:iskp.me@gmail.com">Elve.Xu</a>
 * @version ${project.version} - 2019-07-29.
 */
@Getter
@Setter
@NoArgsConstructor
@ConfigurationProperties(prefix = "remoting.deliverer.client")
public class DelivererClientProperties extends DefaultDelivererProperties {

  /**
   * Remoting Deliverer Server Address List, Like: xx.xx.xx.xx:14110,xx.xx.xx.xxx:14110
   *
   * <p>
   */
  private List<String> remotingAddr;
}
