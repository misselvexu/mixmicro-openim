/*
 * Copyright 1999-2018 Acmedcare+ Holding Ltd.
 * Licensed under the Apache License, Version 2.0 (the "License");
 */

package com.acmedcare.framework.newim.server.im;

import com.acmedcare.framework.newim.server.Server;
import com.acmedcare.framework.newim.spi.Extension;

/**
 * {@link IMServer}
 *
 * @author <a href="mailto:iskp.me@gmail.com">Elve.Xu</a>
 * @version ${project.version} - 2019-07-15.
 */
@Extension("imserver")
public class IMServer implements Server {

  /**
   * Server Startup Method
   *
   * @return server instance
   */
  @Override
  public Server startup() {
    return null;
  }

  /**
   * Shutdown Server
   *
   * <p>
   */
  @Override
  public void shutdown() {

  }
}
