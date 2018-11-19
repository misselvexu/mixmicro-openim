package com.acmedcare.framework.newim.server.endpoint;

import com.acmedcare.framework.aorp.beans.Principal;
import com.acmedcare.framework.boot.web.socket.processor.WssSession;
import com.acmedcare.tiffany.framework.remoting.common.Pair;
import com.google.common.collect.Maps;
import io.netty.util.AttributeKey;
import java.util.Map;

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
}
