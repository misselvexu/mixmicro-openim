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
import org.springframework.boot.context.properties.NestedConfigurationProperty;
import org.springframework.validation.annotation.Validated;

import javax.validation.constraints.Max;
import javax.validation.constraints.Min;
import javax.validation.constraints.NotBlank;
import java.io.Serializable;

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

  private static final long serialVersionUID = -1113631365862971274L;

  /** Deliverer Connector Server Host Address, Default: 0.0.0.0 */
  @NotBlank private String host = "0.0.0.0";

  /** Deliverer Connector Server Port , Default: 14110 */
  @Min(0)
  @Max(65536)
  private int port = 14110;

  /**
   * Deliverer Timer Config Properties
   *
   * @see DelivererTimedProperties
   */
  @NestedConfigurationProperty private DelivererTimedProperties timer;

  @Getter
  @Setter
  @NoArgsConstructor
  public static class DelivererTimedProperties implements Serializable {

    private static final long serialVersionUID = 1380876216311196413L;

    private boolean enabled = true;

    /**
     * Batch Fetch Row Size ,Default: 50
     *
     * <p>
     */
    private int batchRow = 50;

    /** thread startup delay time(ms), default: 5000 */
    private long threadDelay = 5000;

    /** thread execute timed, peroid time(ms), default: 10000 */
    private long threadPeriod = 10000;

    /** disorganize enable flag ,default is false */
    private boolean disorganizeEnabled = false;

    /** disorganize interval ,default: 5000 ms */
    private long disorganizeInterval = 5000;
  }
}
