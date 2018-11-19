package com.acmedcare.framework.newim.server.endpoint;

import static com.acmedcare.framework.newim.server.ClusterLogger.wssServerLog;

import com.acmedcare.framework.constants.defined.AuthConstants.AuthHeaders;
import com.acmedcare.framework.newim.server.core.IMSession;
import com.acmedcare.framework.newim.server.exception.UnauthorizedException;
import com.acmedcare.framework.newim.server.service.RemotingAuthService;
import io.netty.handler.codec.http.HttpHeaders;

/**
 * WebSocket & Socket Message Adapter
 *
 * @author <a href="mailto:iskp.me@gmail.com">Elve.Xu</a>
 * @version ${project.version} - 08/11/2018.
 */
public abstract class WssAdapter {

  /**
   * Wss Client Type
   *
   * <p>//TODO 多客户端类型分布处理
   *
   * <p>
   */
  protected static final String WSS_TYPE = "wssClientType";

  protected final WssSessionContext wssSessionContext;
  protected final RemotingAuthService remotingAuthService;
  protected final IMSession imSession;

  public WssAdapter(WssSessionContext wssSessionContext, RemotingAuthService remotingAuthService, IMSession imSession) {
    this.wssSessionContext = wssSessionContext;
    this.remotingAuthService = remotingAuthService;
    this.imSession = imSession;
  }

  protected boolean validateAuth(HttpHeaders headers) {
    try {
      String token = parseWssHeaderToken(headers);
      wssServerLog.info("[WSS] Wss Client With Token: {}", token);
      return remotingAuthService.auth(token);
    } catch (Exception e) {
      throw new UnauthorizedException("WebSocket请求授权校验失败");
    }
  }

  protected String parseWssHeaderToken(HttpHeaders headers) {
    if (headers != null) {
      if (headers.contains(AuthHeaders.AUTHORIZATION_TOKEN)) {
        return headers.get(AuthHeaders.AUTHORIZATION_TOKEN);
      }
      if (headers.contains(AuthHeaders.ACCESS_TOKEN)) {
        return headers.get(AuthHeaders.ACCESS_TOKEN);
      }
    }
    throw new UnauthorizedException("WebSocket请求链接为包含授权头信息参数,[AccessToken,Authorization]");
  }
}
