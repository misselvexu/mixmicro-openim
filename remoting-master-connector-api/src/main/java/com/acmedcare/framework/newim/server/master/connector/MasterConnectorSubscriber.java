/*
 * Copyright 1999-2018 Acmedcare+ Holding Ltd.
 * Licensed under the Apache License, Version 2.0 (the "License");
 */

package com.acmedcare.framework.newim.server.master.connector;

import com.acmedcare.framework.kits.event.Event;
import com.acmedcare.framework.kits.event.Subscriber;

/**
 * MasterConnectorSubscriber
 *
 * @author <a href="mailto:iskp.me@gmail.com">Elve.Xu</a>
 * @version ${project.version} - 2018-12-17.
 */
public class MasterConnectorSubscriber extends Subscriber {

  protected final MasterConnectorContext context;

  public MasterConnectorSubscriber(MasterConnectorContext context, boolean sync) {
    super(sync);
    this.context = context;
  }

  /**
   * 事件处理，请处理异常
   *
   * @param event 事件
   */
  @Override
  public void onEvent(Event event) {
    // empty implement
  }

}
