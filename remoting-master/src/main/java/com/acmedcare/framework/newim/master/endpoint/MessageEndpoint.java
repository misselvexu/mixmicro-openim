package com.acmedcare.framework.newim.master.endpoint;

import com.acmedcare.framework.newim.master.services.MessageServices;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Message Endpoint
 *
 * @author <a href="mailto:iskp.me@gmail.com">Elve.Xu</a>
 * @version ${project.version} - 21/11/2018.
 */
@RestController
@RequestMapping("/msssage")
public class MessageEndpoint {

  // ========================= Inject Bean Defined =============================

  private final MessageServices messageServices;

  @Autowired
  public MessageEndpoint(MessageServices messageServices) {
    this.messageServices = messageServices;
  }

  // ========================= Request Mapping Method ==========================


}
