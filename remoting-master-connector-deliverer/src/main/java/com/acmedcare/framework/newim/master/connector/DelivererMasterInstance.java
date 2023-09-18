/*
 * Copyright 1999-2018 Acmedcare+ Holding Ltd.
 * Licensed under the Apache License, Version 2.0 (the "License");
 */

package com.acmedcare.framework.newim.master.connector;

import com.acmedcare.framework.newim.InstanceNode;
import com.acmedcare.framework.newim.protocol.request.ClusterRegisterHeader;
import com.acmedcare.tiffany.framework.remoting.netty.NettyClientConfig;
import com.acmedcare.tiffany.framework.remoting.netty.NettyRemotingSocketClient;

/**
 * {@link DelivererMasterInstance}
 *
 * @author <a href="mailto:iskp.me@gmail.com">Elve.Xu</a>
 * @version ${project.version} - 2019-08-22.
 */
public class DelivererMasterInstance extends MasterInstance {

  /**
   * Protected Constructor For {@link DelivererMasterInstance}
   */
  protected DelivererMasterInstance(String host, int port) {
    super(host, port);
  }

  /**
   * Create new master instance with address
   *
   * @param nodeAddress address , eg : 192.168.1.1:8080
   * @return a instance of {@link DelivererMasterInstance}
   */
  static DelivererMasterInstance newInstance(String nodeAddress) {
    String[] temp = nodeAddress.split(":");
    if (temp.length != 2) {
      throw new IllegalArgumentException("invalid master node address param , sample: [host:port]");
    }
    return new DelivererMasterInstance(temp[0], Integer.parseInt(temp[1]));
  }

  private DelivererMasterConnectorContext context;

  void registerClientInstance(
      DelivererMasterConnectorContext context,
      MasterConnectorProperties properties,
      NettyClientConfig config,
      NettyRemotingSocketClient client) {
    super.context = context;
    this.context = context;
    this.properties = properties;
    this.config = config;
    this.client = client;
  }

  /**
   * Build Register Body
   *
   * @param properties application properties
   * @param context    master connector context
   * @return body object
   */
  @Override
  protected Object buildRegisterBody(MasterConnectorProperties properties, MasterConnectorContext context) {
    // Default implement
    return null;
  }

  /**
   * Build register Header
   *
   * @param properties application properties
   * @return instance of {@link ClusterRegisterHeader}
   */
  @Override
  protected ClusterRegisterHeader buildRegisterHeader(MasterConnectorProperties properties) {

    ClusterRegisterHeader header = new ClusterRegisterHeader();
    header.setNodeServerType(InstanceNode.NodeType.DELIVERER.name());
    header.setNodeServerAddress(this.properties.getConnectorHost() + ":" + this.properties.getConnectorPort());

    return header;
  }
}
