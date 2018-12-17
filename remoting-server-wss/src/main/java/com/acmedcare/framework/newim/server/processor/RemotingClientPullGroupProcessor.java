package com.acmedcare.framework.newim.server.processor;

import com.acmedcare.framework.newim.BizResult;
import com.acmedcare.framework.newim.BizResult.ExceptionWrapper;
import com.acmedcare.framework.newim.Group;
import com.acmedcare.framework.newim.server.core.IMSession;
import com.acmedcare.framework.newim.server.core.SessionContextConstants.RemotePrincipal;
import com.acmedcare.framework.newim.server.processor.header.PullGroupHeader;
import com.acmedcare.framework.newim.server.service.GroupService;
import com.acmedcare.tiffany.framework.remoting.protocol.RemotingCommand;
import io.netty.channel.ChannelHandlerContext;
import java.util.List;

/**
 * Pull Owner Group List
 *
 * @author <a href="mailto:iskp.me@gmail.com">Elve.Xu</a>
 * @version v1.0 - 09/08/2018.
 */
public class RemotingClientPullGroupProcessor extends AbstractNormalRequestProcessor {

  private final GroupService groupService;

  public RemotingClientPullGroupProcessor(GroupService groupService, IMSession imSession) {
    super(imSession);
    this.groupService = groupService;
  }

  @Override
  public RemotingCommand processRequest(
      ChannelHandlerContext channelHandlerContext, RemotingCommand remotingCommand)
      throws Exception {

    RemotingCommand response =
        RemotingCommand.createResponseCommand(remotingCommand.getCode(), null);

    try {

      // valid
      RemotePrincipal principal = validatePrincipal(channelHandlerContext.channel());

      PullGroupHeader pullGroupHeader =
          (PullGroupHeader) remotingCommand.decodeCommandCustomHeader(PullGroupHeader.class);
      if (pullGroupHeader == null) {

        response.setBody(
            BizResult.builder()
                .code(-1)
                .exception(
                    ExceptionWrapper.builder()
                        .message("拉取群组会话列表需要参数用户名")
                        .type(NullPointerException.class)
                        .build())
                .build()
                .bytes());

        return response;
      }

      // 查询当前用户名下的群组信息
      List<Group> list =
          this.groupService.queryAccountGroups(
              pullGroupHeader.getNamespace(), pullGroupHeader.getPassportId());

      response.setBody(BizResult.builder().code(0).data(list).build().bytes());

    } catch (Exception e) {
      // set error response
      response.setBody(
          BizResult.builder()
              .code(-1)
              .exception(
                  ExceptionWrapper.builder()
                      .message(e.getMessage())

                      .build())
              .build()
              .bytes());
    }

    return response;
  }

  @Override
  public boolean rejectRequest() {
    return false;
  }
}
