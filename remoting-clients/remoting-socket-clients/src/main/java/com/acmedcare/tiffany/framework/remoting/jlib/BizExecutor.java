package com.acmedcare.tiffany.framework.remoting.jlib;

import com.acmedcare.nas.client.NasClient;
import com.acmedcare.nas.client.NasClientFactory;
import com.acmedcare.tiffany.framework.remoting.android.core.exception.RemotingConnectException;
import com.acmedcare.tiffany.framework.remoting.android.core.exception.RemotingSendRequestException;
import com.acmedcare.tiffany.framework.remoting.android.core.exception.RemotingTimeoutException;
import com.acmedcare.tiffany.framework.remoting.android.core.protocol.RemotingCommand;
import com.acmedcare.tiffany.framework.remoting.jlib.biz.BizCode;
import com.acmedcare.tiffany.framework.remoting.jlib.biz.BizResult;
import com.acmedcare.tiffany.framework.remoting.jlib.biz.request.*;
import com.acmedcare.tiffany.framework.remoting.jlib.exception.BizException;
import com.acmedcare.tiffany.framework.remoting.jlib.jre.JREBizExectuor;
import com.alibaba.fastjson.JSON;

import javax.annotation.Nullable;
import java.util.List;
import java.util.Random;

/**
 * Biz Executor
 *
 * @author <a href="mailto:iskp.me@gmail.com">Elve.Xu</a>
 * @version alpha - 26/07/2018.
 */
public abstract class BizExecutor {

  /** 请求超时时间 */
  protected static long requestTimeout =
      Long.parseLong(System.getProperty("tiffany.quantum.request.timeout", "5000"));

  protected AcmedcareRemoting remoting;

  protected NasClient nasClient;

  public BizExecutor(AcmedcareRemoting remoting) {
    this.remoting = remoting;

    try {
      if (AcmedcareRemoting.getNasProperties() != null) {
        this.nasClient = NasClientFactory.createNewNasClient(AcmedcareRemoting.getNasProperties());
        System.out.println(this.nasClient);
      }
    } catch (Exception e) {
      e.printStackTrace();
      AcmedcareLogger.e(null, e, "Acmedcare Nas Init failed");
    }
  }

  public NasClient nasClient() {
    return this.nasClient;
  }

  protected String remotingAddress() {
    if (this.remoting != null) {
      if (this.remoting.getCurrentRemotingAddress() != null) {
        return this.remoting.getCurrentRemotingAddress();
      } else {
        if (AcmedcareRemoting.getAddresses() != null) {
          List<String> addresses = AcmedcareRemoting.getAddresses();
          if (addresses.size() > 0) {
            this.remoting.setCurrentRemotingAddress(
                addresses.get(new Random(addresses.size()).nextInt()));
            return this.remoting.getCurrentRemotingAddress();
          }
        }
        AcmedcareLogger.w(
            this.getClass().getSimpleName(), "No found remoting address for current request;");
        return null;
      }
    } else {
      throw new BizException("BizExecutor not init-ed with framework.");
    }
  }

  /**
   * 授权 Api
   *
   * @throws BizException exception
   */
  protected abstract void auth(AuthRequest request, AuthRequest.AuthCallback authCallback)
      throws BizException;

  /**
   * 拉取消息列表
   *
   * @param request 请求
   * @param callback 回调(回调为空,事件通知)
   * @throws BizException exception
   * @see
   *     com.acmedcare.tiffany.framework.remoting.jlib.events.AcmedcareEvent.BizEvent#PULL_MESSAGE_RESPONSE
   */
  public abstract <T> void pullMessage(
      PullMessageRequest request, @Nullable PullMessageRequest.Callback<T> callback)
      throws BizException;

  /**
   * 推送消息已读状态
   *
   * @param request 请求
   * @param callback 回调(回调为空,事件通知)
   * @throws BizException exception
   * @see
   *     com.acmedcare.tiffany.framework.remoting.jlib.events.AcmedcareEvent.BizEvent#PUSH_MESSAGE_READ_STATUS_RESPONSE
   */
  public abstract void pushMessageReadStatus(
      PushMessageReadStatusRequest request,
      @Nullable PushMessageReadStatusRequest.Callback callback)
      throws BizException;

