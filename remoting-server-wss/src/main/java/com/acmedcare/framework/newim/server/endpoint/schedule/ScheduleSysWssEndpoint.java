package com.acmedcare.framework.newim.server.endpoint.schedule;

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
import com.acmedcare.framework.newim.server.service.RemotingAuthService;
import com.acmedcare.framework.newim.wss.WssPayload.WssResponse;
import com.acmedcare.tiffany.framework.remoting.common.RemotingHelper;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import io.netty.handler.codec.http.HttpHeaders;
import io.netty.handler.timeout.IdleStateEvent;
import java.io.IOException;
import java.util.concurrent.ExecutorService;
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

  /**
   * Register Processor
   *
   * @param bizCode biz code
   * @param processor processor
   * @param executorService executor
   */
  public void registerProcessor(
      int bizCode, WssMessageRequestProcessor processor, ExecutorService executorService) {
    ((ScheduleSysContext) wssSessionContext).registerProcessor(bizCode, processor, executorService);
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
          session.sendText(WssResponse.authFailedPayload("无效的登录凭证信息").json());
          session.close();
        }
      } else {
        session.sendText(WssResponse.authFailedPayload("登录票据校验失败,无效凭证").json());
        session.close();
      }
    } catch (Exception e) {
      wssServerLog.error(
          "[WSS] session:{} create connection failed",
          RemotingHelper.parseChannelRemoteAddr(session.channel()),
          e);

      session.sendText(WssResponse.authFailedPayload(e.getMessage()).json());
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
    wssServerLog.info("[WSS] Receive remoting client message: {} ", message);
    JSONObject receivedMessage = JSONObject.parseObject(message);
  }

  @OnEvent
  public void onEvent(WssSession session, Object evt) {
    if (evt instanceof IdleStateEvent) {
      IdleStateEvent idleStateEvent = (IdleStateEvent) evt;
      switch (idleStateEvent.state()) {
        case READER_IDLE:
          System.out.println("read idle");
          break;
        case WRITER_IDLE:
          System.out.println("write idle");
          break;
        case ALL_IDLE:
          System.out.println("all idle");
          break;
        default:
          break;
      }
    }
  }
}
