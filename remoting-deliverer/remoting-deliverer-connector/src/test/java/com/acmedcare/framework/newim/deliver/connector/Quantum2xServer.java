/*
 * Copyright 1999-2018 Acmedcare+ Holding Ltd.
 * Licensed under the Apache License, Version 2.0 (the "License");
 */

package com.acmedcare.framework.newim.deliver.connector;

import com.acmedcare.framework.newim.deliver.connector.processor.RegisterProcessor;
import com.acmedcare.framework.remoting.Connection;
import com.acmedcare.framework.remoting.ConnectionEventProcessor;
import com.acmedcare.framework.remoting.ConnectionEventType;
import com.acmedcare.framework.remoting.rpc.RpcServer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * {@link Quantum2xServer}
 *
 * @author <a href="mailto:iskp.me@gmail.com">Elve.Xu</a>
 * @version ${project.version} - 2019-07-25.
 */
public class Quantum2xServer {

  private static final Logger log = LoggerFactory.getLogger(Quantum2xServer.class);

  public static void main(String[] args) {
    //

    RpcServer rpcServer = new RpcServer("0.0.0.0", 7777, true);

    rpcServer.addConnectionEventProcessor(
        ConnectionEventType.CONNECT,
        new ConnectionEventProcessor() {
          @Override
          public void onEvent(String remoteAddress, Connection connection) {

            log.info("Remoting Client: {} is connected .", remoteAddress);
          }
        });

    rpcServer.registerUserProcessor(new RegisterProcessor());

    rpcServer.startup();
  }
}
