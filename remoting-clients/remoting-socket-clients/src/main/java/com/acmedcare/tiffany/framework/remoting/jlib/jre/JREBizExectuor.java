package com.acmedcare.tiffany.framework.remoting.jlib.jre;

import com.acmedcare.nas.api.NasClientConstants.ResponseCode;
import com.acmedcare.nas.api.entity.UploadEntity;
import com.acmedcare.tiffany.framework.remoting.android.core.InvokeCallback;
import com.acmedcare.tiffany.framework.remoting.android.core.exception.RemotingConnectException;
import com.acmedcare.tiffany.framework.remoting.android.core.exception.RemotingSendRequestException;
import com.acmedcare.tiffany.framework.remoting.android.core.exception.RemotingTimeoutException;
import com.acmedcare.tiffany.framework.remoting.android.core.exception.RemotingTooMuchRequestException;
import com.acmedcare.tiffany.framework.remoting.android.core.protocol.RemotingCommand;
import com.acmedcare.tiffany.framework.remoting.android.core.xlnio.XLMRResponseFuture;
import com.acmedcare.tiffany.framework.remoting.jlib.AcmedcareLogger;
import com.acmedcare.tiffany.framework.remoting.jlib.AcmedcareRemoting;
import com.acmedcare.tiffany.framework.remoting.jlib.BizExecutor;
import com.acmedcare.tiffany.framework.remoting.jlib.biz.BizCode;
import com.acmedcare.tiffany.framework.remoting.jlib.biz.BizResult;
import com.acmedcare.tiffany.framework.remoting.jlib.biz.bean.Group;
import com.acmedcare.tiffany.framework.remoting.jlib.biz.bean.Message;
import com.acmedcare.tiffany.framework.remoting.jlib.biz.bean.Message.CustomMediaPayloadWithExt;
import com.acmedcare.tiffany.framework.remoting.jlib.biz.bean.Message.InnerType;
import com.acmedcare.tiffany.framework.remoting.jlib.biz.bean.Message.MediaPayload;
import com.acmedcare.tiffany.framework.remoting.jlib.biz.request.AuthHeader;
import com.acmedcare.tiffany.framework.remoting.jlib.biz.request.AuthRequest;
import com.acmedcare.tiffany.framework.remoting.jlib.biz.request.JoinOrLeaveGroupHeader;
import com.acmedcare.tiffany.framework.remoting.jlib.biz.request.JoinOrLeaveGroupRequest;
import com.acmedcare.tiffany.framework.remoting.jlib.biz.request.OperateType;
import com.acmedcare.tiffany.framework.remoting.jlib.biz.request.PullGroupHeader;
import com.acmedcare.tiffany.framework.remoting.jlib.biz.request.PullMessageHeader;
import com.acmedcare.tiffany.framework.remoting.jlib.biz.request.PullMessageRequest;
import com.acmedcare.tiffany.framework.remoting.jlib.biz.request.PullOwnerGroupListRequest;
import com.acmedcare.tiffany.framework.remoting.jlib.biz.request.PushMessageHeader;
import com.acmedcare.tiffany.framework.remoting.jlib.biz.request.PushMessageReadStatusRequest;
import com.acmedcare.tiffany.framework.remoting.jlib.biz.request.PushMessageRequest;
import com.acmedcare.tiffany.framework.remoting.jlib.biz.request.PushMessageRequest.Callback;
import com.acmedcare.tiffany.framework.remoting.jlib.biz.request.PushMessageStatusHeader;
import com.acmedcare.tiffany.framework.remoting.jlib.events.AcmedcareEvent;
import com.acmedcare.tiffany.framework.remoting.jlib.exception.BizException;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.alibaba.fastjson.TypeReference;
import java.io.File;
import java.util.List;

/**
 * Biz Executor / JRE implements
 *
 * @author <a href="mailto:iskp.me@gmail.com">Elve.Xu</a>
 * @version alpha - 26/07/2018.
 */
public class JREBizExectuor extends BizExecutor {

  public JREBizExectuor(AcmedcareRemoting remoting) {
    super(remoting);
  }

