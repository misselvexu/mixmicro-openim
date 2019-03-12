package com.acmedcare.framework.newim.server.endpoint;

import com.acmedcare.framework.aorp.beans.Principal;
import com.acmedcare.framework.boot.web.socket.processor.WssSession;
import com.acmedcare.framework.newim.Message;
import com.acmedcare.framework.newim.Message.MessageType;
import com.acmedcare.framework.newim.protocol.Command.WebSocketClusterCommand;
import com.acmedcare.framework.newim.server.core.IMSession;
import com.acmedcare.framework.newim.server.core.SessionContextConstants;
import com.acmedcare.framework.newim.server.core.SessionContextConstants.RemotePrincipal;
import com.acmedcare.framework.newim.server.exception.UnauthorizedException;
import com.acmedcare.framework.newim.wss.WssPayload.WssMessage;
import com.acmedcare.tiffany.framework.remoting.common.Pair;
import com.alibaba.fastjson.JSON;
import com.google.common.collect.Maps;
import lombok.Getter;

import java.util.List;
import java.util.Map;

import static com.acmedcare.framework.newim.server.ClusterLogger.convertLog;

/**
 * Wss Session Context
 *
 * @author <a href="mailto:iskp.me@gmail.com">Elve.Xu</a>
 * @version ${project.version} - 19/11/2018.
 */
public class WssSessionContext {

  private static Map<Long, Pair<Principal, WssSession>> onlineWssClientSessions =
      Maps.newConcurrentMap();

  @Getter protected IMSession imSession;

  public WssSessionContext(IMSession imSession) {
    this.imSession = imSession;
    this.imSession.registerWssSessionContext(this);
  }

  public Pair<Principal, WssSession> getLocalSession(Long passportId) {
    return onlineWssClientSessions.get(passportId);
  }

  /**
   * Register Login-ed Wss Client
   *
   * @param principal principal detail
   * @param session session channel
   */
  public void registerWssClient(RemotePrincipal principal, WssSession session) {
    // set login status
    session.channel().attr(SessionContextConstants.PRINCIPAL_KEY).set(principal);
    // save cache
    onlineWssClientSessions.put(principal.getPassportUid(), new Pair<>(principal, session));
  }

  /**
   * 发送消息到Web客户端
   *
   * @param passportIds 通行证列表
   * @param message 消息
   */
  public void sendMessageToPassports(List<String> passportIds, byte[] message) {

    for (String passportId : passportIds) {

      try {
        Pair<Principal, WssSession> pair = onlineWssClientSessions.get(Long.parseLong(passportId));
        if (pair != null) {
          convertLog.info(
              "[TCP-WS] [准备发送消息]  {}, 通行证ID:{},登录名:{}",
              new String(message),
              passportId,
              pair.getObject1().getPassportAccount());

          pair.getObject2()
              .sendText(
                  WssMessage.builder()
                      .bizCode(WebSocketClusterCommand.WS_PUSH_MESSAGE)
                      .message(new String(message, Message.DEFAULT_CHARSET))
                      .build()
                      .json());

          convertLog.info(
              "[TCP-WS] [消息发送成功]  {}, 通行证ID:{},登录名:{}",
              new String(message),
              passportId,
              pair.getObject1().getPassportAccount());
        }
      } catch (Exception e) {
        convertLog.error("[TCP-WS] [消息发送失败]  {}, 通行证ID:{}", new String(message), passportId);
      }
    }
  }

  public void revokeWssClient(WssSession session) {
    if (session.channel().hasAttr(SessionContextConstants.PRINCIPAL_KEY)) {
      session.channel().attr(SessionContextConstants.PRINCIPAL_KEY).set(null);
    }
  }

  public void auth(WssSession session) {
    if (session.channel().attr(SessionContextConstants.PRINCIPAL_KEY).get() == null) {
      throw new UnauthorizedException();
    }
  }

  public void forwardMessage(String namespace, List<String> passportIds, Object message) {
    convertLog.info(
        "[WS<->TCP] forward message:{} to passports: {}",
        JSON.toJSONString(message),
        JSON.toJSONString(passportIds));
    imSession.sendMessageToPassport(
        namespace, passportIds, MessageType.SINGLE, JSON.toJSONBytes(message));



  }
}
