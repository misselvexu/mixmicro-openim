/*
 * Copyright 1999-2018 Acmedcare+ Holding Ltd.
 * Licensed under the Apache License, Version 2.0 (the "License");
 */

package com.acmedcare.framework.newim.deliver.context.processor;

import com.acmedcare.framework.kits.Assert;
import com.acmedcare.framework.newim.BizResult;
import com.acmedcare.framework.newim.deliver.api.bean.DelivererMessageBean;
import com.acmedcare.framework.newim.deliver.api.exception.RemotingDelivererException;
import com.acmedcare.framework.newim.deliver.api.header.MessageHeader;
import com.acmedcare.framework.newim.deliver.api.request.MessageRequestBean;
import com.acmedcare.framework.newim.deliver.api.response.MessageResponseBean;
import com.acmedcare.tiffany.framework.remoting.common.RemotingHelper;
import com.acmedcare.tiffany.framework.remoting.protocol.RemotingCommand;
import com.alibaba.fastjson.JSON;
import io.netty.channel.ChannelHandlerContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.Optional;

/**
 * {@link MessageProcessor}
 *
 * @author <a href="mailto:iskp.me@gmail.com">Elve.Xu</a>
 * @version ${project.version} - 2019-07-30.
 */
public class MessageProcessor extends AbstractProcessor {

  private static final Logger log = LoggerFactory.getLogger(MessageProcessor.class);

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

    log.info("deliverer client :{} request ready to deliverer message list .", remotingAddr);

    try {

      MessageHeader header = (MessageHeader) request.decodeCommandCustomHeader(MessageHeader.class);

      Assert.notNull(header, "deliverer message list request header must not be null.");

      byte[] payload = request.getBody();

      if(payload != null && payload.length > 0) {

        MessageRequestBean bean = JSON.parseObject(payload,MessageRequestBean.class);

        Assert.notNull(bean,"deliverer message list request body must not be null.");

        List<DelivererMessageBean> messages = this.delivererService.fetchDelivererMessages(bean.getNamespace(),bean.getPassportId(),bean.getMessageType());

        MessageResponseBean responseBean = MessageResponseBean.builder().messages(messages).build();

        log.info("deliverer message list response: {}" ,JSON.toJSONString(responseBean));

        defaultResponse.setBody(BizResult.builder().code(0).data(responseBean).build().bytes());

        return defaultResponse;

      } else {

        throw new RemotingDelivererException("deliverer message list request body must not be null.");

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
                              .orElse("request ready to deliverer message list exception-ed."))
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