  /**
   * 授权 Api
   *
   * @throws BizException exception
   */
  @Override
  protected void auth(final AuthRequest request, final AuthRequest.AuthCallback authCallback)
      throws BizException {

    AuthHeader authHeader = new AuthHeader();
    authHeader.setPassport(request.getUsername());
    authHeader.setAccessToken(request.getAccessToken());
    authHeader.setAreaNo(request.getAreaNo());
    authHeader.setDeviceId(request.getDeviceId());
    authHeader.setOrgId(request.getOrgId());
    authHeader.setPassportId(request.getPassportId());

    AcmedcareLogger.i(this.getClass().getSimpleName(), "授权请求头:" + JSON.toJSONString(authHeader));
    RemotingCommand command = RemotingCommand.createRequestCommand(BizCode.CLIENT_AUTH, authHeader);

    try {
      AcmedcareRemoting.getRemotingClient()
          .invokeAsync(
              this.remotingAddress(),
              command,
              5000,
              new InvokeCallback() {
                @Override
                public void operationComplete(XLMRResponseFuture xlmrResponseFuture) {
                  if (xlmrResponseFuture.isSendRequestOK()) {

                    RemotingCommand response = xlmrResponseFuture.getResponseCommand();

                    if (response != null) {

                      BizResult bizResult =
                          BizResult.fromBytes(response.getBody(), BizResult.class);
                      AcmedcareLogger.i(
                          JREBizExectuor.class.getSimpleName(), "授权请求返回值:" + bizResult.json());

                      if (bizResult.getCode() == 0) {
                        // success
                        AcmedcareLogger.i(
                            null, "Auth Succeed , then update local connection status");
                        remoting.updateConnectStatus();

                        AcmedcareLogger.i(
                            null,
                            "Auth Succeed, cached local authed passport <"
                                + request.getUsername()
                                + ">");
                        remoting.setCurrentLoginName(request.getUsername());

                        // authCallback
                        if (authCallback != null) {
                          authCallback.onSuccess();
                        }
                      } else {
                        if (authCallback != null) {
                          authCallback.onFailed(
                              bizResult.getCode(), bizResult.getException().getMessage());
                        }
                      }
                    }
                  }
                }
              });
    } catch (InterruptedException
        | RemotingConnectException
        | RemotingTimeoutException
        | RemotingTooMuchRequestException
        | RemotingSendRequestException e) {

      AcmedcareLogger.e(JREBizExectuor.class.getSimpleName(), e, "Auth Request Failed");
      throw new BizException(e);
    }
  }

  @Override
  public <T> void pullMessage(
      PullMessageRequest request, final PullMessageRequest.Callback<T> callback)
      throws BizException {

    PullMessageHeader header =
        PullMessageHeader.builder()
            .leastMessageId(request.getLeastMessageId())
            .limit(request.getLimit())
            .sender(request.getSender())
            .type(request.getType())
            .passport(request.getUsername())
            .passportId(request.getPassportId())
            .build();

    AcmedcareLogger.i(this.getClass().getSimpleName(), "拉取消息请求头:" + JSON.toJSONString(header));

    RemotingCommand command =
        RemotingCommand.createRequestCommand(BizCode.CLIENT_PULL_MESSAGE, header);

    try {
      AcmedcareRemoting.getRemotingClient()
          .invokeAsync(
              this.remotingAddress(),
              command,
              5000,
              new InvokeCallback() {
                @Override
                public void operationComplete(XLMRResponseFuture xlmrResponseFuture) {
                  if (xlmrResponseFuture.isSendRequestOK()) {

                    RemotingCommand response = xlmrResponseFuture.getResponseCommand();

                    if (response != null) {

                      BizResult bizResult =
                          BizResult.fromBytes(response.getBody(), BizResult.class);
                      AcmedcareLogger.i(
                          this.getClass().getSimpleName(), "拉取消息列表返回值:" + bizResult.json());

                      if (bizResult.getCode() == 0) {
                        // success
                        final List<T> messages =
                            JSONObject.parseObject(
                                JSON.toJSONString(bizResult.getData()),
                                new TypeReference<List<T>>() {});

                        if (callback != null) {

                          callback.onSuccess(messages);

                        } else {

                          // push event
                          remoting
                              .eventBus()
                              .post(
                                  new AcmedcareEvent() {
                                    @Override
                                    public Event eventType() {
                                      return BizEvent.PULL_MESSAGE_RESPONSE;
                                    }

                                    @Override
                                    public Object data() {
                                      return messages;
                                    }
                                  });
                        }
                      } else {
                        if (callback != null) {
                          callback.onFailed(
                              bizResult.getCode(), bizResult.getException().getMessage());
                        }
                      }
                    }
                  }
                }
              });
    } catch (InterruptedException
        | RemotingConnectException
        | RemotingTimeoutException
        | RemotingTooMuchRequestException
        | RemotingSendRequestException e) {

      AcmedcareLogger.e(JREBizExectuor.class.getSimpleName(), e, "Pull Message Request Failed");
      throw new BizException(e);
    }
  }

