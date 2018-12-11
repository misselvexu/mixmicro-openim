package com.acmedcare.framework.newim.server.processor;

import com.acmedcare.framework.kits.Assert;
import com.acmedcare.framework.newim.BizResult;
import com.acmedcare.framework.newim.BizResult.ExceptionWrapper;
import com.acmedcare.framework.newim.client.bean.Member;
import com.acmedcare.framework.newim.server.core.IMSession;
import com.acmedcare.framework.newim.server.core.SessionContextConstants.RemotePrincipal;
import com.acmedcare.framework.newim.server.processor.header.PullGroupMembersOnlineStatusHeader;
import com.acmedcare.framework.newim.server.service.GroupService;
import com.acmedcare.tiffany.framework.remoting.protocol.RemotingCommand;
import io.netty.channel.ChannelHandlerContext;
import java.util.List;

/**
 * Pull Group Members Online Status Processor
 *
 * @author <a href="mailto:iskp.me@gmail.com">Elve.Xu</a>
 * @version ${project.version} - 2018-12-05.
 * @since 2.2.0
 */
public class RemotingClientPullGroupMembersOnlineStatusProcessor
    extends AbstractNormalRequestProcessor {

  private final GroupService groupService;

  public RemotingClientPullGroupMembersOnlineStatusProcessor(
      GroupService groupService, IMSession imSession) {
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

      PullGroupMembersOnlineStatusHeader header =
          (PullGroupMembersOnlineStatusHeader)
              remotingCommand.decodeCommandCustomHeader(PullGroupMembersOnlineStatusHeader.class);
      if (header == null) {

        response.setBody(
            BizResult.builder()
                .code(-1)
                .exception(
                    ExceptionWrapper.builder()
                        .message("拉取群组成员在线状态请求头参数异常")
                        .type(NullPointerException.class)
                        .build())
                .build()
                .bytes());

        return response;
      }

      List<Member> groupMembers =
          this.groupService.queryGroupMembers(header.getNamespace(), header.getGroupId());
      Assert.isTrue(
          groupMembers != null && !groupMembers.isEmpty(),
          "群组:[" + header.getGroupId() + "]中没有任何成员");

      List<Member> onlineMemberLists =
          this.imSession.getOnlineMemberList(
              header.getNamespace(), groupMembers, header.getGroupId());

      response.setBody(BizResult.builder().code(0).data(onlineMemberLists).build().bytes());

    } catch (Exception e) {
      // set error response
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

  @Override
  public boolean rejectRequest() {
    return false;
  }
}
