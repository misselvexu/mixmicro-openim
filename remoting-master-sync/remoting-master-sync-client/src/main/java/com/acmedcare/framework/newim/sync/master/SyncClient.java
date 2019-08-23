/*
 * Copyright 2014-2019 Acmedcare+ Holding Ltd.
 * Licensed under the Apache License, Version 2.0 (the "License");
 */

package com.acmedcare.framework.newim.sync.master;

import com.acmedcare.framework.newim.sync.master.config.SyncProperties;

/**
 * {@link SyncClient}
 *
 * @author <a href="mailto:iskp.me@gmail.com">Elve.Xu</a>
 * @version ${project.version} - 2019-03-17.
 */
public class SyncClient {

  /**
   * Constructor
   *
   * @param syncProperties sync properties
   */
  public SyncClient(SyncProperties syncProperties) {
    this.syncProperties = syncProperties;
  }

  private final SyncProperties syncProperties;
}
