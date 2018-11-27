package com.acmedcare.tiffany.framework.remoting.jlib.jre;

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
import com.acmedcare.tiffany.framework.remoting.jlib.biz.bean.Session;
import com.acmedcare.tiffany.framework.remoting.jlib.biz.request.AuthHeader;
import com.acmedcare.tiffany.framework.remoting.jlib.biz.request.AuthRequest;
import com.acmedcare.tiffany.framework.remoting.jlib.biz.request.PullGroupHeader;
import com.acmedcare.tiffany.framework.remoting.jlib.biz.request.PullMessageHeader;
import com.acmedcare.tiffany.framework.remoting.jlib.biz.request.PullMessageRequest;
import com.acmedcare.tiffany.framework.remoting.jlib.biz.request.PullOwnerGroupListRequest;
import com.acmedcare.tiffany.framework.remoting.jlib.biz.request.PullSessionHeader;
import com.acmedcare.tiffany.framework.remoting.jlib.biz.request.PullSessionListRequest;
import com.acmedcare.tiffany.framework.remoting.jlib.biz.request.PullSessionStatusHeader;
import com.acmedcare.tiffany.framework.remoting.jlib.biz.request.PullSessionStatusRequest;
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
    authHeader.setUsername(request.getUsername());
    authHeader.setAccessToken(request.getAccessToken());
    authHeader.setAreaNo(request.getAreaNo());
    authHeader.setDeviceId(request.getDeviceId());
    authHeader.setOrgId(request.getOrgId());
    authHeader.setPassportId(request.getPassportId());

    AcmedcareLogger.i(this.getClass().getSimpleName(), "授权请求头:" + JSON.toJSONString(authCallback));
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
                            "Auth Succeed, cached local authed username <"
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
  public void pullMessage(PullMessageRequest request, final PullMessageRequest.Callback callback)
      throws BizException {

    PullMessageHeader header =
        PullMessageHeader.builder()
            .leastMessageId(request.getLeastMessageId())
            .limit(request.getLimit())
            .sender(request.getSender())
            .type(request.getType())
            .username(request.getUsername())
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
                        final List<Message> messages =
                            JSONObject.parseObject(
                                JSON.toJSONString(bizResult.getData()),
                                new TypeReference<List<Message>>() {});

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
            .username(request.getUsername())
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
   * 拉取会话列表
   *
   * @param request 请求
   * @param callback 回调(可为空,为空状态下事件通知)
   * @throws BizException exception
   */
  @Override
  public void pullOwnerSessionList(
      PullSessionListRequest request, final PullSessionListRequest.Callback callback)
      throws BizException {

    PullSessionHeader header = PullSessionHeader.builder().username(request.getUsername()).build();

    AcmedcareLogger.i(this.getClass().getSimpleName(), "拉取会话列表请求头:" + JSON.toJSONString(header));

    RemotingCommand command =
        RemotingCommand.createRequestCommand(BizCode.CLIENT_PULL_OWNER_SESSIONS, header);

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
                          this.getClass().getSimpleName(), "拉取会话列表返回值:" + bizResult.json());

                      if (bizResult.getCode() == 0) {
                        // success
                        final List<Session> sessions =
                            JSONObject.parseObject(
                                JSON.toJSONString(bizResult.getData()),
                                new TypeReference<List<Session>>() {});

                        if (callback != null) {

                          callback.onSuccess(sessions);

                        } else {
                          // push event
                          remoting
                              .eventBus()
                              .post(
                                  new AcmedcareEvent() {
                                    @Override
                                    public Event eventType() {
                                      return BizEvent.PULL_SESSION_LIST_RESPONSE;
                                    }

                                    @Override
                                    public Object data() {
                                      return sessions;
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
          JREBizExectuor.class.getSimpleName(), e, "Pull Session List Request Failed");
      throw new BizException(e);
    }
  }

  @Override
  public void pullSessionStatus(
      PullSessionStatusRequest request, final PullSessionStatusRequest.Callback callback)
      throws BizException {

    PullSessionStatusHeader header =
        PullSessionStatusHeader.builder()
            .flagId(request.getFlagId())
            .type(request.getType())
            .username(request.getUsername())
            .build();

    AcmedcareLogger.i(this.getClass().getSimpleName(), "拉取会话状态详情请求头:" + JSON.toJSONString(header));

    RemotingCommand command =
        RemotingCommand.createRequestCommand(BizCode.CLIENT_PULL_SESSION_STATUS, header);

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
                          this.getClass().getSimpleName(), "拉取会话状态返回值:" + bizResult.json());

                      if (bizResult.getCode() == 0) {
                        // success
                        final Session session = (Session) bizResult.getData();

                        if (callback != null) {

                          callback.onSuccess(session);

                        } else {
                          // push event
                          remoting
                              .eventBus()
                              .post(
                                  new AcmedcareEvent() {
                                    @Override
                                    public Event eventType() {
                                      return BizEvent.PULL_SESSION_LIST_RESPONSE;
                                    }

                                    @Override
                                    public Object data() {
                                      return session;
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
          JREBizExectuor.class.getSimpleName(), e, "Pull Session Status Request Failed");
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
            .username(request.getUsername())
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
            .username(request.getUsername())
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

    command.setBody(request.getMessage().bytes());

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
}
