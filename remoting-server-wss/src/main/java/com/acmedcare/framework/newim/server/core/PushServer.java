package com.acmedcare.framework.newim.server.core;

import com.acmedcare.framework.newim.server.config.PushProperties;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * Push Server
 *
 * @author <a href="mailto:iskp.me@gmail.com">Elve.Xu</a>
 * @version ${project.version} - 09/11/2018.
 */
@Component
public class PushServer {

  private final PushProperties pushProperties;

  @Autowired
  public PushServer(PushProperties pushProperties) {
    this.pushProperties = pushProperties;
  }
}
