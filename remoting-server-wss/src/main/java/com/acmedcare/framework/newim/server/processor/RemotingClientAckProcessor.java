/*
 * Copyright 2014-2019 Acmedcare+ Holding Ltd.
 * Licensed under the Apache License, Version 2.0 (the "License");
 */

package com.acmedcare.framework.newim.server.processor;

import com.acmedcare.framework.kits.Assert;
import com.acmedcare.framework.newim.BizResult;
import com.acmedcare.framework.newim.server.core.IMSession;
import com.acmedcare.framework.newim.server.processor.header.ClientMessageAckHeader;
import com.acmedcare.framework.newim.server.service.MessageService;
import com.acmedcare.tiffany.framework.remoting.netty.NettyRequestProcessor;
import com.acmedcare.tiffany.framework.remoting.protocol.RemotingCommand;
import com.alibaba.fastjson.JSON;
import io.netty.channel.ChannelHandlerContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static com.acmedcare.framework.newim.server.ClusterLogger.imServerLog;

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

    imServerLog.info("接收到客户端消息Ack响应");
    RemotingCommand response = RemotingCommand.createResponseCommand(request.getCode(), null);

    try {

      ClientMessageAckHeader header = (ClientMessageAckHeader) request.decodeCommandCustomHeader(ClientMessageAckHeader.class);

      Assert.notNull(header,"客户端Ack消息响应请求头不能为空");

      imServerLog.debug("客户端消息Ack请求参数:{}", JSON.toJSONString(header));

      this.imSession.processClientAck(header.getNamespace(),header.getMessageId(),header.getPassportId());

      response.setBody(BizResult.SUCCESS.bytes());

      return response;

    } catch (Exception e) {
      // exception
      response.setBody(
          BizResult.builder()
              .code(-1)
              .exception(BizResult.ExceptionWrapper.builder().message(e.getMessage()).build())
              .build()
              .bytes());
    }
    return response;
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
