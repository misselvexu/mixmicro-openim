/*
 * Copyright 1999-2018 Acmedcare+ Holding Ltd.
 * Licensed under the Apache License, Version 2.0 (the "License");
 */

package com.acmedcare.framework.newim.deliver.context.processor;

import com.acmedcare.framework.newim.BizResult;
import com.acmedcare.tiffany.framework.remoting.netty.NettyRequestProcessor;
import com.acmedcare.tiffany.framework.remoting.protocol.RemotingCommand;
import io.netty.channel.ChannelHandlerContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static com.acmedcare.framework.newim.deliver.api.DelivererCommand.HANDSHAKE_COMMAND_VALUE;

/**
 * Default Processor
 *
 * @author <a href="mailto:iskp.me@gmail.com">Elve.Xu</a>
 * @version ${project.version} - 12/11/2018.
 */
public class DefaultDelivererProcessor implements NettyRequestProcessor {

  private static final Logger log = LoggerFactory.getLogger(DefaultDelivererProcessor.class);

  @Override
  public RemotingCommand processRequest(
      ChannelHandlerContext channelHandlerContext, RemotingCommand remotingCommand)
      throws Exception {

    RemotingCommand response = RemotingCommand.createResponseCommand(remotingCommand.getCode(), "DEFAULT");

    switch (remotingCommand.getCode()) {

      case HANDSHAKE_COMMAND_VALUE:
        log.info("[==] Received Deliverer Client handshake request ~");
        response.setBody(BizResult.SUCCESS.bytes());
        break;

      default:

        log.warn("[==] Default processor code:{} executing", remotingCommand.getCode());
        break;
    }

    return response;
  }

  @Override
  public boolean rejectRequest() {
    return false;
  }
}
