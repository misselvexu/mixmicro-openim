/*
 * Copyright 1999-2018 Acmedcare+ Holding Ltd.
 * Licensed under the Apache License, Version 2.0 (the "License");
 */

package com.acmedcare.framework.newim.master.connector.processors;

import com.acmedcare.framework.newim.BizResult;
import com.acmedcare.framework.newim.BizResult.ExceptionWrapper;
import com.acmedcare.framework.newim.Message;
import com.acmedcare.framework.newim.Message.GroupMessage;
import com.acmedcare.framework.newim.Message.MQMessage;
import com.acmedcare.framework.newim.Message.MessageType;
import com.acmedcare.framework.newim.Message.SingleMessage;
import com.acmedcare.framework.newim.master.connector.DefaultMasterConnectorContext;
import com.acmedcare.framework.newim.protocol.request.MasterPushMessageHeader;
import com.acmedcare.tiffany.framework.remoting.netty.NettyRequestProcessor;
import com.acmedcare.tiffany.framework.remoting.protocol.RemotingCommand;
import com.alibaba.fastjson.JSON;
import io.netty.channel.ChannelHandlerContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.Assert;

/**
 * MasterPushMessageRequestProcessor
 *
 * @author <a href="mailto:iskp.me@gmail.com">Elve.Xu</a>
 * @version ${project.version} - 2018-12-17.
 */
public class MasterPushMessageRequestProcessor implements NettyRequestProcessor {

  private static final Logger logger =
      LoggerFactory.getLogger(MasterPushMessageRequestProcessor.class);
  private final DefaultMasterConnectorContext context;

  public MasterPushMessageRequestProcessor(DefaultMasterConnectorContext defaultMasterConnectorContext) {
    this.context = defaultMasterConnectorContext;
  }

  @Override
  public RemotingCommand processRequest(
      ChannelHandlerContext channelHandlerContext, RemotingCommand remotingCommand)
      throws Exception {
    logger.info("接收到Master服务器分发消息请求");
    RemotingCommand response =
        RemotingCommand.createResponseCommand(remotingCommand.getCode(), null);

    try {
      MasterPushMessageHeader header =
          (MasterPushMessageHeader)
              remotingCommand.decodeCommandCustomHeader(MasterPushMessageHeader.class);
      Assert.notNull(header, "Master服务器分发消息请求头不能为空");
      logger.info("接收到Master分发消息请求头信息:{}", JSON.toJSONString(header));

      MessageType messageType = header.decodeMessageType();
      logger.info("接收到Master分发消息解析出消息的类型为:{}", messageType);
      byte[] message = remotingCommand.getBody();
      logger.info("接收到Master分发消息解析出的消息内容为:{}", new String(message, Message.DEFAULT_CHARSET));

      switch (messageType) {
        case GROUP:
          GroupMessage groupMessage = JSON.parseObject(message, GroupMessage.class);
          this.context.onMasterMessage(header.getNamespace(), messageType, groupMessage);
          break;
        case SINGLE:
          SingleMessage singleMessage = JSON.parseObject(message, SingleMessage.class);
          this.context.onMasterMessage(header.getNamespace(), messageType, singleMessage);
          break;
        case MQ:
          MQMessage mqMessage = JSON.parseObject(message, MQMessage.class);
          this.context.onMasterMessage(header.getNamespace(), messageType, mqMessage);
          break;
        default:
          logger.warn("un-supported message type :" + messageType);
          break;
      }

      response.setBody(BizResult.builder().code(0).build().bytes());

    } catch (Exception e) {
      // exception
      response.setBody(
          BizResult.builder()
              .code(-1)
              .exception(ExceptionWrapper.builder().message(e.getMessage()).build())
              .build()
              .bytes());
    }
    return response;
  }

  @Override
  public boolean rejectRequest() {
    return false;
  }
}