  @Override
  public void pushMessageReadStatus(
      PushMessageReadStatusRequest request, final PushMessageReadStatusRequest.Callback callback)
      throws BizException {

    PushMessageStatusHeader header =
        PushMessageStatusHeader.builder()
            .leastMessageId(request.getLeastMessageId())
            .pmt(request.getPmt())
            .sender(request.getSender())
            .passport(request.getPassport())
            .passportId(request.getPassportId())
            .build();

    AcmedcareLogger.i(this.getClass().getSimpleName(), "推送消息已读状态请求头:" + JSON.toJSONString(header));

    RemotingCommand command =
        RemotingCommand.createRequestCommand(BizCode.CLIENT_PUSH_MESSAGE_READ_STATUS, header);

    try {
      AcmedcareRemoting.getRemotingClient()
          .invokeAsync(
              this.remotingAddress(),
              command,
              5000,
              new InvokeCallback() {
                @Override
                public void operationComplete(XLMRResponseFuture xlmrResponseFuture) {
                  if (xlmrResponseFuture.isSendRequestOK()) {

                    RemotingCommand response = xlmrResponseFuture.getResponseCommand();

                    if (response != null) {

                      BizResult bizResult =
                          BizResult.fromBytes(response.getBody(), BizResult.class);
                      AcmedcareLogger.i(
                          this.getClass().getSimpleName(), "推送消息已读状态返回值:" + bizResult.json());

                      if (bizResult.getCode() == 0) {
                        // success

                        if (callback != null) {

                          callback.onSuccess();

                        } else {
                          // push event
                          remoting
                              .eventBus()
                              .post(
                                  new AcmedcareEvent() {
                                    @Override
                                    public Event eventType() {
                                      return BizEvent.PUSH_MESSAGE_READ_STATUS_RESPONSE;
                                    }

                                    @Override
                                    public Object data() {
                                      return null;
                                    }
                                  });
                        }
                      } else {
                        if (callback != null) {
                          callback.onFailed(
                              bizResult.getCode(), bizResult.getException().getMessage());
                        }
                      }
                    }
                  }
                }
              });
    } catch (InterruptedException
        | RemotingConnectException
        | RemotingTimeoutException
        | RemotingTooMuchRequestException
        | RemotingSendRequestException e) {

      AcmedcareLogger.e(
          JREBizExectuor.class.getSimpleName(), e, "Push Message Status Request Failed");
      throw new BizException(e);
    }
  }

