package com.acmedcare.framework.newim.master.services;

import static com.acmedcare.framework.newim.MasterLogger.endpointLog;

import com.acmedcare.framework.boot.snowflake.Snowflake;
import com.acmedcare.framework.newim.Message;
import com.acmedcare.framework.newim.Message.GroupMessage;
import com.acmedcare.framework.newim.Message.InnerType;
import com.acmedcare.framework.newim.Message.MessageType;
import com.acmedcare.framework.newim.Message.SingleMessage;
import com.acmedcare.framework.newim.client.MessageAttribute;
import com.acmedcare.framework.newim.master.core.MasterClusterAcceptorServer;
import com.acmedcare.framework.newim.storage.api.GroupRepository;
import com.acmedcare.framework.newim.storage.api.MessageRepository;
import com.google.common.collect.Lists;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * Message Services
 *
 * @author <a href="mailto:iskp.me@gmail.com">Elve.Xu</a>
 * @version ${project.version} - 21/11/2018.
 */
@Component
public class MessageServices {

  private final MessageRepository messageRepository;
  private final GroupRepository groupRepository;
  private final Snowflake snowflake;

  /** Server Instance of {@link MasterClusterAcceptorServer} */
  private final MasterClusterAcceptorServer server;

  @Autowired
  public MessageServices(
      MessageRepository messageRepository,
      MasterClusterAcceptorServer server,
      Snowflake snowflake,
      GroupRepository groupRepository) {
    this.messageRepository = messageRepository;
    this.server = server;
    this.snowflake = snowflake;
    this.groupRepository = groupRepository;
  }

  public void sendMessage(
      MessageAttribute attribute, String sender, String receiver, String type, String content) {

    // 1. build message
    SingleMessage singleMessage = new SingleMessage();
    singleMessage.setNamespace(attribute.getNamespace());
    singleMessage.setReadFlag(false);
    singleMessage.setReceiver(receiver);
    singleMessage.setBody(content.getBytes());
    singleMessage.setInnerType(InnerType.valueOf(type.toUpperCase()));
    singleMessage.setMaxRetryTimes(attribute.getMaxRetryTimes());
    singleMessage.setMessageType(MessageType.SINGLE);
    singleMessage.setQos(attribute.isQos());
    singleMessage.setMid(snowflake.nextId());
    singleMessage.setRetryPeriod(attribute.getRetryPeriod());
    singleMessage.setSender(sender);
    singleMessage.setSendTimestamp(new Date());

    // 2. save
    long result = this.messageRepository.saveMessage(singleMessage);
    endpointLog.info("保存消息到数据库操作,返回值:{}", result);

    // 3. distribute
    this.server.getMasterClusterSession().distributeMessage(attribute, singleMessage);
    endpointLog.info("分发消息任务提交成功");
  }

  public void sendMessage(
      MessageAttribute attribute,
      String sender,
      List<String> receivers,
      String type,
      String content) {

    List<Message> messages = Lists.newArrayList();

    for (String receiver : receivers) {
      // 1. build message
      SingleMessage singleMessage = new SingleMessage();
      singleMessage.setNamespace(attribute.getNamespace());
      singleMessage.setReadFlag(false);
      singleMessage.setReceiver(receiver);
      singleMessage.setBody(content.getBytes());
      singleMessage.setInnerType(InnerType.valueOf(type.toUpperCase()));
      singleMessage.setMaxRetryTimes(attribute.getMaxRetryTimes());
      singleMessage.setMessageType(MessageType.SINGLE);
      singleMessage.setQos(attribute.isQos());
      singleMessage.setMid(snowflake.nextId());
      singleMessage.setRetryPeriod(attribute.getRetryPeriod());
      singleMessage.setSender(sender);
      singleMessage.setSendTimestamp(new Date());
      messages.add(singleMessage);
    }
    // 2. save
    Long[] result = this.messageRepository.batchSaveMessage(messages.toArray(new Message[0]));
    endpointLog.info("批量保存消息到数据库操作,返回值:{}", Arrays.toString(result));

    // 3. distribute
    this.server
        .getMasterClusterSession()
        .batchDistributeMessage(attribute, messages.toArray(new Message[0]));
    endpointLog.info("批量分发消息任务提交成功");
  }

  public void sendGroupMessage(
      MessageAttribute attribute, String sender, String groupId, String type, String content) {

    List<String> memberIds =
        this.groupRepository.queryGroupMemberIds(attribute.getNamespace(), groupId);
    memberIds.remove(sender);
    //
    GroupMessage message = new GroupMessage();
    message.setNamespace(attribute.getNamespace());
    message.setBody(content.getBytes());
    message.setInnerType(InnerType.valueOf(type.toUpperCase()));
    message.setMaxRetryTimes(attribute.getMaxRetryTimes());
    message.setMessageType(MessageType.GROUP);
    message.setQos(attribute.isQos());
    message.setMid(snowflake.nextId());
    message.setRetryPeriod(attribute.getRetryPeriod());
    message.setSender(sender);
    message.setSendTimestamp(new Date());
    message.setGroup(groupId);
    message.setReceivers(memberIds);
    message.setUnReadSize(memberIds.size() - 1 < 0 ? 0 : memberIds.size() - 1);
    message.setReadedSize(0); // 标记自己已读

    long result = this.messageRepository.saveMessage(message);
    endpointLog.info("保存群消息到数据库操作,返回值:{}", result);

    this.server.getMasterClusterSession().distributeMessage(attribute, message);
    endpointLog.info("分发群消息任务提交成功");
  }
}
