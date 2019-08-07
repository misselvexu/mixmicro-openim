/*
 * Copyright 1999-2018 Acmedcare+ Holding Ltd.
 * Licensed under the Apache License, Version 2.0 (the "License");
 */

package com.acmedcare.framework.newim.deliver.context;

import com.acmedcare.framework.newim.deliver.api.exception.RemotingDelivererException;
import io.netty.channel.Channel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.io.Serializable;

/**
 * {@link ConnectorConnection}
 *
 * @author <a href="mailto:iskp.me@gmail.com">Elve.Xu</a>
 * @version ${project.version} - 2019-08-07.
 */
@Getter
@Setter
@Builder
@NoArgsConstructor
public class ConnectorConnection implements Serializable {

  private ConnectorInstance.ConnectorServerInstance serverInstance;

  private Channel channel;

  /**
   * Client connect remoting server instance
   *
   * @throws RemotingDelivererException maybe thrown {@link RemotingDelivererException}
   */
  public void connect()
      throws RemotingDelivererException {

    // TODO

  }

  /**
   * dis-connect remoting server instance's connection
   *
   * @throws RemotingDelivererException maybe thrown {@link RemotingDelivererException}
   */
  public void disconnect()
      throws RemotingDelivererException {

    // TODO

  }


  /**
   * Release remoting server instance's connection
   *
   * @throws RemotingDelivererException maybe thrown {@link RemotingDelivererException}
   */
  public void release()
      throws RemotingDelivererException {

    // TODO

  }
}
