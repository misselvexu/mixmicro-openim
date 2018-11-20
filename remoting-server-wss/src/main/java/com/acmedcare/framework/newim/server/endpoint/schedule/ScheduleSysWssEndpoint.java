package com.acmedcare.framework.newim.server.endpoint.schedule;

import static com.acmedcare.framework.newim.protocol.Command.WebSocketClusterCommand.WS_AUTH;
import static com.acmedcare.framework.newim.protocol.Command.WebSocketClusterCommand.WS_ERROR;
import static com.acmedcare.framework.newim.server.ClusterLogger.wssServerLog;

import com.acmedcare.framework.aorp.beans.Principal;
import com.acmedcare.framework.boot.web.socket.annotation.OnClose;
import com.acmedcare.framework.boot.web.socket.annotation.OnError;
import com.acmedcare.framework.boot.web.socket.annotation.OnEvent;
import com.acmedcare.framework.boot.web.socket.annotation.OnMessage;
import com.acmedcare.framework.boot.web.socket.annotation.OnOpen;
import com.acmedcare.framework.boot.web.socket.annotation.ServerEndpoint;
import com.acmedcare.framework.boot.web.socket.processor.WssSession;
import com.acmedcare.framework.newim.server.core.IMSession;
import com.acmedcare.framework.newim.server.endpoint.WssAdapter;
import com.acmedcare.framework.newim.server.endpoint.WssMessageRequestProcessor;
import com.acmedcare.framework.newim.server.exception.UnauthorizedException;
import com.acmedcare.framework.newim.server.service.RemotingAuthService;
import com.acmedcare.framework.newim.wss.WssPayload.WssResponse;
import com.acmedcare.tiffany.framework.remoting.common.Pair;
import com.acmedcare.tiffany.framework.remoting.common.RemotingHelper;
import com.alibaba.fastjson.JSON;
import io.netty.handler.codec.http.HttpHeaders;
import io.netty.handler.timeout.IdleStateEvent;
import java.io.IOException;
import java.util.concurrent.ExecutorService;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
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

  @Autowired
  public ScheduleSysWssEndpoint(
      RemotingAuthService remotingAuthService,
      IMSession imSession,
      ScheduleSysContext scheduleSysContext) {
    super(scheduleSysContext, remotingAuthService, imSession);
  }

  @OnOpen
  public void onOpen(WssSession session, HttpHeaders headers) throws IOException {
    wssServerLog.info(
        "[WSS] Wss Session Create, remoting client :{} ",
        RemotingHelper.parseChannelRemoteAddr(session.channel()));
    try {
      boolean validResult = validateAuth(headers);
      String wssClientType = headers.get(WSS_TYPE);
      wssServerLog.info("[WSS] Remoting Client Type is :{} ", wssClientType);
      if (validResult) {
        wssServerLog.info("[WSS] token is valid,then to get principal detail with token");
        Principal principal = remotingAuthService.principal(parseWssHeaderToken(headers));
        wssServerLog.info("[WSS] principal detail : {}", JSON.toJSONString(principal));
        if (principal != null) {
          wssSessionContext.registerWssClient(principal, session);
          wssServerLog.info(
              "[WSS] Remoting client:{} is connected with auth",
              RemotingHelper.parseChannelRemoteAddr(session.channel()));
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
      wssSessionContext.auth(session);

      if (StringUtils.isAnyBlank(message)) {
        return;
      }
      wssServerLog.info("[WSS] Receive remoting client message: {} ", message);

      ScheduleCommand scheduleCommand = ScheduleCommand.parseCommand(message);

      Pair<WssMessageRequestProcessor, ExecutorService> processor =
          getProcessor(scheduleCommand.getBizCode());
      Object object = scheduleCommand.parseRequest(message);
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
}
