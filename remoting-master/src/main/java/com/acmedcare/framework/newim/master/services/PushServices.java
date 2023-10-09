package com.acmedcare.framework.newim.master.services;

import com.acmedcare.framework.newim.client.bean.request.PushMessageRequest;
import com.acmedcare.framework.newim.storage.api.MessageRepository;
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


  public void send(PushMessageRequest request) {
    // TODO add send logic
  }
}
