package com.acmedcare.framework.newim.server.endpoint.schedule;

import static com.acmedcare.framework.newim.protocol.Command.WebSocketClusterCommand.WS_AUTH;
import static com.acmedcare.framework.newim.protocol.Command.WebSocketClusterCommand.WS_ERROR;
import static com.acmedcare.framework.newim.protocol.Command.WebSocketClusterCommand.WS_HEARTBEAT;
import static com.acmedcare.framework.newim.protocol.Command.WebSocketClusterCommand.WS_REGISTER;
import static com.acmedcare.framework.newim.protocol.Command.WebSocketClusterCommand.WS_SHUTDOWN;
import static com.acmedcare.framework.newim.server.ClusterLogger.wssServerLog;

import com.acmedcare.framework.boot.web.socket.annotation.OnClose;
import com.acmedcare.framework.boot.web.socket.annotation.OnError;
import com.acmedcare.framework.boot.web.socket.annotation.OnEvent;
import com.acmedcare.framework.boot.web.socket.annotation.OnMessage;
import com.acmedcare.framework.boot.web.socket.annotation.OnOpen;
import com.acmedcare.framework.boot.web.socket.annotation.ServerEndpoint;
import com.acmedcare.framework.boot.web.socket.processor.WssSession;
import com.acmedcare.framework.newim.server.core.SessionContextConstants.RemotePrincipal;
import com.acmedcare.framework.newim.server.endpoint.WssAdapter;
import com.acmedcare.framework.newim.server.endpoint.WssMessageRequestProcessor;
import com.acmedcare.framework.newim.server.endpoint.schedule.ScheduleCommand.AuthRequest;
import com.acmedcare.framework.newim.server.endpoint.schedule.processor.HeartbeatProcessor;
import com.acmedcare.framework.newim.server.endpoint.schedule.processor.PullOnlineSubOrgsRequestProcessor;
import com.acmedcare.framework.newim.server.endpoint.schedule.processor.PushOrderProcessor;
import com.acmedcare.framework.newim.server.endpoint.schedule.processor.RegisterProcessor;
import com.acmedcare.framework.newim.server.endpoint.schedule.processor.ShutdownProcessor;
import com.acmedcare.framework.newim.server.exception.UnauthorizedException;
import com.acmedcare.framework.newim.wss.WssPayload.WssRequest;
import com.acmedcare.framework.newim.wss.WssPayload.WssResponse;
import com.acmedcare.tiffany.framework.remoting.common.Pair;
import com.acmedcare.tiffany.framework.remoting.common.RemotingHelper;
import com.alibaba.fastjson.JSON;
import io.netty.handler.codec.http.HttpHeaders;
import io.netty.handler.timeout.IdleStateEvent;
import java.io.IOException;
import java.util.concurrent.ExecutorService;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Component;

/**
 * Default Wss Endpoint
 *
 * @author <a href="mailto:iskp.me@gmail.com">Elve.Xu</a>
 * @version ${project.version} - 09/11/2018.
 */
@Component
@ServerEndpoint(prefix = "schedule-sys")
public class ScheduleSysWssEndpoint extends WssAdapter {

  @OnOpen
  public void onOpen(WssSession session, HttpHeaders headers) throws IOException {
    wssServerLog.info(
        "[WSS] Wss Session Create, remoting client :{} ",
        RemotingHelper.parseChannelRemoteAddr(session.channel()));
  }

  private void doAuth(WssSession session, AuthRequest authRequest) {
    try {
      boolean validResult = validateAuth(authRequest.getAccessToken());
      wssServerLog.info("[WSS] Remoting Client Type is :{} ", authRequest.getWssClientType());
      if (validResult) {
        wssServerLog.info("[WSS] token is valid,then to get principal detail with token");
        RemotePrincipal principal = remotingAuthService.principal(authRequest.getAccessToken());
        wssServerLog.info("[WSS] principal detail : {}", JSON.toJSONString(principal));
        if (principal != null) {
          wssSessionContext.registerWssClient(principal, session);
          wssServerLog.info(
              "[WSS] Remoting client:{} is connected with auth",
              RemotingHelper.parseChannelRemoteAddr(session.channel()));
          session.sendText(WssResponse.successResponse(authRequest.getBizCode()).json());
        } else {
          session.sendText(WssResponse.failResponse(WS_AUTH, "无效的登录凭证信息").json());
          session.close();
        }
      } else {
        session.sendText(WssResponse.failResponse(WS_AUTH, "登录票据校验失败,无效凭证").json());
        session.close();
      }
    } catch (Exception e) {
      wssServerLog.error(
          "[WSS] session:{} create connection failed",
          RemotingHelper.parseChannelRemoteAddr(session.channel()),
          e);

      session.sendText(WssResponse.failResponse(WS_AUTH, e.getMessage()).json());
      session.close();
    }
  }

