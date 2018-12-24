package com.acmedcare.framework.remoting.mq.client.jre;

import com.acmedcare.framework.remoting.mq.client.AcmedcareLogger;
import com.acmedcare.framework.remoting.mq.client.AcmedcareMQRemoting;
import com.acmedcare.framework.remoting.mq.client.BizExecutor;
import com.acmedcare.framework.remoting.mq.client.Constants;
import com.acmedcare.framework.remoting.mq.client.biz.BizCode.Common;
import com.acmedcare.framework.remoting.mq.client.biz.BizCode.MonitorClient;
import com.acmedcare.framework.remoting.mq.client.biz.BizCode.SamplingClient;
import com.acmedcare.framework.remoting.mq.client.biz.BizResult;
import com.acmedcare.framework.remoting.mq.client.biz.bean.Message;
import com.acmedcare.framework.remoting.mq.client.biz.bean.Topic;
import com.acmedcare.framework.remoting.mq.client.biz.body.TopicSubscribeMapping;
import com.acmedcare.framework.remoting.mq.client.biz.request.AuthHeader;
import com.acmedcare.framework.remoting.mq.client.biz.request.AuthRequest;
import com.acmedcare.framework.remoting.mq.client.biz.request.FixTopicMessageListHeader;
import com.acmedcare.framework.remoting.mq.client.biz.request.FixTopicMessageListRequest;
import com.acmedcare.framework.remoting.mq.client.biz.request.NewTopicHeader;
import com.acmedcare.framework.remoting.mq.client.biz.request.NewTopicRequest;
import com.acmedcare.framework.remoting.mq.client.biz.request.PullTopicListHeader;
import com.acmedcare.framework.remoting.mq.client.biz.request.PullTopicListRequest;
import com.acmedcare.framework.remoting.mq.client.biz.request.PullTopicSubscribedMappingsHeader;
import com.acmedcare.framework.remoting.mq.client.biz.request.PullTopicSubscribedMappingsRequest;
import com.acmedcare.framework.remoting.mq.client.biz.request.SendTopicMessageHeader;
import com.acmedcare.framework.remoting.mq.client.biz.request.SendTopicMessageRequest;
import com.acmedcare.framework.remoting.mq.client.biz.request.SubscribeTopicOperateHeader;
import com.acmedcare.framework.remoting.mq.client.biz.request.SubscribeTopicOperateHeader.OperateType;
import com.acmedcare.framework.remoting.mq.client.biz.request.SubscribeTopicRequest;
import com.acmedcare.framework.remoting.mq.client.biz.request.SubscribeTopicRequest.Callback;
import com.acmedcare.framework.remoting.mq.client.exception.BizException;
import com.acmedcare.tiffany.framework.remoting.android.core.InvokeCallback;
import com.acmedcare.tiffany.framework.remoting.android.core.exception.RemotingConnectException;
import com.acmedcare.tiffany.framework.remoting.android.core.exception.RemotingSendRequestException;
import com.acmedcare.tiffany.framework.remoting.android.core.exception.RemotingTimeoutException;
import com.acmedcare.tiffany.framework.remoting.android.core.exception.RemotingTooMuchRequestException;
import com.acmedcare.tiffany.framework.remoting.android.core.protocol.RemotingCommand;
import com.acmedcare.tiffany.framework.remoting.android.core.xlnio.XLMRResponseFuture;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.TypeReference;
import java.util.List;

/**
 * Biz Executor / JRE implements
 *
 * @author <a href="mailto:iskp.me@gmail.com">Elve.Xu</a>
 * @version alpha - 26/07/2018.
 */
public class JREBizExectuor extends BizExecutor {

