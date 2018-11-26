package com.acmedcare.framework.newim.server.service;

import static com.acmedcare.framework.newim.server.ClusterLogger.imServerLog;

import com.acmedcare.framework.newim.Message;
import com.acmedcare.framework.newim.Message.GroupMessage;
import com.acmedcare.framework.newim.Message.SingleMessage;
import com.acmedcare.framework.newim.client.MessageAttribute;
import com.acmedcare.framework.newim.server.core.IMSession;
import com.acmedcare.framework.newim.storage.api.MessageRepository;
import com.alibaba.fastjson.JSON;
import java.util.Arrays;
import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * Message Service
 * <li>Process Client Push Message
 * <li>Process WebEndpoint Send Message
 *
 * @author <a href="mailto:iskp.me@gmail.com">Elve.Xu</a>
 * @version ${project.version} - 13/11/2018.
 */
@Component
public class MessageService {

  private final MessageRepository messageRepository;

  @Autowired
  public MessageService(MessageRepository messageRepository) {
    this.messageRepository = messageRepository;
  }

  /**
   * Push Message
   *
   * @param message message
   * @see Message
   * @see Message.SingleMessage
   * @see Message.GroupMessage
   */
  public void processMessage(IMSession imSession, Message message) {
    // 1. (根据消息类型)存储消息
    this.messageRepository.saveMessage(message);
    imServerLog.info("消息,ID:{},内容:{},存储成功", message.getMid(), JSON.toJSONString(message));

    MessageAttribute attribute = MessageAttribute.builder().build();
    // 2. 拉取目标对象的服务器地址(Master Server)
    if (message instanceof SingleMessage) {
      // 单聊消息,校验是否接收者是否在本机
      SingleMessage singleMessage = (SingleMessage) message;
      attribute.setMaxRetryTimes(singleMessage.getMaxRetryTimes());
      attribute.setPersistent(singleMessage.isPersistent());
      attribute.setQos(singleMessage.isQos());
      attribute.setRetryPeriod(singleMessage.getRetryPeriod());

    } else if (message instanceof GroupMessage) {
      // 群组消息
      GroupMessage groupMessage = (GroupMessage) message;
      attribute.setMaxRetryTimes(groupMessage.getMaxRetryTimes());
      attribute.setPersistent(groupMessage.isPersistent());
      attribute.setQos(groupMessage.isQos());
      attribute.setRetryPeriod(groupMessage.getRetryPeriod());
    }

    // 分发
    imServerLog.info("提交分发消息到其他的通讯服务器任务");
    imSession.distributeMessage(attribute, message);

    imServerLog.info("本机尝试推送消息给客户端");
    // 本机发送
    switch (message.getMessageType()) {
      case SINGLE:
        try {
          SingleMessage singleMessage = (SingleMessage) message;
          imSession.sendMessageToPassport(
              singleMessage.getReceiver(), singleMessage.getMessageType(), message.bytes());
          imServerLog.info(
              "本机发送单聊消息到客户端完成, 消息编号:{} , 接收人:{}",
              singleMessage.getMid(),
              singleMessage.getReceiver());
        } catch (Exception e) {
          imServerLog.error("本机发送单聊消息(ID:" + message.getMid() + ")到客户端异常", e);
        }
        break;
      case GROUP:
        try {
          GroupMessage groupMessage = (GroupMessage) message;
          imSession.sendMessageToPassport(
              groupMessage.getReceivers(), groupMessage.getMessageType(), message.bytes());
          imServerLog.info(
              "本机批量发送单聊消息到客户端完成, 消息编号:{} , 接收人列表:{}",
              groupMessage.getMid(),
              Arrays.toString(groupMessage.getReceivers().toArray()));
        } catch (Exception e) {
          imServerLog.error("本机批量发送单聊消息(ID:" + message.getMid() + ")到客户端异常", e);
        }
        break;
    }
  }

  public List<Message> queryAccountGroupMessages(
      String username, String sender, int type, long leastMessageId, long limit) {

    // TODO 查询消息

    return null;
  }
}
