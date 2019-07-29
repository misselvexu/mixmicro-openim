/*
 * Copyright 1999-2018 Acmedcare+ Holding Ltd.
 * Licensed under the Apache License, Version 2.0 (the "License");
 */

package com.acmedcare.framework.newim.deliver.api;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.io.Serializable;

/**
 * {@link DefaultDelivererProperties}
 *
 * @author <a href="mailto:iskp.me@gmail.com">Elve.Xu</a>
 * @version ${project.version} - 2019-07-29.
 */
@Getter
@Setter
@NoArgsConstructor
public class DefaultDelivererProperties implements Serializable {

  /**
   * Remoting Call Request Timeout , Default: 5000 ms
   *
   * <p>
   */
  private long requestTimeout = 5000;



}
