package com.acmedcare.framework.newim.master.services;

import com.acmedcare.framework.newim.storage.api.MessageRepository;
import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * Push Service
 *
 * @author <a href="mailto:iskp.me@gmail.com">Elve.Xu</a>
 * @version ${project.version} - 26/11/2018.
 */
@Component
public class PushServices {

  private final MessageRepository messageRepository;

  @Autowired
  public PushServices(MessageRepository messageRepository) {
    this.messageRepository = messageRepository;
  }

  public void sendNotice(
      boolean useTimer,
      String timerExpression,
      String appName,
      String content,
      String action,
      String title,
      String ext,
      List<String> deviceIds) {
    //

  }

  // TODO 推送逻辑

}
