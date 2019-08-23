/*
 * Copyright 1999-2018 Acmedcare+ Holding Ltd.
 * Licensed under the Apache License, Version 2.0 (the "License");
 */

package com.acmedcare.framework.newim.master.connector.processors;

import com.acmedcare.framework.newim.BizResult;
import com.acmedcare.framework.newim.BizResult.ExceptionWrapper;
import com.acmedcare.framework.newim.master.connector.DefaultMasterConnectorContext;
import com.acmedcare.framework.newim.protocol.request.MasterNoticeSessionDataBody;
import com.acmedcare.tiffany.framework.remoting.netty.NettyRequestProcessor;
import com.acmedcare.tiffany.framework.remoting.protocol.RemotingCommand;
import com.alibaba.fastjson.JSON;
import io.netty.channel.ChannelHandlerContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * MasterNoticeClientChannelsRequestProcessor
 *
 * @author <a href="mailto:iskp.me@gmail.com">Elve.Xu</a>
 * @version ${project.version} - 2018-12-17.
 */
public class MasterNoticeClientChannelsRequestProcessor implements NettyRequestProcessor {

  private static final Logger logger =
      LoggerFactory.getLogger(MasterNoticeClientChannelsRequestProcessor.class);

  private final DefaultMasterConnectorContext context;

  public MasterNoticeClientChannelsRequestProcessor(DefaultMasterConnectorContext defaultMasterConnectorContext) {
    this.context = defaultMasterConnectorContext;
  }

  @Override
  public RemotingCommand processRequest(
      ChannelHandlerContext channelHandlerContext, RemotingCommand remotingCommand)
      throws Exception {

    logger.info("接收到Master服务器分发全局链接");
    RemotingCommand response =
        RemotingCommand.createResponseCommand(remotingCommand.getCode(), null);

    try {

      byte[] sessions = remotingCommand.getBody();
      logger.debug("接受到的同步的数据为:{}", new String(sessions, "UTF-8"));
      MasterNoticeSessionDataBody noticeSessionDataBody =
          JSON.parseObject(sessions, MasterNoticeSessionDataBody.class);

      this.context.diff(
          noticeSessionDataBody.getPassportsConnections(),
          noticeSessionDataBody.getDevicesConnections());

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
