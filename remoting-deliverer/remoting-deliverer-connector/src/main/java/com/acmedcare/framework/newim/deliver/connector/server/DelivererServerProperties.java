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
import org.springframework.validation.annotation.Validated;

import javax.validation.constraints.Max;
import javax.validation.constraints.Min;
import javax.validation.constraints.NotBlank;

/**
 * {@link DelivererServerProperties}
 *
 * @author <a href="mailto:iskp.me@gmail.com">Elve.Xu</a>
 * @version ${project.version} - 2019-07-29.
 */
@Getter
@Setter
@Validated
@NoArgsConstructor
@ConfigurationProperties(prefix = "remoting.deliverer.server")
public class DelivererServerProperties extends DefaultDelivererProperties {

  /**
   * Deliverer Connector Server Host Address, Default: 0.0.0.0
   */
  @NotBlank
  private String host = "0.0.0.0";

  /**
   * Deliverer Connector Server Port , Default: 14110
   */
  @Min(0)
  @Max(65536)
  private int port = 14110;

}
