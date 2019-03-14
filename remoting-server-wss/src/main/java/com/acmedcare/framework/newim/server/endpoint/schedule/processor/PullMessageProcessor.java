/*
 * Copyright 1999-2018 Acmedcare+ Holding Ltd.
 * Licensed under the Apache License, Version 2.0 (the "License");
 */

package com.acmedcare.framework.newim.server.endpoint.schedule.processor;

import com.acmedcare.framework.boot.web.socket.processor.WssSession;
import com.acmedcare.framework.newim.server.endpoint.WssMessageRequestProcessor;
import com.acmedcare.framework.newim.server.endpoint.schedule.ScheduleSysContext;
import com.acmedcare.framework.newim.wss.WssPayload;

/**
 * {@link PullMessageProcessor}
 *
 * @author <a href="mailto:iskp.me@gmail.com">Elve.Xu</a>
 * @version ${project.version} - 2019-03-14.
 */
public class PullMessageProcessor implements WssMessageRequestProcessor {

  private final ScheduleSysContext context;

  public PullMessageProcessor(ScheduleSysContext context) {
    this.context = context;
  }

  /**
   * Processor Wss Client Request
   *
   * @param session session
   * @param request request
   * @return response
   * @throws Exception exception
   */
  @Override
  public WssPayload.WssResponse processRequest(WssSession session, Object request)
      throws Exception {
    return null;
  }
}
