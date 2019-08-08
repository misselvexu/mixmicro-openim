/*
 * Copyright 1999-2018 Acmedcare+ Holding Ltd.
 * Licensed under the Apache License, Version 2.0 (the "License");
 */

package com.acmedcare.framework.newim.deliver.context.processor;

import com.acmedcare.framework.kits.Assert;
import com.acmedcare.framework.newim.BizResult;
import com.acmedcare.framework.newim.deliver.api.header.RegistryHeader;
import com.acmedcare.framework.newim.deliver.api.request.RegistryRequestBean;
import com.acmedcare.framework.newim.deliver.context.ConnectorContext;
import com.acmedcare.framework.newim.deliver.context.ConnectorInstance;
import com.acmedcare.tiffany.framework.remoting.common.RemotingHelper;
import com.acmedcare.tiffany.framework.remoting.netty.NettyRequestProcessor;
import com.acmedcare.tiffany.framework.remoting.protocol.RemotingCommand;
import com.alibaba.fastjson.JSON;
import io.netty.channel.ChannelHandlerContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Optional;

/**
 * {@link RegisterProcessor}
 *
 * @author <a href="mailto:iskp.me@gmail.com">Elve.Xu</a>
 * @version ${project.version} - 2019-07-30.
 */
public class RegisterProcessor implements NettyRequestProcessor {

  private static final Logger log = LoggerFactory.getLogger(RegisterProcessor.class);

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

    log.info("deliverer client :{} request register .", remotingAddr);

    try {

      RegistryHeader header =
          (RegistryHeader) request.decodeCommandCustomHeader(RegistryHeader.class);

      Assert.notNull(header, "register request header must not be null .");

      byte[] payload = request.getBody();

      if (payload != null && payload.length > 0) {

        RegistryRequestBean registryRequestBean = JSON.parseObject(payload, RegistryRequestBean.class);

        // instance
        ConnectorInstance.ConnectorClientInstance clientInstance =
            ConnectorInstance.ConnectorClientInstance.builder()
                .channel(ctx.channel())
                .clientId(registryRequestBean.getServiceId())
                .build();

        // context
        ConnectorContext.context().register(clientInstance);

        // result
        defaultResponse.setBody(BizResult.SUCCESS.bytes());

      } else {
        // empty request body
        defaultResponse.setBody(
            BizResult.builder()
                .code(-1)
                .data("register request param payload is empty .")
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
                              .orElse("deliverer register exception-ed."))
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
