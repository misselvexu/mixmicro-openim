/*
 * Copyright 1999-2018 Acmedcare+ Holding Ltd.
 * Licensed under the Apache License, Version 2.0 (the "License");
 */

package com.acmedcare.framework.newim.server.processor.header;

import com.acmedcare.framework.newim.client.MessageConstants;
import com.acmedcare.tiffany.framework.remoting.CommandCustomHeader;
import com.acmedcare.tiffany.framework.remoting.annotation.CFNotNull;
import com.acmedcare.tiffany.framework.remoting.exception.RemotingCommandException;
import lombok.*;

/**
 * {@link ClientMessageAckHeader}
 *
 * @author <a href="mailto:iskp.me@gmail.com">Elve.Xu</a>
 * @version ${project.version} - 2019-08-16.
 */
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ClientMessageAckHeader implements CommandCustomHeader {

  @Builder.Default private String namespace = MessageConstants.DEFAULT_NAMESPACE;

  @CFNotNull private String messageId;

  @CFNotNull private String passportId;

  /**
   * Header Fields Checker
   *
   * @throws RemotingCommandException exception
   */
  @Override
  public void checkFields() throws RemotingCommandException {}
}
