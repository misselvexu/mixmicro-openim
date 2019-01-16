/*
 * Copyright 1999-2018 Acmedcare+ Holding Ltd.
 * Licensed under the Apache License, Version 2.0 (the "License");
 */

package com.acmedcare.framework.newim;

import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.io.Serializable;

/**
 * RemotingEvent
 *
 * @author <a href="mailto:iskp.me@gmail.com">Elve.Xu</a>
 * @version ${project.version} - 2019-01-15.
 */
@Getter
@Setter
@NoArgsConstructor
public class RemotingEvent implements Serializable {

  /** Event Name */
  private String event;

  /** Event Payload */
  private byte[] payload;

  @Builder
  public RemotingEvent(String event, byte[] payload) {
    this.event = event;
    this.payload = payload;
  }
}
