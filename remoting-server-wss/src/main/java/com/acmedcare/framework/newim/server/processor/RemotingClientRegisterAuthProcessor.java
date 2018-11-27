package com.acmedcare.framework.newim.server.processor;

import static com.acmedcare.framework.newim.server.core.SessionContextConstants.PRINCIPAL_KEY;

import com.acmedcare.framework.aorp.beans.Principal;
import com.acmedcare.framework.aorp.exception.InvalidTokenException;
import com.acmedcare.framework.kits.Assert;
import com.acmedcare.framework.newim.BizResult;
import com.acmedcare.framework.newim.BizResult.ExceptionWrapper;
import com.acmedcare.framework.newim.server.core.IMSession;
import com.acmedcare.framework.newim.server.core.SessionContextConstants.WssPrincipal;
import com.acmedcare.framework.newim.server.processor.header.AuthHeader;
import com.acmedcare.framework.newim.server.service.RemotingAuthService;
import com.acmedcare.tiffany.framework.remoting.protocol.RemotingCommand;
import io.netty.channel.ChannelHandlerContext;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeanUtils;

/**
 * Remoting Client Register Auth Processor
 *
 * @author <a href="mailto:iskp.me@gmail.com">Elve.Xu</a>
 * @version ${project.version} - 12/11/2018.
 */
public class RemotingClientRegisterAuthProcessor extends AbstractNormalRequestProcessor {

  private static final Logger LOG =
      LoggerFactory.getLogger(RemotingClientRegisterAuthProcessor.class);
  private IMSession imSession;
  private RemotingAuthService remotingAuthService;

  public RemotingClientRegisterAuthProcessor(
      IMSession imSession, RemotingAuthService remotingAuthService) {
    super(imSession);
    this.imSession = imSession;
    this.remotingAuthService = remotingAuthService;
  }

  @Override
  public RemotingCommand processRequest(
      ChannelHandlerContext channelHandlerContext, RemotingCommand remotingCommand)
      throws Exception {

    RemotingCommand response =
        RemotingCommand.createResponseCommand(remotingCommand.getCode(), null);

    try {
      AuthHeader authHeader =
          (AuthHeader) remotingCommand.decodeCommandCustomHeader(AuthHeader.class);

      Assert.notNull(authHeader, "登录授权请求参数异常");

      // check accessToken
      boolean result = this.remotingAuthService.auth(authHeader.getAccessToken());
      if (!result) {
        throw new InvalidTokenException("登录票据授权校验失败,无效Token");
      }

      Principal principal = this.remotingAuthService.principal(authHeader.getAccessToken());
      if (!StringUtils.equals(authHeader.getPassportId(), principal.getPassportUid().toString())
          || !StringUtils.equals(authHeader.getPassport(), principal.getPassportAccount())) {
        throw new InvalidTokenException("登录票据与通行证不匹配,非法Token");
      }

      WssPrincipal wssPrincipal = new WssPrincipal();
      BeanUtils.copyProperties(principal, wssPrincipal);
      wssPrincipal.setAreaNo(authHeader.getAreaNo());
      wssPrincipal.setDeviceId(authHeader.getDeviceId());
      wssPrincipal.setOrgId(authHeader.getOrgId());

      // bind
      imSession.bindTcpSession(
          authHeader.getDeviceId(), authHeader.getPassportId(), channelHandlerContext.channel());

      // set session info
      channelHandlerContext.channel().attr(PRINCIPAL_KEY).set(wssPrincipal);

      // return success
      response.setBody(BizResult.SUCCESS.bytes());

    } catch (Exception e) {
      // exception
      response.setBody(
          BizResult.builder()
              .code(-1)
              .exception(
                  ExceptionWrapper.builder()
                      .message(e.getMessage())
                      .type(e.getCause().getClass())
                      .build())
              .build()
              .bytes());
    }

    return response;
  }
}
