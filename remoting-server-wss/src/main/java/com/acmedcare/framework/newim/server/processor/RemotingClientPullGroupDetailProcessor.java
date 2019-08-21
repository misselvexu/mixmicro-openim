/*
 * Copyright 1999-2018 Acmedcare+ Holding Ltd.
 * Licensed under the Apache License, Version 2.0 (the "License");
 */

package com.acmedcare.framework.newim.server.processor;

import com.acmedcare.framework.newim.BizResult;
import com.acmedcare.framework.newim.BizResult.ExceptionWrapper;
import com.acmedcare.framework.newim.Group;
import com.acmedcare.framework.newim.server.core.IMSession;
import com.acmedcare.framework.newim.server.core.SessionContextConstants.RemotePrincipal;
import com.acmedcare.framework.newim.server.processor.header.PullGroupDetailHeader;
import com.acmedcare.framework.newim.server.service.GroupService;
import com.acmedcare.tiffany.framework.remoting.protocol.RemotingCommand;
import io.netty.channel.ChannelHandlerContext;

/**
 * Pull Group Detail Processor
 *
 * @author <a href="mailto:iskp.me@gmail.com">Elve.Xu</a>
 * @version ${project.version} - 2018-12-05.
 * @since 2.2.0
 */
public class RemotingClientPullGroupDetailProcessor extends AbstractNormalRequestProcessor {

  private final GroupService groupService;

  public RemotingClientPullGroupDetailProcessor(IMSession imSession, GroupService groupService) {
    super(imSession);
    this.groupService = groupService;
  }

  @Override
  public RemotingCommand processRequest(
      ChannelHandlerContext channelHandlerContext, RemotingCommand remotingCommand)
      throws Exception {

    RemotingCommand response =
        RemotingCommand.createResponseCommand(remotingCommand.getCode(), null);

    try {

      // valid
      RemotePrincipal principal = validatePrincipal(channelHandlerContext.channel());

      PullGroupDetailHeader header =
          (PullGroupDetailHeader)
              remotingCommand.decodeCommandCustomHeader(PullGroupDetailHeader.class);

      if (header == null) {

        response.setBody(
            BizResult.builder()
                .code(-1)
                .exception(
                    ExceptionWrapper.builder()
                        .message("拉取群组详情请求头参数异常")
                        .type(NullPointerException.class)
                        .build())
                .build()
                .bytes());

        return response;
      }

      Group group = this.groupService.queryGroupDetail(header.getGroupId(),header.getNamespace());

      response.setBody(BizResult.builder().code(0).data(group).build().bytes());

    } catch (Exception e) {
      // set error response
      response.setBody(
          BizResult.builder()
              .code(-1)
              .exception(
                  ExceptionWrapper.builder()
                      .message(e.getMessage())

                      .build())
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