  @OnClose
  public void onClose(WssSession session) throws IOException {
    wssSessionContext.revokeWssClient(session);

    wssServerLog.info(
        "[WSS] Remoting client:{} session is closed.",
        RemotingHelper.parseChannelRemoteAddr(session.channel()));
  }

  @OnError
  public void onError(WssSession session, Throwable throwable) {
    wssServerLog.error("[WSS] Remoting client exception:{}", throwable.getMessage(), throwable);
    session.close();
  }

  @OnMessage
  public void onMessage(WssSession session, String message) {

    try {
      ScheduleCommand scheduleCommand = ScheduleCommand.parseCommand(message);

      try {
        wssSessionContext.auth(session);
      } catch (UnauthorizedException e) {
        WssRequest request = scheduleCommand.parseRequest(message);
        if (request instanceof AuthRequest) {
          AuthRequest authRequest = (AuthRequest) request;
          if (StringUtils.isAnyBlank(
              authRequest.getAccessToken(), authRequest.getWssClientType())) {
            session.sendText(
                WssResponse.failResponse(WS_AUTH, "授权校验参数[accessToken,wssClientType]不能为空").json());
            session.close();
          } else {
            doAuth(session, authRequest);
            return;
          }

        } else {
          throw e;
        }
      }

      wssServerLog.info("[WSS] Receive remoting client message: {} ", message);

      Pair<WssMessageRequestProcessor, ExecutorService> processor =
          getProcessor(scheduleCommand.getBizCode());
      WssRequest object = scheduleCommand.parseRequest(message);
      wssServerLog.info("[WSS] Receive remoting client request: {} ", JSON.toJSONString(object));
      processor
          .getObject2()
          .execute(
              () -> {
                try {
                  WssResponse wssResponse = processor.getObject1().processRequest(session, object);
                  wssServerLog.info(
                      "[WSS] Biz:{} 处理成功,返回值:{}", scheduleCommand.getBizCode(), wssResponse.json());
                  session.sendText(wssResponse.json());
                } catch (Exception e) {
                  session.sendText(WssResponse.failResponse(WS_ERROR, "请求处理失败,重试").json());
                  wssServerLog.info("[WSS] Biz:{} 处理失败", scheduleCommand.getBizCode(), e);
                }
              });

    } catch (UnauthorizedException e) {
      wssServerLog.error("[WSS] Unauthorized Exception");
      session.sendText(WssResponse.failResponse(WS_AUTH, "Unauthorized Exception").json());
    } catch (Exception e) {
      wssServerLog.error("[WSS] Process remoting message failed", e);
      session.sendText(WssResponse.failResponse(WS_ERROR, e.getMessage()).json());
    }
  }

  @OnEvent
  public void onEvent(WssSession session, java.lang.Object evt) {
    if (evt instanceof IdleStateEvent) {
      IdleStateEvent idleStateEvent = (IdleStateEvent) evt;
      switch (idleStateEvent.state()) {
        case READER_IDLE:
          wssServerLog.info(
              "[WSS-EVENT] Session:{} is read idle",
              RemotingHelper.parseChannelRemoteAddr(session.channel()));
          break;
        case WRITER_IDLE:
          wssServerLog.info(
              "[WSS-EVENT] Session:{} is write idle",
              RemotingHelper.parseChannelRemoteAddr(session.channel()));
          break;
        case ALL_IDLE:
          wssServerLog.info(
              "[WSS-EVENT] Session:{} is all idle, server will close this remote session",
              RemotingHelper.parseChannelRemoteAddr(session.channel()));
          session.close();
          break;
      }
    }
  }

  @Override
  public void afterPropertiesSet() throws Exception {

    // 注册处理器
    this.registerProcessor(
        WS_REGISTER, new RegisterProcessor((ScheduleSysContext) wssSessionContext), null);
    // 注销处理器
    this.registerProcessor(
        WS_SHUTDOWN, new ShutdownProcessor((ScheduleSysContext) wssSessionContext), null);
    // 心跳处理器
    this.registerProcessor(
        WS_HEARTBEAT, new HeartbeatProcessor((ScheduleSysContext) wssSessionContext), null);
    // 拉取在线子机构列表
    this.registerProcessor(
        ScheduleCommand.PULL_ONLINE_SUB_ORGS.getBizCode(),
        new PullOnlineSubOrgsRequestProcessor((ScheduleSysContext) wssSessionContext),
        null);

    // 推送订单
    this.registerProcessor(
        ScheduleCommand.PUSH_ORDER.getBizCode(),
        new PushOrderProcessor((ScheduleSysContext) wssSessionContext),
        null);

    wssServerLog.info("[WSS] wss message processors register-ed.");
  }
}
