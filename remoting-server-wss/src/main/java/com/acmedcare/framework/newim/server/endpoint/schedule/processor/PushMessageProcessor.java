/*
 * Copyright 1999-2018 Acmedcare+ Holding Ltd.
 * Licensed under the Apache License, Version 2.0 (the "License");
 */

package com.acmedcare.framework.newim.server.endpoint.schedule.processor;

import com.acmedcare.framework.aorp.beans.Principal;
import com.acmedcare.framework.boot.web.socket.processor.WssSession;
import com.acmedcare.framework.kits.Assert;
import com.acmedcare.framework.newim.server.endpoint.WssMessageRequestProcessor;
import com.acmedcare.framework.newim.server.endpoint.schedule.ScheduleCommand;
import com.acmedcare.framework.newim.server.endpoint.schedule.ScheduleSysContext;
import com.acmedcare.framework.newim.server.exception.InvalidBizCodeException;
import com.acmedcare.framework.newim.server.exception.InvalidRequestParamsException;
import com.acmedcare.framework.newim.wss.WssPayload;
import com.acmedcare.tiffany.framework.remoting.common.Pair;
import com.acmedcare.tiffany.framework.remoting.common.RemotingHelper;
import com.google.common.collect.Maps;
import org.apache.commons.lang3.StringUtils;

import java.util.Map;

import static com.acmedcare.framework.newim.server.ClusterLogger.wssServerLog;

/**
 * {@link PushMessageProcessor}
 *
 * @author <a href="mailto:iskp.me@gmail.com">Elve.Xu</a>
 * @version ${project.version} - 2019-03-12.
 */
public class PushMessageProcessor implements WssMessageRequestProcessor {

  private final ScheduleSysContext context;

  public PushMessageProcessor(ScheduleSysContext context) {
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

      if (request instanceof ScheduleCommand.PushMessageRequest) {
        ScheduleCommand.PushMessageRequest pushMessageRequest =
            (ScheduleCommand.PushMessageRequest) request;

        if (StringUtils.isAnyBlank(
            pushMessageRequest.getOrgId(),
            pushMessageRequest.getAreaNo(),
            pushMessageRequest.getPassportId(),
            pushMessageRequest.getMessage(),
            pushMessageRequest.getReceiver(),
            pushMessageRequest.getSender())) {
          throw new InvalidRequestParamsException(
              "请求参数[orgId,areaNo,passportId,message,receiver,sender]不能为空");
        }

        if (pushMessageRequest.getType() == null) {
          throw new InvalidRequestParamsException("消息类型必须设置");
        }

        wssServerLog.info(
            "[WSS] Schedule web client push message params: {}", pushMessageRequest.json());
        Pair<Principal, WssSession> pair =
            context.getLocalSession(Long.parseLong(pushMessageRequest.getPassportId()));

        Assert.notNull(pair, "用户通行证编号不能为空");

        // push biz
        long mid =
            context.pushMessage(
                pushMessageRequest.getNamespace(),
                pushMessageRequest.getAreaNo(),
                pushMessageRequest.getMessage(),
                pushMessageRequest.getReceiver(),
                pushMessageRequest.getSender(),
                pushMessageRequest.getType(),
                pushMessageRequest.getInnerType(),
                pushMessageRequest.getPayload());

        Map<String, Long> result = Maps.newHashMap();
        result.put("mid", mid);
        wssServerLog.info(
            "[WSS] Schedule web client:{} push message succeed.",
            RemotingHelper.parseChannelRemoteAddr(session.channel()));
        return WssPayload.WssResponse.successResponse(pushMessageRequest.getBizCode() * -1, result);

      } else {
        throw new InvalidBizCodeException("无效的请求指令");
      }
    } catch (Exception e) {
      wssServerLog.error("[WSS] Schedule web client push message failed ", e);
      return WssPayload.WssResponse.failResponse(
          ScheduleCommand.WS_PUSH_MESSAGE.getBizCode() * -1, e.getMessage());
    }
  }
}
