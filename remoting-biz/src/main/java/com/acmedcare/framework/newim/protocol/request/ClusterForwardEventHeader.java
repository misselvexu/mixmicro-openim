/*
 * Copyright 2014-2019 Acmedcare+ Holding Ltd.
 * Licensed under the Apache License, Version 2.0 (the "License");
 */

package com.acmedcare.framework.newim.protocol.request;

import com.acmedcare.tiffany.framework.remoting.CommandCustomHeader;
import com.acmedcare.tiffany.framework.remoting.annotation.CFNotNull;
import com.acmedcare.tiffany.framework.remoting.exception.RemotingCommandException;
import lombok.Getter;
import lombok.Setter;

/**
 * ClusterForwardEventHeader
 *
 * @author <a href="mailto:iskp.me@gmail.com">Elve.Xu</a>
 * @version ${project.version} - 2019-01-15.
 */
@Getter
@Setter
public class ClusterForwardEventHeader implements CommandCustomHeader {

  @CFNotNull private String eventName;

  @Override
  public void checkFields() throws RemotingCommandException {}
}
