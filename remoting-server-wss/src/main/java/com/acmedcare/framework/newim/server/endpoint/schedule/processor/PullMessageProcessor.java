/*
 * Copyright 1999-2018 Acmedcare+ Holding Ltd.
 * Licensed under the Apache License, Version 2.0 (the "License");
 */

package com.acmedcare.framework.newim.server.endpoint.schedule.processor;

import com.acmedcare.framework.aorp.beans.Principal;
import com.acmedcare.framework.boot.web.socket.processor.WssSession;
import com.acmedcare.framework.kits.Assert;
import com.acmedcare.framework.newim.Message;
import com.acmedcare.framework.newim.server.endpoint.WssMessageRequestProcessor;
import com.acmedcare.framework.newim.server.endpoint.schedule.ScheduleCommand;
import com.acmedcare.framework.newim.server.endpoint.schedule.ScheduleSysContext;
import com.acmedcare.framework.newim.server.exception.InvalidBizCodeException;
import com.acmedcare.framework.newim.server.exception.InvalidRequestParamsException;
import com.acmedcare.framework.newim.wss.WssPayload;
import com.acmedcare.tiffany.framework.remoting.common.Pair;
import com.acmedcare.tiffany.framework.remoting.common.RemotingHelper;
import org.apache.commons.lang3.StringUtils;

import java.util.List;

import static com.acmedcare.framework.newim.server.ClusterLogger.wssServerLog;

/**
 * {@link PullMessageProcessor}
 *
 * @author <a href="mailto:iskp.me@gmail.com">Elve.Xu</a>
 * @version ${project.version} - 2019-03-14.
 */
public class PullMessageProcessor implements WssMessageRequestProcessor {

  private final ScheduleSysContext context;

  public PullMessageProcessor(ScheduleSysContext context) {
    this.context = context;
  }

  /**
   * Processor Wss Client Request
   *
   * @param session session
   * @param request request
   * @return response
   * @throws Exception exception
   */
  @Override
  public WssPayload.WssResponse processRequest(WssSession session, Object request)
      throws Exception {

    try {

      if (request instanceof ScheduleCommand.PullMessageRequest) {
        ScheduleCommand.PullMessageRequest pullMessageRequest =
            (ScheduleCommand.PullMessageRequest) request;

        if (StringUtils.isAnyBlank(
            pullMessageRequest.getOrgId(),
            pullMessageRequest.getAreaNo(),
            pullMessageRequest.getPassportId(),
            pullMessageRequest.getSender())) {
          throw new InvalidRequestParamsException("请求参数[orgId,areaNo,passportId,sender,]不能为空");
        }

        if (pullMessageRequest.getType() == null) {
          throw new InvalidRequestParamsException("消息类型必须设置");
        }

        if (pullMessageRequest.getLimit() <= 0) {
          pullMessageRequest.setLimit(10);
        }

        wssServerLog.info(
            "[WSS] Schedule web client pull message list params: {}", pullMessageRequest.json());
        Pair<Principal, WssSession> pair =
            context.getLocalSession(Long.parseLong(pullMessageRequest.getPassportId()));

        Assert.notNull(pair, "用户通行证编号不能为空");

        // push biz
        List<? extends Message> messages =
            context.pullMessageList(
                pullMessageRequest.getNamespace(),
                pullMessageRequest.getPassportId(),
                pullMessageRequest.getSender(),
                pullMessageRequest.getType(),
                pullMessageRequest.getLeastMessageId(),
                pullMessageRequest.getLimit());

        wssServerLog.info(
            "[WSS] Schedule web client:{} pull message list succeed.",
            RemotingHelper.parseChannelRemoteAddr(session.channel()));
        return WssPayload.WssResponse.successResponse(
            pullMessageRequest.getBizCode() * -1, messages);

      } else {
        throw new InvalidBizCodeException("无效的请求指令");
      }
    } catch (Exception e) {
      wssServerLog.error("[WSS] Schedule web client pull message list failed ", e);
      return WssPayload.WssResponse.failResponse(
          ScheduleCommand.WS_PULL_MESSAGE.getBizCode() * -1, e.getMessage());
    }
  }
}
