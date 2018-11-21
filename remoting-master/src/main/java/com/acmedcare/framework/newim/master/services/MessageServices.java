package com.acmedcare.framework.newim.master.services;

import com.acmedcare.framework.newim.storage.api.MessageRepository;
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

  @Autowired
  public MessageServices(MessageRepository messageRepository) {
    this.messageRepository = messageRepository;
  }


}
