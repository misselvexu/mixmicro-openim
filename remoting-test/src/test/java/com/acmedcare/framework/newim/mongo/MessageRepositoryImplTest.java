package com.acmedcare.framework.newim.mongo;

import com.acmedcare.framework.boot.snowflake.Snowflake;
import com.acmedcare.framework.newim.Message;
import com.acmedcare.framework.newim.Message.GroupMessage;
import com.acmedcare.framework.newim.Message.InnerType;
import com.acmedcare.framework.newim.Message.MessageType;
import com.acmedcare.framework.newim.Message.SingleMessage;
import com.acmedcare.framework.newim.TestApplication;
import com.acmedcare.framework.newim.storage.api.MessageRepository;
import com.alibaba.fastjson.JSON;
import com.google.common.collect.Lists;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import java.util.Date;
import java.util.List;

/**
 * Message Repository Test
 *
 * @author <a href="mailto:iskp.me@gmail.com">Elve.Xu</a>
 * @version ${project.version} - 13/11/2018.
 */
@RunWith(SpringRunner.class)
@SpringBootTest(classes = TestApplication.class)
public class MessageRepositoryImplTest {

  @Autowired private MessageRepository messageRepository;

  @Autowired private Snowflake snowflake;

  @Test
  public void testSaveMessage() throws Exception {

    SingleMessage singleMessage = new SingleMessage();
    singleMessage.setSender("test-sender");
    singleMessage.setBody("content".getBytes());
    singleMessage.setReceiver("test-receiver");
    singleMessage.setInnerType(InnerType.NORMAL);
    singleMessage.setMessageType(MessageType.SINGLE);
    singleMessage.setMid(snowflake.nextId());
    singleMessage.setSendTimestamp(new Date());

    //
    System.out.println(JSON.toJSONString(singleMessage));

    messageRepository.saveMessage(singleMessage);
  }

  @Test
  public void testBatchSaveMessage() throws Exception {

    Message[] messages = new Message[10];

    for (int i = 0; i < 5; i++) {
      SingleMessage singleMessage = new SingleMessage();
      singleMessage.setSender("13910187666");
      singleMessage.setBody("content".getBytes());
      singleMessage.setReceiver("13910187669");
      singleMessage.setInnerType(InnerType.NORMAL);
      singleMessage.setMessageType(MessageType.SINGLE);
      singleMessage.setMid(snowflake.nextId());
      singleMessage.setSendTimestamp(new Date());
      messages[i] = singleMessage;
    }

    List<String> receivers = Lists.newArrayList();
    receivers.add("13910187666");
    receivers.add("13910187669");
    for (int i = 5; i < 10; i++) {
      GroupMessage groupMessage = new GroupMessage();
      groupMessage.setGroup("gid-20181122");
      groupMessage.setSender("13910187666"); // 发送人
      groupMessage.setReceivers(receivers);
      groupMessage.setUnReadSize(2);
      groupMessage.setBody("group-content".getBytes());
      groupMessage.setInnerType(InnerType.COMMAND);
      groupMessage.setMessageType(MessageType.GROUP);
      groupMessage.setMid(snowflake.nextId());
      groupMessage.setSendTimestamp(new Date());
      messages[i] = groupMessage;
    }
    System.out.println(JSON.toJSONString(messages));

    this.messageRepository.batchSaveMessage(messages);
  }

  @Test
  public void testQueryNewestSingleMessageList() {

    List<? extends Message> messages =
        this.messageRepository.querySingleMessages(
            "DEFAULT", "13910187666", "13910187669", 5, true, -1);

    Assert.assertEquals(5, messages.size());
  }

  @Test
  public void testQueryHistorySingleMessageList() {

    List<? extends Message> messages =
        this.messageRepository.querySingleMessages(
            "DEFAULT", "13910187666", "13910187669", 10, false, 1034352586066177L);

    Assert.assertEquals(1, messages.size());
  }

  @Test
  public void testQueryNewestGroupMessageList() {

    List<? extends Message> messages =
        this.messageRepository.queryGroupMessages(
            "DEFAULT", "gid-20181122", "13910187669", 5, true, -1);

    Assert.assertEquals(5, messages.size());
  }

  @Test
  public void testQueryHistoryGroupMessageList() {

    List<? extends Message> messages =
        this.messageRepository.queryGroupMessages(
            "DEFAULT", "gid-20181122", "13910187669", 5, false, 1034352586115330L);

    Assert.assertEquals(2, messages.size());
  }

  @Test
  public void updateGroupMessageReadStatus() {

    // pushGroupMessageReadStatus gid-20181122 1047261835348992
    this.messageRepository.updateGroupMessageReadStatus(
        "3837142362366977", "gid-20181122", "1047261835348992", new Date());
  }
}
