package com.acmedcare.tiffany.framework.remoting.jlib;

import com.acmedcare.nas.client.NasClient;
import com.acmedcare.nas.client.NasClientFactory;
import com.acmedcare.tiffany.framework.remoting.jlib.biz.request.AuthRequest;
import com.acmedcare.tiffany.framework.remoting.jlib.biz.request.PullMessageRequest;
import com.acmedcare.tiffany.framework.remoting.jlib.biz.request.PullOwnerGroupListRequest;
import com.acmedcare.tiffany.framework.remoting.jlib.biz.request.PushMessageReadStatusRequest;
import com.acmedcare.tiffany.framework.remoting.jlib.biz.request.PushMessageRequest;
import com.acmedcare.tiffany.framework.remoting.jlib.exception.BizException;
import java.util.List;
import java.util.Random;
import javax.annotation.Nullable;

/**
 * Biz Executor
 *
 * @author <a href="mailto:iskp.me@gmail.com">Elve.Xu</a>
 * @version alpha - 26/07/2018.
 */
public abstract class BizExecutor {

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
}
