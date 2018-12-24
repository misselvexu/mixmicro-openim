package com.acmedcare.framework.remoting.mq.client;

import com.acmedcare.framework.remoting.mq.client.biz.request.AuthRequest;
import com.acmedcare.framework.remoting.mq.client.biz.request.FixTopicMessageListRequest;
import com.acmedcare.framework.remoting.mq.client.biz.request.PullTopicListRequest;
import com.acmedcare.framework.remoting.mq.client.biz.request.PullTopicSubscribedMappingsRequest;
import com.acmedcare.framework.remoting.mq.client.biz.request.SendTopicMessageRequest;
import com.acmedcare.framework.remoting.mq.client.biz.request.SubscribeTopicRequest;
import com.acmedcare.framework.remoting.mq.client.biz.request.SubscribeTopicRequest.Callback;
import com.acmedcare.framework.remoting.mq.client.exception.BizException;
import com.acmedcare.nas.client.NasClient;
import com.acmedcare.nas.client.NasClientFactory;
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

  protected AcmedcareMQRemoting remoting;

  protected NasClient nasClient;

  public BizExecutor(AcmedcareMQRemoting remoting) {
    this.remoting = remoting;

    try {
      if (AcmedcareMQRemoting.getNasProperties() != null) {
        this.nasClient =
            NasClientFactory.createNewNasClient(AcmedcareMQRemoting.getNasProperties());
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
        if (AcmedcareMQRemoting.getAddresses() != null) {
          List<String> addresses = AcmedcareMQRemoting.getAddresses();
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
   * Subscribe Topic (订阅主题)
   *
   * @param request 订阅请求对象
   * @param callback 订阅结果
   * @throws BizException exception
   */
  public abstract void subscribe(SubscribeTopicRequest request, Callback callback)
      throws BizException;

  /**
   * Revoke Subscribe Topic (取消订阅主题)
   *
   * @param request 取消订阅请求
   * @param callback 回调
   * @throws BizException exception
   */
  public abstract void unsubscribe(SubscribeTopicRequest request, Callback callback)
      throws BizException;

  /**
   * Send Topic Message (发送主题队列消息)
   *
   * @param request 发送消息请求
   * @param callback 回调(消息编号)
   * @throws BizException exception
   */
  public abstract void sendTopicMessage(
      SendTopicMessageRequest request, SendTopicMessageRequest.Callback callback)
      throws BizException;

  /**
   * Pull Topic Subscribed Mappings (拉取主题订阅者列表)
   *
   * @param request 拉取请求
   * @param callback 回调(列表)
   * @throws BizException exception
   */
  public abstract void pullTopicSubscribedMappings(
      PullTopicSubscribedMappingsRequest request,
      PullTopicSubscribedMappingsRequest.Callback callback)
      throws BizException;

  /**
   * Pull Topic List (拉取主题列表)
   *
   * @param request 请求
   * @param callback 回调(主题列表)
   * @throws BizException exception
   */
  public abstract void pullAllTopics(
      PullTopicListRequest request, PullTopicListRequest.Callback callback) throws BizException;

  /**
   * Monitor Fix Message Request (修复数据空缺请求)
   *
   * @param request 请求对象
   * @param callback 回调(消息列表)
   * @throws BizException exception
   */
  public abstract void fixTopicMessage(
      FixTopicMessageListRequest request, FixTopicMessageListRequest.Callback callback)
      throws BizException;
}
