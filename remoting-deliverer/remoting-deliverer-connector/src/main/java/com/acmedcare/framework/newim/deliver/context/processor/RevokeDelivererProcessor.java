/*
 * Copyright 1999-2018 Acmedcare+ Holding Ltd.
 * Licensed under the Apache License, Version 2.0 (the "License");
 */

package com.acmedcare.framework.newim.deliver.context.processor;

import com.acmedcare.framework.kits.Assert;
import com.acmedcare.framework.newim.BizResult;
import com.acmedcare.framework.newim.deliver.api.header.RevokerDelivererMessageHeader;
import com.acmedcare.tiffany.framework.remoting.common.RemotingHelper;
import com.acmedcare.tiffany.framework.remoting.protocol.RemotingCommand;
import io.netty.channel.ChannelHandlerContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Optional;

/**
 * {@link RevokeDelivererProcessor}
 *
 * @author <a href="mailto:iskp.me@gmail.com">Elve.Xu</a>
 * @version ${project.version} - 2019-07-30.
 */
public class RevokeDelivererProcessor extends AbstractProcessor {

  private static final Logger log = LoggerFactory.getLogger(RevokeDelivererProcessor.class);

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

    RemotingCommand defaultResponse = RemotingCommand.createResponseCommand(request.getCode(), "");

    String remotingAddr = RemotingHelper.parseChannelRemoteAddr(ctx.channel());

    log.info("deliverer client :{} request revoker deliverer message .", remotingAddr);

    try {

      //
      RevokerDelivererMessageHeader header = (RevokerDelivererMessageHeader) request.decodeCommandCustomHeader(RevokerDelivererMessageHeader.class);

      Assert.notNull(header,"revoker deliverer message must not be null.");

      this.delivererService.revokerDelivererMessage(header.getPassportId(),header.getMessageId());

      // succeed
      defaultResponse.setBody(BizResult.SUCCESS.bytes());

      return defaultResponse;

    } catch (Exception e) {
      defaultResponse.setBody(
          BizResult.builder()
              .code(-1)
              .exception(
                  BizResult.ExceptionWrapper.builder()
                      .type(e.getClass())
                      .message(
                          Optional.ofNullable(e.getMessage())
                              .orElse("request revoker deliverer message exception-ed."))
                      .build())
              .build()
              .bytes());
    }

    // return response
    return defaultResponse;
  }

}