  public JREBizExectuor(AcmedcareMQRemoting remoting) {
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
    authHeader.setNamespace(
        request.getNamespace() != null ? request.getNamespace() : Constants.DEFAULT_NAMESPACE);
    authHeader.setPassport(request.getUsername());
    authHeader.setAccessToken(request.getAccessToken());
    authHeader.setAreaNo(request.getAreaNo());
    authHeader.setDeviceId(request.getDeviceId());
    authHeader.setOrgId(request.getOrgId());
    authHeader.setPassportId(request.getPassportId());

    AcmedcareLogger.i(this.getClass().getSimpleName(), "授权请求头:" + JSON.toJSONString(authHeader));
    RemotingCommand command =
        RemotingCommand.createRequestCommand(
            this.remoting.isMonitor() ? MonitorClient.REGISTER : SamplingClient.REGISTER,
            authHeader);

    try {
      AcmedcareMQRemoting.getRemotingClient()
          .invokeAsync(
              this.remotingAddress(),
              command,
              requestTimeout,
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
  public void subscribe(SubscribeTopicRequest request, final Callback callback)
      throws BizException {

    if (request == null) {
      throw new BizException("request must not be null.");
    }

    request.validateFields();

    SubscribeTopicOperateHeader header = new SubscribeTopicOperateHeader();
    header.setPassport(request.getPassport());
    header.setPassportId(request.getPassportId());
    header.setOperateType(OperateType.SUBSCRIBE.name());
    header.setTopicIds(request.getTopicIds());

    AcmedcareLogger.i(this.getClass().getSimpleName(), "订阅主题请求头:" + JSON.toJSONString(header));
    RemotingCommand command =
        RemotingCommand.createRequestCommand(MonitorClient.TOPIC_SUBSCRIBE, header);

    try {
      AcmedcareMQRemoting.getRemotingClient()
          .invokeAsync(
              this.remotingAddress(),
              command,
              requestTimeout,
              new InvokeCallback() {
                @Override
                public void operationComplete(XLMRResponseFuture xlmrResponseFuture) {
                  if (xlmrResponseFuture.isSendRequestOK()) {

                    RemotingCommand response = xlmrResponseFuture.getResponseCommand();

                    if (response != null) {

                      BizResult bizResult =
                          BizResult.fromBytes(response.getBody(), BizResult.class);
                      AcmedcareLogger.i(
                          JREBizExectuor.class.getSimpleName(), "订阅主题返回值:" + bizResult.json());

                      if (bizResult.getCode() == 0) {
                        // callback
                        if (callback != null) {
                          callback.onSuccess();
                        }
                      } else {
                        if (callback != null) {
                          callback.onFailed(
                              bizResult.getCode(), bizResult.getException().getMessage());
                        }
                      }
                    } else {
                      if (callback != null) {
                        callback.onException(
                            new BizException("client not received server request response."));
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

      AcmedcareLogger.e(JREBizExectuor.class.getSimpleName(), e, "Sub topics request Failed");
      throw new BizException(e);
    }
  }

  @Override
  public void unsubscribe(SubscribeTopicRequest request, final Callback callback)
      throws BizException {

    if (request == null) {
      throw new BizException("request must not be null.");
    }

    request.validateFields();

    SubscribeTopicOperateHeader header = new SubscribeTopicOperateHeader();
    header.setPassport(request.getPassport());
    header.setPassportId(request.getPassportId());
    header.setOperateType(OperateType.UB_SUBSCRIBE.name());
    header.setTopicIds(request.getTopicIds());

    AcmedcareLogger.i(this.getClass().getSimpleName(), "取消订阅主题请求头:" + JSON.toJSONString(header));
    RemotingCommand command =
        RemotingCommand.createRequestCommand(MonitorClient.REVOKE_TOPIC_SUBSCRIBE, header);

    try {
      AcmedcareMQRemoting.getRemotingClient()
          .invokeAsync(
              this.remotingAddress(),
              command,
              requestTimeout,
              new InvokeCallback() {
                @Override
                public void operationComplete(XLMRResponseFuture xlmrResponseFuture) {
                  if (xlmrResponseFuture.isSendRequestOK()) {

                    RemotingCommand response = xlmrResponseFuture.getResponseCommand();

                    if (response != null) {

                      BizResult bizResult =
                          BizResult.fromBytes(response.getBody(), BizResult.class);
                      AcmedcareLogger.i(
                          JREBizExectuor.class.getSimpleName(), "取消订阅主题返回值:" + bizResult.json());

                      if (bizResult.getCode() == 0) {
                        // callback
                        if (callback != null) {
                          callback.onSuccess();
                        }
                      } else {
                        if (callback != null) {
                          callback.onFailed(
                              bizResult.getCode(), bizResult.getException().getMessage());
                        }
                      }
                    } else {
                      if (callback != null) {
                        callback.onException(
                            new BizException("client not received server request response."));
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

      AcmedcareLogger.e(JREBizExectuor.class.getSimpleName(), e, "Sub topics request Failed");
      throw new BizException(e);
    }
  }

  @Override
  public void sendTopicMessage(
      SendTopicMessageRequest request, final SendTopicMessageRequest.Callback callback)
      throws BizException {

    if (request == null) {
      throw new BizException("request must not be null.");
    }

    request.validateFields();

    SendTopicMessageHeader header = new SendTopicMessageHeader();
    header.setPassport(request.getPassport());
    header.setPassportId(request.getPassportId());
    //header.setContent(request.getContent());
    header.setTopicId(request.getTopicId());
    header.setTopicTag(request.getTopicTag());

    AcmedcareLogger.i(this.getClass().getSimpleName(), "发送主题消息请求头:" + JSON.toJSONString(header));
    RemotingCommand command =
        RemotingCommand.createRequestCommand(SamplingClient.SEND_TOPIC_MESSAGE, header);

    command.setBody(request.getContent());

    try {
      AcmedcareMQRemoting.getRemotingClient()
          .invokeAsync(
              this.remotingAddress(),
              command,
              requestTimeout,
              new InvokeCallback() {
                @Override
                public void operationComplete(XLMRResponseFuture xlmrResponseFuture) {
                  if (xlmrResponseFuture.isSendRequestOK()) {

                    RemotingCommand response = xlmrResponseFuture.getResponseCommand();

                    if (response != null) {

                      BizResult bizResult =
                          BizResult.fromBytes(response.getBody(), BizResult.class);
                      AcmedcareLogger.i(
                          JREBizExectuor.class.getSimpleName(), "发送主题消息返回值:" + bizResult.json());

                      if (bizResult.getCode() == 0 && bizResult.getData() != null) {
                        // callback
                        if (callback != null) {
                          callback.onSuccess(Long.parseLong(bizResult.getData().toString()));
                        }
                      } else {
                        if (callback != null) {
                          callback.onFailed(
                              bizResult.getCode(), bizResult.getException().getMessage());
                        }
                      }
                    } else {
                      if (callback != null) {
                        callback.onException(
                            new BizException("client not received server request response."));
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
          JREBizExectuor.class.getSimpleName(), e, "Send topic message request Failed");
      throw new BizException(e);
    }
  }

  @Override
  public void pullTopicSubscribedMappings(
      PullTopicSubscribedMappingsRequest request,
      final PullTopicSubscribedMappingsRequest.Callback callback)
      throws BizException {

    if (request == null) {
      throw new BizException("request must not be null.");
    }

    request.validateFields();

    PullTopicSubscribedMappingsHeader header = new PullTopicSubscribedMappingsHeader();
    header.setPassport(request.getPassport());
    header.setPassportId(request.getPassportId());

    AcmedcareLogger.i(this.getClass().getSimpleName(), "拉取主题订阅关系请求头:" + JSON.toJSONString(header));
    RemotingCommand command =
        RemotingCommand.createRequestCommand(SamplingClient.PULL_TOPIC_SUBSCRIBE_MAPPING, header);

    try {
      AcmedcareMQRemoting.getRemotingClient()
          .invokeAsync(
              this.remotingAddress(),
              command,
              requestTimeout,
              new InvokeCallback() {
                @Override
                public void operationComplete(XLMRResponseFuture xlmrResponseFuture) {
                  if (xlmrResponseFuture.isSendRequestOK()) {

                    RemotingCommand response = xlmrResponseFuture.getResponseCommand();

                    if (response != null) {

                      BizResult bizResult =
                          BizResult.fromBytes(response.getBody(), BizResult.class);
                      AcmedcareLogger.i(
                          JREBizExectuor.class.getSimpleName(), "拉取主题订阅关系返回值:" + bizResult.json());

                      if (bizResult.getCode() == 0 && bizResult.getData() != null) {

                        Object o = bizResult.getData();

                        List<TopicSubscribeMapping> mappings =
                            JSON.parseObject(
                                JSON.toJSONString(o),
                                new TypeReference<List<TopicSubscribeMapping>>() {});

                        // callback
                        if (callback != null) {
                          callback.onSuccess(mappings);
                        }
                      } else {
                        if (callback != null) {
                          callback.onFailed(
                              bizResult.getCode(), bizResult.getException().getMessage());
                        }
                      }
                    } else {
                      if (callback != null) {
                        callback.onException(
                            new BizException("client not received server request response."));
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
          JREBizExectuor.class.getSimpleName(), e, "Send topic message request Failed");
      throw new BizException(e);
    }
  }

  @Override
  public void pullAllTopics(
      PullTopicListRequest request, final PullTopicListRequest.Callback callback)
      throws BizException {
    if (request == null) {
      throw new BizException("request must not be null.");
    }

    request.validateFields();

    PullTopicListHeader header = new PullTopicListHeader();
    header.setPassport(request.getPassport());
    header.setPassportId(request.getPassportId());

    AcmedcareLogger.i(this.getClass().getSimpleName(), "拉取主题列表请求头:" + JSON.toJSONString(header));
    RemotingCommand command = RemotingCommand.createRequestCommand(Common.PULL_TOPICS, header);

    try {
      AcmedcareMQRemoting.getRemotingClient()
          .invokeAsync(
              this.remotingAddress(),
              command,
              requestTimeout,
              new InvokeCallback() {
                @Override
                public void operationComplete(XLMRResponseFuture xlmrResponseFuture) {
                  if (xlmrResponseFuture.isSendRequestOK()) {

                    RemotingCommand response = xlmrResponseFuture.getResponseCommand();

                    if (response != null) {

                      BizResult bizResult =
                          BizResult.fromBytes(response.getBody(), BizResult.class);
                      AcmedcareLogger.i(
                          JREBizExectuor.class.getSimpleName(), "拉取主题列表返回值:" + bizResult.json());

                      if (bizResult.getCode() == 0 && bizResult.getData() != null) {
                        // success
                        AcmedcareLogger.i(
                            null, "Pull Topic list Succeed , parse topics result list");

                        Object o = bizResult.getData();

                        List<Topic> topics =
                            JSON.parseObject(
                                JSON.toJSONString(o), new TypeReference<List<Topic>>() {});

                        // callback
                        if (callback != null) {
                          callback.onSuccess(topics);
                        }
                      } else {
                        if (callback != null) {
                          callback.onFailed(
                              bizResult.getCode(), bizResult.getException().getMessage());
                        }
                      }
                    } else {
                      if (callback != null) {
                        callback.onException(
                            new BizException("client not received server request response."));
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

      AcmedcareLogger.e(JREBizExectuor.class.getSimpleName(), e, "Pull topics list Request Failed");
      throw new BizException(e);
    }
  }

  @Override
  public void createNewTopic(final NewTopicRequest request, final NewTopicRequest.Callback callback)
      throws BizException {

    if (request == null) {
      throw new BizException("request must not be null.");
    }

    request.validateFields();

    NewTopicHeader header = new NewTopicHeader();
    header.setPassportId(request.getPassportId());
    header.setPassport(request.getPassport());
    header.setTopicName(request.getTopicName());
    header.setTopicTag(request.getTopicTag());
    header.setTopicDesc(request.getTopicDesc());
    header.setTopicExt(request.getTopicExt());

    AcmedcareLogger.i(this.getClass().getSimpleName(), "创建主题请求头:" + JSON.toJSONString(header));
    RemotingCommand command = RemotingCommand.createRequestCommand(Common.CREATE_TOPIC, header);

    try {
      AcmedcareMQRemoting.getRemotingClient()
          .invokeAsync(
              this.remotingAddress(),
              command,
              requestTimeout,
              new InvokeCallback() {
                @Override
                public void operationComplete(XLMRResponseFuture xlmrResponseFuture) {
                  if (xlmrResponseFuture.isSendRequestOK()) {

                    RemotingCommand response = xlmrResponseFuture.getResponseCommand();

                    if (response != null) {

                      BizResult bizResult =
                          BizResult.fromBytes(response.getBody(), BizResult.class);
                      AcmedcareLogger.i(
                          JREBizExectuor.class.getSimpleName(), "创建请求返回值:" + bizResult.json());

                      if (bizResult.getCode() == 0 && bizResult.getData() != null) {
                        // success
                        AcmedcareLogger.i(null, "Create Topic Succeed , parse topic id");

                        Object o = bizResult.getData();

                        // callback
                        if (callback != null) {
                          callback.onSuccess(Long.parseLong(o.toString()));
                        }
                      } else {
                        if (callback != null) {
                          callback.onFailed(
                              bizResult.getCode(), bizResult.getException().getMessage());
                        }
                      }
                    } else {
                      if (callback != null) {
                        callback.onException(
                            new BizException("client not received server request response."));
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

      AcmedcareLogger.e(JREBizExectuor.class.getSimpleName(), e, "Create new topic Request Failed");
      throw new BizException(e);
    }
  }

  @Override
  public void fixTopicMessage(
      FixTopicMessageListRequest request, final FixTopicMessageListRequest.Callback callback)
      throws BizException {

    if (request == null) {
      throw new BizException("request must not be null.");
    }

    request.validateFields();

    FixTopicMessageListHeader header = new FixTopicMessageListHeader();
    header.setPassportId(request.getPassportId());
    header.setPassport(request.getPassport());
    header.setLastTopicMessageId(request.getLastTopicMessageId());
    header.setLimit(request.getLimit());
    header.setTopicId(request.getTopicId());

    AcmedcareLogger.i(this.getClass().getSimpleName(), "补漏主题消息请求头:" + JSON.toJSONString(header));
    RemotingCommand command =
        RemotingCommand.createRequestCommand(MonitorClient.FIX_MESSAGE, header);

    try {
      AcmedcareMQRemoting.getRemotingClient()
          .invokeAsync(
              this.remotingAddress(),
              command,
              requestTimeout,
              new InvokeCallback() {
                @Override
                public void operationComplete(XLMRResponseFuture xlmrResponseFuture) {
                  if (xlmrResponseFuture.isSendRequestOK()) {

                    RemotingCommand response = xlmrResponseFuture.getResponseCommand();

                    if (response != null) {

                      BizResult bizResult =
                          BizResult.fromBytes(response.getBody(), BizResult.class);
                      AcmedcareLogger.i(
                          JREBizExectuor.class.getSimpleName(), "补漏主题消息返回值:" + bizResult.json());

                      if (bizResult.getCode() == 0 && bizResult.getData() != null) {

                        Object o = bizResult.getData();

                        List<Message> messages =
                            JSON.parseObject(
                                JSON.toJSONString(o), new TypeReference<List<Message>>() {});

                        // callback
                        if (callback != null) {
                          callback.onSuccess(messages);
                        }
                      } else {
                        if (callback != null) {
                          callback.onFailed(
                              bizResult.getCode(), bizResult.getException().getMessage());
                        }
                      }
                    } else {
                      if (callback != null) {
                        callback.onException(
                            new BizException("client not received server request response."));
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
          JREBizExectuor.class.getSimpleName(), e, "Fix topic message Request Failed");
      throw new BizException(e);
    }
  }
}
