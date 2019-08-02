/*
 * Copyright 1999-2018 Acmedcare+ Holding Ltd.
 * Licensed under the Apache License, Version 2.0 (the "License");
 */

package com.acmedcare.framework.newim.master.connector;

import com.acmedcare.tiffany.framework.remoting.netty.NettyClientConfig;
import com.acmedcare.tiffany.framework.remoting.netty.NettyRemotingSocketClient;

/**
 * {@link DelivererMasterConnector}
 *
 * @author <a href="mailto:iskp.me@gmail.com">Elve.Xu</a>
 * @version ${project.version} - 2019-08-02.
 */
public class DelivererMasterConnector extends MasterConnector {

  DelivererMasterConnector(
      MasterConnectorProperties properties, DelivererMasterConnectorContext context) {
    super(properties, context);
  }

  /**
   * Register Master Connector Event Processor
   *
   * @param subscriber event subscribe
   */
  @Override
  protected void registerEvent(MasterConnectorSubscriber subscriber) {

    //

  }

  /**
   * Register Client Processor
   *
   * @param nodeAddress server node address
   * @param config config properties
   * @param client client instance of {@link NettyRemotingSocketClient}
   * @return instance of {@link MasterInstance}
   */
  @Override
  protected MasterInstance registerClientProcessor(
      String nodeAddress, NettyClientConfig config, NettyRemotingSocketClient client) {

    

    return null;
  }
}
