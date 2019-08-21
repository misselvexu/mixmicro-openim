/*
 * Copyright 1999-2018 Acmedcare+ Holding Ltd.
 * Licensed under the Apache License, Version 2.0 (the "License");
 */

package com.acmedcare.framework.newim.deliver.context.processor;

import com.acmedcare.framework.kits.Assert;
import com.acmedcare.framework.newim.BizResult;
import com.acmedcare.framework.newim.deliver.api.request.TimedDelivererMessageRequestBean;
import com.acmedcare.tiffany.framework.remoting.common.RemotingHelper;
import com.acmedcare.tiffany.framework.remoting.protocol.RemotingCommand;
import com.alibaba.fastjson.JSON;
import io.netty.channel.ChannelHandlerContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Optional;

/**
 * {@link TimedDeliveryMessageProcessor} , Deliverer server timed distribute time-message
 *
 * @author <a href="mailto:iskp.me@gmail.com">Elve.Xu</a>
 * @version ${project.version} - 2019-08-08.
 */
public class TimedDeliveryMessageProcessor extends AbstractProcessor {

  private static final Logger log = LoggerFactory.getLogger(TimedDeliveryMessageProcessor.class);

  @Override
  public RemotingCommand processRequest(ChannelHandlerContext ctx, RemotingCommand request)
      throws Exception {

    RemotingCommand defaultResponse = RemotingCommand.createResponseCommand(request.getCode(), "");

    String remotingAddr = RemotingHelper.parseChannelRemoteAddr(ctx.channel());

    log.info("[==] deliverer server :{} pushed timed deliverer message(s).", remotingAddr);

    try {

      byte[] payload  = request.getBody();

      if(payload != null && payload.length > 0) {

        TimedDelivererMessageRequestBean bean = JSON.parseObject(payload,TimedDelivererMessageRequestBean.class);

        Assert.notNull(bean,"Timed deliverer message bean must not be null .");

        // process
        this.delivererService.postTimerDelivererMessage(bean.getMessages());

        // succeed
        defaultResponse.setBody(BizResult.SUCCESS.bytes());

        return defaultResponse;

      } else {
        throw new IllegalArgumentException("[xx] timed deliverer message request payload bean is invalid.");
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
                              .orElse("request revoker deliverer message exception-ed."))
                      .build())
              .build()
              .bytes());
    }

    // return response
    return defaultResponse;
  }
}
