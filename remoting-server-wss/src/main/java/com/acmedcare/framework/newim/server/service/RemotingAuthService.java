package com.acmedcare.framework.newim.server.service;

import com.acmedcare.framework.aorp.beans.Principal;
import com.acmedcare.framework.aorp.client.AorpClient;
import com.acmedcare.framework.newim.server.core.SessionContextConstants.WssPrincipal;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * Remoting Auth Service
 *
 * @author <a href="mailto:iskp.me@gmail.com">Elve.Xu</a>
 * @version ${project.version} - 09/11/2018.
 */
@Component
public class RemotingAuthService {

  private final AorpClient aorpClient;

  @Autowired
  public RemotingAuthService(AorpClient aorpClient) {
    this.aorpClient = aorpClient;
  }

  /**
   * 校验Token是否合法
   *
   * @param token accessToken
   * @return true/false
   */
  public boolean auth(String token) {
    return aorpClient.validateVaguely(token);
  }

  /**
   * 获取登录的用户信息
   *
   * @param token 登录票据
   * @return 用户信息
   */
  public WssPrincipal principal(String token) {
    Principal principal = aorpClient.getPrincipal(token);
    WssPrincipal wssPrincipal = new WssPrincipal();
    BeanUtils.copyProperties(principal, wssPrincipal);
    return wssPrincipal;
  }
}
