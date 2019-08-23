/*
 * Copyright 1999-2018 Acmedcare+ Holding Ltd.
 * Licensed under the Apache License, Version 2.0 (the "License");
 */

package com.acmedcare.tiffany.framework.remoting.jlib.biz.request;

import com.acmedcare.tiffany.framework.remoting.android.core.CFNotNull;
import com.acmedcare.tiffany.framework.remoting.android.core.exception.RemotingCommandException;
import com.acmedcare.tiffany.framework.remoting.android.core.protocol.CommandCustomHeader;
import com.acmedcare.tiffany.framework.remoting.jlib.Constants;
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

  @Builder.Default private String namespace = Constants.DEFAULT_NAMESPACE;

  @CFNotNull private String messageId;

  @CFNotNull private String passportId;

  /**
   * Header Fields Checker
   *
   * @throws
   *     com.acmedcare.tiffany.framework.remoting.android.core.exception.RemotingCommandException
   *     exception
   */
  @Override
  public void checkFields() throws RemotingCommandException {}
}
