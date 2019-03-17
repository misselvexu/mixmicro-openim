/*
 * Copyright 1999-2018 Acmedcare+ Holding Ltd.
 * Licensed under the Apache License, Version 2.0 (the "License");
 */

package com.acmedcare.framework.newim.sync.master;

import com.acmedcare.framework.newim.sync.master.config.SyncServerProperties;

/**
 * {@link MasterSyncServer}
 *
 * @author <a href="mailto:iskp.me@gmail.com">Elve.Xu</a>
 * @version ${project.version} - 2019-03-17.
 */
public class MasterSyncServer {

  private final SyncServerProperties syncServerProperties;

  /**
   * Constructor
   *
   * @param syncServerProperties sync server properties
   */
  public MasterSyncServer(SyncServerProperties syncServerProperties) {
    this.syncServerProperties = syncServerProperties;
  }
}
