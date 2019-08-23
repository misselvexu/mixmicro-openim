/*
 * Copyright 1999-2018 Acmedcare+ Holding Ltd.
 * Licensed under the Apache License, Version 2.0 (the "License");
 */

package com.acmedcare.framework.newim.master.connector;

import com.acmedcare.framework.kits.lang.Nullable;

/**
 * {@link MasterConnectorContext}
 *
 * @author <a href="mailto:iskp.me@gmail.com">Elve.Xu</a>
 * @version ${project.version} - 2019-08-01.
 */
public abstract class MasterConnectorContext {

  /**
   * Register-ed master connector handler
   *
   * <p>
   */
  protected MasterConnectorHandler handler;

  /**
   * Register Method
   *
   * @param handler instance of {@link MasterConnectorHandler}
   */
  protected void registerMasterConnectorHandler(@Nullable MasterConnectorHandler handler) {
    this.handler = handler;
  }
}
