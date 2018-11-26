package com.acmedcare.framework.newim.server.processor;

import static com.acmedcare.framework.newim.server.core.SessionContextConstants.PRINCIPAL_KEY;

import com.acmedcare.framework.kits.Assert;
import com.acmedcare.framework.newim.BizResult;
import com.acmedcare.framework.newim.BizResult.ExceptionWrapper;
import com.acmedcare.framework.newim.server.core.IMSession;
import com.acmedcare.framework.newim.server.core.SessionContextConstants.WssPrincipal;
import com.acmedcare.framework.newim.server.exception.UnauthorizedException;
import com.acmedcare.framework.newim.server.processor.header.PullSessionHeader;
import com.acmedcare.tiffany.framework.remoting.protocol.RemotingCommand;
import io.netty.channel.ChannelHandlerContext;
import java.util.List;
import org.apache.commons.lang3.StringUtils;

/**
 * Remoting Client Pull Session Processor
 *
 * @author <a href="mailto:iskp.me@gmail.com">Elve.Xu</a>
 * @version ${project.version} - 13/11/2018.
 */
public class RemotingClientPullSessionProcessor extends AbstractNormalRequestProcessor {

  public RemotingClientPullSessionProcessor(IMSession imSession) {
    super(imSession);
  }

  @Override
  public RemotingCommand processRequest(
      ChannelHandlerContext channelHandlerContext, RemotingCommand remotingCommand)
      throws Exception {

    RemotingCommand response =
        RemotingCommand.createResponseCommand(remotingCommand.getCode(), null);

    try {

      PullSessionHeader pullSessionHeader =
          (PullSessionHeader) remotingCommand.decodeCommandCustomHeader(PullSessionHeader.class);

      Assert.notNull(pullSessionHeader, "拉取会话列表请求参数异常");

      // get principal from session
      WssPrincipal principal = channelHandlerContext.channel().attr(PRINCIPAL_KEY).get();
      if (principal == null) {
        throw new UnauthorizedException("链接未授权异常");
      }

      if (!StringUtils.equals(
          principal.getPassportUid().toString(), pullSessionHeader.getPassportId())) {
        throw new UnauthorizedException("链接授权信息与请求头信息不一致");
      }

      // TODO 拉取 Session 列表
      List<?> sessions = null;

      response.setBody(BizResult.builder().code(0).data(sessions).build().bytes());

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
