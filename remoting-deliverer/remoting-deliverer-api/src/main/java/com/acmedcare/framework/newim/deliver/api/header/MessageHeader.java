/*
 * Copyright 1999-2018 Acmedcare+ Holding Ltd.
 * Licensed under the Apache License, Version 2.0 (the "License");
 */

package com.acmedcare.framework.newim.deliver.api.header;

import com.acmedcare.tiffany.framework.remoting.CommandCustomHeader;
import com.acmedcare.tiffany.framework.remoting.annotation.CFNotNull;
import com.acmedcare.tiffany.framework.remoting.exception.RemotingCommandException;
import lombok.*;

/**
 * {@link MessageHeader}
 *
 * @author <a href="mailto:iskp.me@gmail.com">Elve.Xu</a>
 * @version ${project.version} - 2019-08-14.
 */
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MessageHeader implements CommandCustomHeader {

  /**
   * Request Timestamp
   *
   * <p>
   */
  @Builder.Default @CFNotNull private long timestamp = System.currentTimeMillis();

  // ==== check fields method ====

  @Override
  public void checkFields() throws RemotingCommandException {}

}
