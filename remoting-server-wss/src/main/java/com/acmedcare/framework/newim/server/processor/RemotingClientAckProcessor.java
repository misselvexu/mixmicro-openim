/*
 * Copyright 2014-2019 Acmedcare+ Holding Ltd.
 * Licensed under the Apache License, Version 2.0 (the "License");
 */

package com.acmedcare.framework.newim.server.processor;

import com.acmedcare.framework.newim.server.core.IMSession;
import com.acmedcare.framework.newim.server.service.MessageService;
import com.acmedcare.tiffany.framework.remoting.netty.NettyRequestProcessor;
import com.acmedcare.tiffany.framework.remoting.protocol.RemotingCommand;
import io.netty.channel.ChannelHandlerContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * {@link RemotingClientAckProcessor}
 *
 * @author <a href="mailto:iskp.me@gmail.com">Elve.Xu</a>
 * @version ${project.version} - 2019-07-11.
 */
public class RemotingClientAckProcessor implements NettyRequestProcessor {

  private static final Logger log = LoggerFactory.getLogger(RemotingClientAckProcessor.class);

  private final IMSession imSession;
  private final MessageService messageService;

  public RemotingClientAckProcessor(IMSession imSession, MessageService messageService) {
    this.imSession = imSession;
    this.messageService = messageService;
  }

  /**
   * Process Request
   *
   * @param ctx channel handler context
   * @param request request command
   * @return process result (if udp request processor ,will return null)
   * @throws Exception exception
   */
  @Override
  public RemotingCommand processRequest(ChannelHandlerContext ctx, RemotingCommand request)
      throws Exception {

    // TODO processor client ack command

    return null;
  }

  /**
   * Reject Request Rules
   *
   * @return true/false
   */
  @Override
  public boolean rejectRequest() {
    return false;
  }
}
