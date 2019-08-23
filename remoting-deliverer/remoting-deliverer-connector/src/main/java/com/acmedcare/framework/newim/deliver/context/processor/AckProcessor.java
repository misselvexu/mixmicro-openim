/*
 * Copyright 1999-2018 Acmedcare+ Holding Ltd.
 * Licensed under the Apache License, Version 2.0 (the "License");
 */

package com.acmedcare.framework.newim.deliver.context.processor;

import com.acmedcare.framework.kits.Assert;
import com.acmedcare.framework.newim.BizResult;
import com.acmedcare.framework.newim.deliver.api.exception.RemotingDelivererException;
import com.acmedcare.framework.newim.deliver.api.request.CommitDelivererAckMessageRequestBean;
import com.acmedcare.tiffany.framework.remoting.common.RemotingHelper;
import com.acmedcare.tiffany.framework.remoting.protocol.RemotingCommand;
import com.alibaba.fastjson.JSON;
import io.netty.channel.ChannelHandlerContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Optional;

/**
 * {@link AckProcessor}
 *
 * @author <a href="mailto:iskp.me@gmail.com">Elve.Xu</a>
 * @version ${project.version} - 2019-08-20.
 */
public class AckProcessor extends AbstractProcessor {

  private static final Logger log = LoggerFactory.getLogger(AckProcessor.class);

  /**
   * Process Request
   *
   * @param ctx     channel handler context
   * @param request request command
   * @return process result (if udp request processor ,will return null)
   * @throws Exception exception
   */
  @Override
  public RemotingCommand processRequest(ChannelHandlerContext ctx, RemotingCommand request) throws Exception {

    RemotingCommand defaultResponse = RemotingCommand.createResponseCommand(request.getCode(), "");

    String remotingAddr = RemotingHelper.parseChannelRemoteAddr(ctx.channel());

    log.info("deliverer client :{} request commit ack deliverer message .", remotingAddr);

    try {

      byte[] payload = request.getBody();

      if(payload != null && payload.length > 0) {

        CommitDelivererAckMessageRequestBean bean = JSON.parseObject(payload,CommitDelivererAckMessageRequestBean.class);

        Assert.notNull(bean,"deliverer ack message request body must not be null.");

        this.delivererService.commitDelivererAckMessage(bean.getNamespace(),bean.getPassportId(),bean.getMessageId());

        defaultResponse.setBody(BizResult.builder().code(0).build().bytes());

        return defaultResponse;

      } else {

        throw new RemotingDelivererException("deliverer ack message request body must not be null.");

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
                              .orElse("deliverer ack message process exception-ed."))
                      .build())
              .build()
              .bytes());
    }

    // return response
    return defaultResponse;
  }
}
