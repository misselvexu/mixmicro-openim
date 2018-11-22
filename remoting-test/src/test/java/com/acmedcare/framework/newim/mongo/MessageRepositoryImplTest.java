package com.acmedcare.framework.newim.mongo;

import com.acmedcare.framework.newim.Message.InnerType;
import com.acmedcare.framework.newim.Message.SingleMessage;
import com.acmedcare.framework.newim.TestApplication;
import com.acmedcare.framework.newim.storage.api.MessageRepository;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

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

  @Test
  public void testSaveMessage() throws Exception {

    SingleMessage singleMessage = new SingleMessage();
    singleMessage.setSender("test-sender");
    singleMessage.setBody("content".getBytes());
    singleMessage.setReceiver("test-receiver");
    singleMessage.setInnerType(InnerType.NORMAL);
    singleMessage.setMid(11111L);

    messageRepository.saveMessage(singleMessage);
  }
}
