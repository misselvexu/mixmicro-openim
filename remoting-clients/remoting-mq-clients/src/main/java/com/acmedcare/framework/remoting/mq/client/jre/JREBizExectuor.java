package com.acmedcare.framework.remoting.mq.client.jre;

import com.acmedcare.framework.remoting.mq.client.AcmedcareLogger;
import com.acmedcare.framework.remoting.mq.client.AcmedcareMQRemoting;
import com.acmedcare.framework.remoting.mq.client.BizExecutor;
import com.acmedcare.framework.remoting.mq.client.Constants;
import com.acmedcare.framework.remoting.mq.client.biz.BizCode.MonitorClient;
import com.acmedcare.framework.remoting.mq.client.biz.BizCode.SamplingClient;
import com.acmedcare.framework.remoting.mq.client.biz.BizResult;
import com.acmedcare.framework.remoting.mq.client.biz.request.AuthHeader;
import com.acmedcare.framework.remoting.mq.client.biz.request.AuthRequest;
import com.acmedcare.framework.remoting.mq.client.biz.request.FixTopicMessageListRequest;
import com.acmedcare.framework.remoting.mq.client.biz.request.PullTopicListRequest;
import com.acmedcare.framework.remoting.mq.client.biz.request.PullTopicSubscribedMappingsRequest;
import com.acmedcare.framework.remoting.mq.client.biz.request.SendTopicMessageRequest;
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
  public void subscribe(SubscribeTopicRequest request, Callback callback) throws BizException {}

  @Override
  public void unsubscribe(SubscribeTopicRequest request, Callback callback) throws BizException {}

  @Override
  public void sendTopicMessage(
      SendTopicMessageRequest request, SendTopicMessageRequest.Callback callback)
      throws BizException {}

  @Override
  public void pullTopicSubscribedMappings(
      PullTopicSubscribedMappingsRequest request,
      PullTopicSubscribedMappingsRequest.Callback callback)
      throws BizException {}

  @Override
  public void pullAllTopics(PullTopicListRequest request, PullTopicListRequest.Callback callback)
      throws BizException {}

  @Override
  public void fixTopicMessage(
      FixTopicMessageListRequest request, FixTopicMessageListRequest.Callback callback)
      throws BizException {}
}
