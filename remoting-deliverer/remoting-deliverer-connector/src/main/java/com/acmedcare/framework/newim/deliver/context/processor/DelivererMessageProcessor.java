/*
 * Copyright 1999-2018 Acmedcare+ Holding Ltd.
 * Licensed under the Apache License, Version 2.0 (the "License");
 */

package com.acmedcare.framework.newim.deliver.context.processor;

import com.acmedcare.framework.kits.Assert;
import com.acmedcare.framework.newim.BizResult;
import com.acmedcare.framework.newim.deliver.api.header.DelivererMessageHeader;
import com.acmedcare.framework.newim.deliver.api.request.DelivererMessageRequestBean;
import com.acmedcare.framework.newim.deliver.context.ConnectorContext;
import com.acmedcare.framework.newim.deliver.services.DelivererService;
import com.acmedcare.tiffany.framework.remoting.common.RemotingHelper;
import com.acmedcare.tiffany.framework.remoting.netty.NettyRequestProcessor;
import com.acmedcare.tiffany.framework.remoting.protocol.RemotingCommand;
import com.alibaba.fastjson.JSON;
import io.netty.channel.ChannelHandlerContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Optional;

/**
 * {@link DelivererMessageProcessor}
 *
 * @author <a href="mailto:iskp.me@gmail.com">Elve.Xu</a>
 * @version ${project.version} - 2019-07-30.
 */
public class DelivererMessageProcessor implements NettyRequestProcessor {

  private static final Logger log = LoggerFactory.getLogger(DelivererMessageProcessor.class);

  /** Deliverer Server Side */
  private final boolean delivererServerSide;

  public DelivererMessageProcessor(boolean delivererServerSide) {
    this.delivererServerSide = delivererServerSide;
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

    RemotingCommand defaultResponse = RemotingCommand.createResponseCommand(request.getCode(), "");

    String remotingAddr = RemotingHelper.parseChannelRemoteAddr(ctx.channel());

    log.info("deliverer client :{} request post deliverer message .", remotingAddr);

    try {

      DelivererMessageHeader header =
          (DelivererMessageHeader) request.decodeCommandCustomHeader(DelivererMessageHeader.class);

      Assert.notNull(header, "deliverer client post message request header must not be null .");

      byte[] payload = request.getBody();

      if (payload != null && payload.length > 0) {

        DelivererMessageRequestBean messageRequestBean = JSON.parseObject(payload, DelivererMessageRequestBean.class);

        DelivererService service = ConnectorContext.context().getBean(DelivererService.class);

        Assert.notNull(service,"Deliverer service instance handler must not be null .");

        // execute biz
        service.postDelivererMessage(
            messageRequestBean.isHalf(),
            messageRequestBean.getNamespace(),
            messageRequestBean.getPassportId(),
            messageRequestBean.getMessageType(),
            messageRequestBean.getMessage());

        // succeed

        defaultResponse.setBody(BizResult.SUCCESS.bytes());

        return defaultResponse;

      } else {
        defaultResponse.setBody(
            BizResult.builder()
                .code(-1)
                .data("deliverer client post message request param payload is empty .")
                .build()
                .bytes());
      }

    } catch (Exception e) {
      defaultResponse.setBody(
          BizResult.builder()
              .code(-1)
              .exception(
                  BizResult.ExceptionWrapper.builder()
                      .type(e.getClass())
                      .message(
                          Optional.ofNullable(e.getMessage())
                              .orElse("deliverer client post message exception-ed."))
                      .build())
              .build()
              .bytes());
    }

    // return response
    return defaultResponse;
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
