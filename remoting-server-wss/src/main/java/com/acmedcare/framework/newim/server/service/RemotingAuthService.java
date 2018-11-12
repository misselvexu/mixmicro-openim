package com.acmedcare.framework.newim.server.service;

import com.acmedcare.framework.aorp.beans.Principal;
import com.acmedcare.framework.aorp.client.AorpClient;
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
   * @param token token
   * @return true/false
   */
  public boolean auth(String token) {
    return aorpClient.validateToken(token);
  }

  /**
   * 获取登录的用户信息
   *
   * @param token 登录票据
   * @return 用户信息
   */
  public Principal principal(String token) {
    return aorpClient.getPrincipal(token);
  }
}
