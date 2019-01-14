/*
 * Copyright 1999-2018 Acmedcare+ Holding Ltd.
 * Licensed under the Apache License, Version 2.0 (the "License");
 */

package com.acmedcare.framework.newim.server.mq.processor.header;

import com.acmedcare.tiffany.framework.remoting.CommandCustomHeader;
import com.acmedcare.tiffany.framework.remoting.exception.RemotingCommandException;
import lombok.Getter;
import lombok.Setter;

/**
 * OnTopicRemovedHeader
 *
 * @author <a href="mailto:iskp.me@gmail.com">Elve.Xu</a>
 * @version ${project.version} - 2018-12-29.
 */
@Getter
@Setter
public class OnTopicRemovedHeader implements CommandCustomHeader {

  private Long topicId;

  @Override
  public void checkFields() throws RemotingCommandException {}
}
