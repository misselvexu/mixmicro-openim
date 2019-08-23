/*
 * Copyright 1999-2018 Acmedcare+ Holding Ltd.
 * Licensed under the Apache License, Version 2.0 (the "License");
 */

package com.acmedcare.framework.newim.deliver.connector.client;

import com.acmedcare.framework.newim.deliver.api.DefaultDelivererProperties;
import com.google.common.collect.Lists;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

import javax.validation.constraints.NotEmpty;
import java.util.List;

/**
 * {@link DelivererClientProperties}
 *
 * @author <a href="mailto:iskp.me@gmail.com">Elve.Xu</a>
 * @version ${project.version} - 2019-07-29.
 */
@Getter
@Setter
@Validated
@NoArgsConstructor
@ConfigurationProperties(prefix = "remoting.deliverer.client")
public class DelivererClientProperties extends DefaultDelivererProperties {

  private static final long serialVersionUID = -5026059123829689693L;

  @Value("${spring.application.name}")
  private String serviceId;

  /**
   * Remoting Deliverer Server Address List, Like: xx.xx.xx.xx:14110,xx.xx.xx.xxx:14110
   *
   * <p>
   */
  @NotEmpty(
      message =
          "remoting server address list must not be empty , example: xx.xx.xx.xx:14110,xx.xx.xx.xxx:14110")
  private List<String> remotingAddr = Lists.newArrayList();

  /** Connect remoting server delay time, default: 5000 ms */
  private long connectDelay = 5000;

  /** Enabled heartbeat flag ,default : true */
  private boolean heartbeatEnabled = true;

  private boolean ssl = false;

  /** Heartbeat Timer Period , Default : 10 * 1000 ms */
  private long heartbeatPeriod = 10 * 1000;

  /**
   * Heartbeat request execute failed times, if bigger than assigned times, server will reconnect .
   *
   * <p>default: 3 times
   */
  private int maxHeartbeatFailedTimes = 3;

  /**
   * Submit Request Fast Failed Enabled , default: true
   *
   * <p>
   */
  private boolean postRequestFastFailEnabled = false;

  /** fast fail max retry times , default: 3 */
  private int maxFastFailedRetryTimes = 3;

  /** Fast fail retry delay time, default: 10 s */
  private int fastFailRetryDelay = 1000 * 10;

  /**
   * Post Queue Capacity ,default: 2048
   *
   * <p>
   */
  private int postQueueCapacity = 2048;

  private int postQueueThreadNum = 5;
}
