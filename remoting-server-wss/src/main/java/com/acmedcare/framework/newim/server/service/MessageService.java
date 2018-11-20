package com.acmedcare.framework.newim.server.service;

import com.acmedcare.framework.newim.Message;
import com.acmedcare.framework.newim.Message.GroupMessage;
import com.acmedcare.framework.newim.Message.SingleMessage;
import com.acmedcare.framework.newim.server.core.IMSession;
import com.acmedcare.framework.newim.storage.api.MessageRepository;
import com.acmedcare.tiffany.framework.remoting.netty.NettyRemotingSocketClient;
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
  public void pushMessage(IMSession imSession, Message message) {
    // TODO 推送消息

    // 1. (根据消息类型)存储消息
    this.messageRepository.saveMessage(message);

    // 2. 拉取目标对象的服务器地址(Master Server)
    if (message instanceof SingleMessage) {
      // 单聊消息,校验是否接收者是否在本机
      SingleMessage singleMessage = (SingleMessage) message;
      // TODO
      boolean localServer = false;
      if (localServer) {
        // 发送
        imSession.sendMessageToPassport(
            singleMessage.getReceiver(), singleMessage.getMessageType(), singleMessage.bytes());
      } else {
        // 推送消息到对应的转发服务器
        NettyRemotingSocketClient client = imSession.findClientConnectedClusterConnector(singleMessage.getReceiver());

      }

    } else if (message instanceof GroupMessage) {
      // 群组消息
      // 1. 集群群发消息

      // 2. 本机发送群组消息

    }
  }
}