  /**
   * 拉取用户群组列表
   *
   * @param request 请求对象
   * @param callback 回调(回调为空,事件通知)
   * @throws BizException exception
   * @see AcmedcareEvent.BizEvent#PULL_GROUPS_LIST_RESPONSE
   */
  @Override
  public void pullOwnerGroupList(
      PullOwnerGroupListRequest request, final PullOwnerGroupListRequest.Callback callback)
      throws BizException {

    PullGroupHeader header =
        PullGroupHeader.builder()
            .passport(request.getPassport())
            .passportId(request.getPassportId())
            .build();

    AcmedcareLogger.i(this.getClass().getSimpleName(), "拉取用户群组请求头:" + JSON.toJSONString(header));

    RemotingCommand command =
        RemotingCommand.createRequestCommand(BizCode.CLIENT_PULL_OWNER_GROUPS, header);

    try {
      AcmedcareRemoting.getRemotingClient()
          .invokeAsync(
              this.remotingAddress(),
              command,
              5000,
              new InvokeCallback() {
                @Override
                public void operationComplete(XLMRResponseFuture xlmrResponseFuture) {
                  if (xlmrResponseFuture.isSendRequestOK()) {

                    RemotingCommand response = xlmrResponseFuture.getResponseCommand();

                    if (response != null) {

                      BizResult bizResult =
                          BizResult.fromBytes(response.getBody(), BizResult.class);
                      AcmedcareLogger.i(
                          this.getClass().getSimpleName(), "拉取群组列表返回值:" + bizResult.json());

                      if (bizResult.getCode() == 0) {
                        // success
                        final List<Group> groups =
                            JSONObject.parseObject(
                                JSON.toJSONString(bizResult.getData()),
                                new TypeReference<List<Group>>() {});

                        if (callback != null) {

                          callback.onSuccess(groups);

                        } else {
                          // push event
                          remoting
                              .eventBus()
                              .post(
                                  new AcmedcareEvent() {
                                    @Override
                                    public Event eventType() {
                                      return BizEvent.PULL_GROUPS_LIST_RESPONSE;
                                    }

                                    @Override
                                    public Object data() {
                                      return groups;
                                    }
                                  });
                        }
                      } else {
                        if (callback != null) {
                          callback.onFailed(
                              bizResult.getCode(), bizResult.getException().getMessage());
                        }
                      }
                    }
                  }
                }
              });
    } catch (InterruptedException
        | RemotingConnectException
        | RemotingTimeoutException
        | RemotingTooMuchRequestException
        | RemotingSendRequestException e) {

      AcmedcareLogger.e(JREBizExectuor.class.getSimpleName(), e, "Pull Group List Request Failed");
      throw new BizException(e);
    }
  }

  /**
   * 发送消息
   *
   * @param request 发送消息请求
   * @param callback 回调(回调为空,事件通知)
   * @throws BizException exception
   */
  @Override
  public void pushMessage(final PushMessageRequest request, final Callback callback)
      throws BizException {

    PushMessageHeader header =
        PushMessageHeader.builder()
            .passport(request.getPassport())
            .messageType(request.getMessageType())
            .maxRetryTimes(request.getAttribute().getMaxRetryTimes())
            .passportId(request.getPassportId())
            .persistent(request.getAttribute().isPersistent())
            .qos(request.getAttribute().isQos())
            .retryPeriod(request.getAttribute().getRetryPeriod())
            .build();

    AcmedcareLogger.i(this.getClass().getSimpleName(), "发送消息请求头:" + JSON.toJSONString(header));

    RemotingCommand command =
        RemotingCommand.createRequestCommand(BizCode.CLIENT_PUSH_MESSAGE, header);

    //
    Message message = request.getMessage();

    boolean hasCustomBody = false;
    if (message.getBody() != null && message.getBody().length > 0) {
      hasCustomBody = true;
    }
    if (message.getInnerType().equals(InnerType.MEDIA)) {
      // 媒体消息
      File source = request.getFile();
      if (source != null && source.exists()) {
        String fileName = source.getName();
        String fileSuffix = fileName.substring(fileName.lastIndexOf(".") + 1);
        UploadEntity uploadEntity =
            this.nasClient.upload(fileName, fileSuffix, source, request.getProgressCallback());

        if (uploadEntity.getResponseCode().equals(ResponseCode.UPLOAD_OK)) {
          String fid = uploadEntity.getFid();
          String publicUrl = uploadEntity.getPublicUrl();

          MediaPayload mediaPayload = null;
          if (hasCustomBody) {
            mediaPayload =
                new CustomMediaPayloadWithExt(
                    fid, publicUrl, fileName, fileSuffix, message.getBody());
          } else {
            // build message with payload url
            mediaPayload = new MediaPayload(fid, publicUrl, fileName, fileSuffix);
          }

          // build bytes
          message.setBody(JSON.toJSONBytes(mediaPayload));
          command.setBody(message.bytes());

        } else {
          if (callback != null) {
            callback.onFailed(uploadEntity.getResponseCode().code(), uploadEntity.getMessage());
          }
        }
      }
    } else {
      command.setBody(request.getMessage().bytes());
    }

    try {
      AcmedcareRemoting.getRemotingClient()
          .invokeAsync(
              this.remotingAddress(),
              command,
              5000,
              new InvokeCallback() {
                @Override
                public void operationComplete(XLMRResponseFuture xlmrResponseFuture) {
                  if (xlmrResponseFuture.isSendRequestOK()) {

                    RemotingCommand response = xlmrResponseFuture.getResponseCommand();

                    if (response != null) {

                      BizResult bizResult =
                          BizResult.fromBytes(response.getBody(), BizResult.class);
                      AcmedcareLogger.i(
                          this.getClass().getSimpleName(), "发送消息返回值:" + bizResult.json());

                      if (bizResult.getCode() == 0) {

                        Object o = bizResult.getData();
                        final JSONObject object = JSONObject.parseObject(JSON.toJSONString(o));
                        // success
                        if (callback != null) {

                          callback.onSuccess(object.getLong("mid"));

                        } else {
                          // push event
                          remoting
                              .eventBus()
                              .post(
                                  new AcmedcareEvent() {
                                    @Override
                                    public Event eventType() {
                                      return BizEvent.PUSH_MESSAGE_RESPONSE;
                                    }

                                    @Override
                                    public Object data() {
                                      return object.getLong("mid");
                                    }
                                  });
                        }
                      } else {
                        if (callback != null) {
                          callback.onFailed(
                              bizResult.getCode(), bizResult.getException().getMessage());
                        }
                      }
                    }
                  }
                }
              });
    } catch (InterruptedException
        | RemotingConnectException
        | RemotingTimeoutException
        | RemotingTooMuchRequestException
        | RemotingSendRequestException e) {

      AcmedcareLogger.e(JREBizExectuor.class.getSimpleName(), e, "Push Message Request Failed");
      throw new BizException(e);
    }
  }

