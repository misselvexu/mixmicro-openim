package com.acmedcare.framework.newim.server.endpoint;

import static com.acmedcare.framework.newim.server.ClusterLogger.convertLog;

import com.acmedcare.framework.aorp.beans.Principal;
import com.acmedcare.framework.boot.web.socket.processor.WssSession;
import com.acmedcare.framework.newim.Message.MessageType;
import com.acmedcare.framework.newim.server.core.IMSession;
import com.acmedcare.framework.newim.server.exception.UnauthorizedException;
import com.acmedcare.tiffany.framework.remoting.common.Pair;
import com.alibaba.fastjson.JSON;
import com.google.common.collect.Maps;
import io.netty.util.AttributeKey;
import java.util.List;
import java.util.Map;
import lombok.Getter;

/**
 * Wss Session Context
 *
 * @author <a href="mailto:iskp.me@gmail.com">Elve.Xu</a>
 * @version ${project.version} - 19/11/2018.
 */
public class WssSessionContext {

  /**
   * Login Attribute Key
   *
   * <p>
   */
  private static final AttributeKey<Principal> LOGIN_KEY =
      AttributeKey.newInstance("WSS_ATTR_LOGIN_KEY");

  private static Map<Long, Pair<Principal, WssSession>> onlineWssClientSessions =
      Maps.newConcurrentMap();

  @Getter protected IMSession imSession;

  public WssSessionContext(IMSession imSession) {
    this.imSession = imSession;
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
  public void registerWssClient(Principal principal, WssSession session) {
    // set login status
    session.channel().attr(LOGIN_KEY).set(principal);
    // save cache
    onlineWssClientSessions.put(principal.getPassportUid(), new Pair<>(principal, session));
  }

  public void revokeWssClient(WssSession session) {
    if (session.channel().hasAttr(LOGIN_KEY)) {
      session.channel().attr(LOGIN_KEY).set(null);
    }
  }

  public void auth(WssSession session) {
    if (session.channel().attr(LOGIN_KEY).get() == null) {
      throw new UnauthorizedException();
    }
  }

  public void forwardMessage(List<String> passportIds, Object message) {
    convertLog.info(
        "[WS<->TCP] forward message:{} to passports: {}",
        JSON.toJSONString(message),
        JSON.toJSONString(passportIds));
    imSession.sendMessageToPassport(passportIds, MessageType.SINGLE, JSON.toJSONBytes(message));
  }
}
