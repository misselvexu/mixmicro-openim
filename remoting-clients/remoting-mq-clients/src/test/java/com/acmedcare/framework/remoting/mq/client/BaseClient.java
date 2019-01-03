package com.acmedcare.framework.remoting.mq.client;

import com.acmedcare.framework.remoting.mq.client.biz.bean.Topic;
import com.acmedcare.framework.remoting.mq.client.biz.body.TopicSubscribeMapping.TopicMapping;
import com.acmedcare.framework.remoting.mq.client.biz.request.NewTopicRequest;
import com.acmedcare.framework.remoting.mq.client.biz.request.PullTopicListRequest;
import com.acmedcare.framework.remoting.mq.client.biz.request.PullTopicSubscribedMappingsRequest;
import com.acmedcare.framework.remoting.mq.client.biz.request.SendTopicMessageRequest;
import com.acmedcare.framework.remoting.mq.client.biz.request.SendTopicMessageRequest.Callback;
import com.acmedcare.framework.remoting.mq.client.biz.request.SubscribeTopicRequest;
import com.acmedcare.framework.remoting.mq.client.exception.BizException;
import com.alibaba.fastjson.JSON;
import java.util.List;

/**
 * BaseClient
 *
 * @author <a href="mailto:iskp.me@gmail.com">Elve.Xu</a>
 * @version ${project.version} - 2018-12-25.
 */
public class BaseClient {
  /**
   * 发送主题消息
   *
   * @param topicId 主题ID
   * @param topicTag 主题标识
   * @param message 消息内容
   */
  static void sendTopicMessage(
      String passport, String passportId, long topicId, String topicTag, String message) {

    SendTopicMessageRequest request = new SendTopicMessageRequest();
    request.setTopicTag(topicTag);
    request.setTopicId(topicId);
    request.setContent(message.getBytes());
    request.setPassport(passport);
    request.setPassportId(passportId);

    AcmedcareMQRemoting.getInstance()
        .executor()
        .sendTopicMessage(
            request,
            new Callback() {
              @Override
              public void onSuccess(Long messageId) {}

              @Override
              public void onFailed(int code, String message) {}

              @Override
              public void onException(BizException e) {}
            });
  }

  /**
   * 创建主题
   *
   * @param topicName 主题名
   * @param topicTag 主题标识
   */
  static void createTopic(String passport, String passportId, String topicName, String topicTag) {
    NewTopicRequest request = new NewTopicRequest();
    request.setPassport(passport);
    request.setPassportId(passportId);
    request.setTopicName(topicName);
    request.setTopicTag(topicTag);
    request.setTopicType("type");

    AcmedcareMQRemoting.getInstance()
        .executor()
        .createNewTopic(
            request,
            new NewTopicRequest.Callback() {
              @Override
              public void onSuccess(Long topicId) {
                System.out.println("主题创建成功,返回值:" + topicId);
              }

              @Override
              public void onFailed(int code, String message) {
                System.out.println("失败,code = " + code + " , message = " + message);
              }

              @Override
              public void onException(BizException e) {
                e.printStackTrace();
              }
            });
  }

  /**
   * 拉取主题订阅关系
   *
   * @param passport
   * @param passportId
   * @param topicId
   */
  static void pullTopicSubscribeMappings(String passport, String passportId, String topicId) {
    PullTopicSubscribedMappingsRequest request = new PullTopicSubscribedMappingsRequest();
    request.setTopicId(topicId);
    request.setPassport(passport);
    request.setPassportId(passportId);

    AcmedcareMQRemoting.getInstance()
        .executor()
        .pullTopicSubscribedMappings(
            request,
            new PullTopicSubscribedMappingsRequest.Callback() {
              @Override
              public void onSuccess(List<TopicMapping> mappings) {
                System.out.println("返回值:" + JSON.toJSONString(mappings));
              }

              @Override
              public void onFailed(int code, String message) {
                System.out.println("失败,code = " + code + " , message = " + message);
              }

              @Override
              public void onException(BizException e) {
                e.printStackTrace();
              }
            });
  }

  /**
   * 拉取主题列表
   *
   * @param passport
   * @param passportId
   */
  static void pullTopics(String passport, String passportId) {
    PullTopicListRequest request = new PullTopicListRequest();
    request.setPassport(passport);
    request.setPassportId(passportId);

    AcmedcareMQRemoting.getInstance()
        .executor()
        .pullAllTopics(
            request,
            new PullTopicListRequest.Callback() {

              @Override
              public void onSuccess(List<Topic> topics) {
                System.out.println("返回值:" + JSON.toJSONString(topics));
              }

              @Override
              public void onFailed(int code, String message) {
                System.out.println("失败,code = " + code + " , message = " + message);
              }

              @Override
              public void onException(BizException e) {
                e.printStackTrace();
              }
            });
  }



  /**
   * 订阅主题
   *
   * @param topicIds 主题编号
   */
  static void subscribeTopic(String passport, String passportId, String topicIds) {
    SubscribeTopicRequest request = new SubscribeTopicRequest();
    request.setPassport(passport);
    request.setPassportId(passportId);
    request.setTopicIds(topicIds.split(","));

    AcmedcareMQRemoting.getInstance()
        .executor()
        .subscribe(
            request,
            new SubscribeTopicRequest.Callback() {
              @Override
              public void onSuccess() {
                System.out.println("订阅成功");
              }

              @Override
              public void onFailed(int code, String message) {
                System.out.println("失败,code = " + code + " , message = " + message);
              }

              @Override
              public void onException(BizException e) {
                e.printStackTrace();
              }
            });
  }

  /**
   * 取消订阅主题
   *
   * @param topicIds 主题编号
   */
  static void unSubscribeTopic(String passport, String passportId, String topicIds) {
    SubscribeTopicRequest request = new SubscribeTopicRequest();
    request.setPassport(passport);
    request.setPassportId(passportId);
    request.setTopicIds(topicIds.split(","));

    AcmedcareMQRemoting.getInstance()
        .executor()
        .unsubscribe(
            request,
            new SubscribeTopicRequest.Callback() {
              @Override
              public void onSuccess() {
                System.out.println("取消订阅成功");
              }

              @Override
              public void onFailed(int code, String message) {
                System.out.println("失败,code = " + code + " , message = " + message);
              }

              @Override
              public void onException(BizException e) {
                e.printStackTrace();
              }
            });
  }
}