  @Override
  public void joinOrLeaveGroup(
      JoinOrLeaveGroupRequest request, final JoinOrLeaveGroupRequest.Callback callback)
      throws BizException {

    JoinOrLeaveGroupHeader header = new JoinOrLeaveGroupHeader();
    header.setGroupId(request.getGroupId());
    header.setOperateType(request.getOperateType());
    header.setPassportId(request.getPassportId());

    int bizCode =
        OperateType.JOIN.equals(request.getOperateType())
            ? BizCode.CLIENT_JOIN_GROUP
            : BizCode.CLIENT_QUIT_GROUP;

    AcmedcareLogger.i(this.getClass().getSimpleName(), "加群/退群操作请求头:" + JSON.toJSONString(header));

    RemotingCommand command = RemotingCommand.createRequestCommand(bizCode, header);

    try {
      AcmedcareRemoting.getRemotingClient()
          .invokeAsync(
              this.remotingAddress(),
              command,
              5000,
              new InvokeCallback() {
                @Override
                public void operationComplete(XLMRResponseFuture xlmrResponseFuture) {
                  if (xlmrResponseFuture.isSendRequestOK()) {

                    RemotingCommand response = xlmrResponseFuture.getResponseCommand();

                    if (response != null) {

                      BizResult bizResult =
                          BizResult.fromBytes(response.getBody(), BizResult.class);
                      AcmedcareLogger.i(
                          this.getClass().getSimpleName(), "加群/退群操作返回值:" + bizResult.json());

                      if (bizResult.getCode() == 0) {
                        // success

                        if (callback != null) {
                          callback.onSuccess();
                        }
                      } else {
                        if (callback != null) {
                          callback.onFailed(
                              bizResult.getCode(), bizResult.getException().getMessage());
                        }
                      }
                    }
                  }
                }
              });
    } catch (InterruptedException
        | RemotingConnectException
        | RemotingTimeoutException
        | RemotingTooMuchRequestException
        | RemotingSendRequestException e) {

      AcmedcareLogger.e(JREBizExectuor.class.getSimpleName(), e, "Group Operate Request Failed");
      throw new BizException(e);
    }
  }
}