  /**
   * 拉取用户群组列表
   *
   * @param request 请求对象
   * @param callback 回调(回调为空,事件通知)
   * @throws BizException exception
   * @see
   *     com.acmedcare.tiffany.framework.remoting.jlib.events.AcmedcareEvent.BizEvent#PULL_GROUPS_LIST_RESPONSE
   */
  public abstract void pullOwnerGroupList(
      PullOwnerGroupListRequest request, PullOwnerGroupListRequest.Callback callback)
      throws BizException;

  /**
   * 发送消息
   *
   * @param request 发送消息请求
   * @param callback 回调(回调为空,事件通知)
   * @throws BizException exception
   * @see
   *     com.acmedcare.tiffany.framework.remoting.jlib.events.AcmedcareEvent.BizEvent#PUSH_MESSAGE_RESPONSE
   */
  public abstract void pushMessage(PushMessageRequest request, PushMessageRequest.Callback callback)
      throws BizException;

  /**
   * 加群/退群操作
   *
   * @param request 请求对象
   * @param callback 回调
   * @throws BizException exception
   */
  public abstract void joinOrLeaveGroup(
      JoinOrLeaveGroupRequest request, JoinOrLeaveGroupRequest.Callback callback)
      throws BizException;

  /**
   * 拉取群组消息已读/未读状态
   *
   * @param request 请求对象
   * @param callback 回调
   * @throws BizException exception
   */
  public abstract void pullGroupMessageReadStatus(
      PullGroupMessageReadStatusRequest request,
      PullGroupMessageReadStatusRequest.Callback callback)
      throws BizException;

  /**
   * 拉取群组人员列表
   *
   * @param request 请求对象
   * @param callback 回调
   * @throws BizException exception
   */
  public abstract void pullGroupMembersList(
      PullGroupMembersRequest request, PullGroupMembersRequest.Callback callback)
      throws BizException;

  /**
   * 拉取群组在线人员列表
   *
   * @param request 请求对象
   * @param callback 回调
   * @throws BizException exception
   */
  public abstract void pullGroupOnlineMembers(
      PullGroupMembersOnlineStatusRequest request,
      PullGroupMembersOnlineStatusRequest.Callback callback)
      throws BizException;

  /**
   * 查询群组详情
   *
   * @param request 请求对象
   * @param callback 回调
   * @throws BizException exception
   */
  public abstract void queryGroupDetail(
      PullGroupDetailRequest request, PullGroupDetailRequest.Callback callback) throws BizException;

  /**
   * 响应消息Ack消息
   *
   * @param namespace 名称空间
   * @param finalReceivedMessageId 消息编号
   * @param passportId 通行证编号
   */
  public final void ack0(String namespace, Long finalReceivedMessageId, Long passportId) {

    ClientMessageAckHeader header =
        ClientMessageAckHeader.builder()
            .namespace(namespace)
            .messageId(finalReceivedMessageId.toString())
            .passportId(passportId.toString())
            .build();

    AcmedcareLogger.i(this.getClass().getSimpleName(), "客户端消息ACK请求头:" + JSON.toJSONString(header));

    RemotingCommand command = RemotingCommand.createRequestCommand(BizCode.CLIENT_RECEIVED_MESSAGE_ACK, header);

    try {
      RemotingCommand response =
          AcmedcareRemoting.getRemotingClient()
              .invokeSync(this.remotingAddress(), command, requestTimeout);

      if (response != null) {

        BizResult bizResult = BizResult.fromBytes(response.getBody(), BizResult.class);
        AcmedcareLogger.i(this.getClass().getSimpleName(), "客户端消息ACK返回值:" + bizResult.json());

        if (bizResult.getCode() != 0) {
          throw new BizException("客户端响应消息Ack失败");
        }
      }

    } catch (InterruptedException
        | RemotingConnectException
        | RemotingTimeoutException
        | RemotingSendRequestException e) {

      AcmedcareLogger.e(JREBizExectuor.class.getSimpleName(), e, "客户端消息ACK请求失败");
      throw new BizException(e);
    }
  }
}
