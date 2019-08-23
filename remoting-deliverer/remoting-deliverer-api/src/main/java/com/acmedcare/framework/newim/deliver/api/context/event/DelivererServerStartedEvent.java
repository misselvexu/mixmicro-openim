/*
 * Copyright 1999-2018 Acmedcare+ Holding Ltd.
 * Licensed under the Apache License, Version 2.0 (the "License");
 */

package com.acmedcare.framework.newim.deliver.api.context.event;

import com.acmedcare.tiffany.framework.remoting.RemotingSocketServer;
import org.springframework.context.ApplicationEvent;

/**
 * {@link DelivererServerStartedEvent}
 *
 * @author <a href="mailto:iskp.me@gmail.com">Elve.Xu</a>
 * @version ${project.version} - 2019-07-29.
 */
public class DelivererServerStartedEvent extends ApplicationEvent {

  private static final long serialVersionUID = 5739886646812610007L;

  private final RemotingSocketServer server;
  /**
   * Create a new ApplicationEvent.
   *
   * @param source the object on which the event initially occurred (never {@code null})
   */
  public DelivererServerStartedEvent(RemotingSocketServer source) {
    super(source);
    this.server = source;
  }
}
