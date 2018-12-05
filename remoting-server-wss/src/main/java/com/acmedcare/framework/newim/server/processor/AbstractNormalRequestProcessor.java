package com.acmedcare.framework.newim.server.processor;

import static com.acmedcare.framework.newim.server.core.SessionContextConstants.PRINCIPAL_KEY;

import com.acmedcare.framework.newim.server.core.IMSession;
import com.acmedcare.framework.newim.server.core.SessionContextConstants.RemotePrincipal;
import com.acmedcare.framework.newim.server.exception.UnauthorizedException;
import com.acmedcare.tiffany.framework.remoting.netty.NettyRequestProcessor;
import io.netty.channel.Channel;
import lombok.Getter;
import lombok.Setter;

/**
 * Abstract Normal Request Processor
 *
 * @author <a href="mailto:iskp.me@gmail.com">Elve.Xu</a>
 * @version ${project.version} - 13/11/2018.
 */
@Getter
@Setter
public abstract class AbstractNormalRequestProcessor implements NettyRequestProcessor {

  protected IMSession imSession;

  public AbstractNormalRequestProcessor(IMSession imSession) {
    this.imSession = imSession;
  }

  protected RemotePrincipal validatePrincipal(Channel channel) {
    if (channel == null) {
      throw new UnauthorizedException("无效的链接对象");
    }
    RemotePrincipal principal = channel.attr(PRINCIPAL_KEY).get();
    if (principal == null) {
      throw new UnauthorizedException("链接未授权异常");
    }
    return principal;
  }

  @Override
  public boolean rejectRequest() {
    return false;
  }
}
