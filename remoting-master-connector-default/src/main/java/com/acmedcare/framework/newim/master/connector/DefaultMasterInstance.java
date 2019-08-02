/*
 * Copyright 1999-2018 Acmedcare+ Holding Ltd.
 * Licensed under the Apache License, Version 2.0 (the "License");
 */

package com.acmedcare.framework.newim.master.connector;

import com.acmedcare.framework.newim.protocol.request.ClusterRegisterHeader;
import com.acmedcare.tiffany.framework.remoting.netty.NettyClientConfig;
import com.acmedcare.tiffany.framework.remoting.netty.NettyRemotingSocketClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * MasterInstance
 *
 * @author <a href="mailto:iskp.me@gmail.com">Elve.Xu</a>
 * @version ${project.version} - 2018-12-17.
 */
public final class DefaultMasterInstance extends MasterInstance {

  private static final Logger logger = LoggerFactory.getLogger(DefaultMasterInstance.class);

  protected DefaultMasterConnectorContext context;

  protected DefaultMasterInstance(String host, int port) {
    super(host, port);
  }

  /**
   * Create new master instance with address
   *
   * @param nodeAddress address , like : 192.168.1.1:8080
   * @return a instance of {@link DefaultMasterInstance}
   */
  static DefaultMasterInstance newInstance(String nodeAddress) {
    String[] temp = nodeAddress.split(":");
    if (temp.length != 2) {
      throw new IllegalArgumentException("invalid master node address param , sample: [host:port]");
    }
    return new DefaultMasterInstance(temp[0], Integer.parseInt(temp[1]));
  }

  void registerClientInstance(
      DefaultMasterConnectorContext context,
      MasterConnectorProperties properties,
      NettyClientConfig config,
      NettyRemotingSocketClient client) {
    super.context = context;
    this.context = context;
    this.properties = properties;
    this.config = config;
    this.client = client;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (!(o instanceof DefaultMasterInstance)) {
      return false;
    }
    DefaultMasterInstance that = (DefaultMasterInstance) o;
    return getPort() == that.getPort() && getHost().equals(that.getHost());
  }

  @Override
  protected Object buildRegisterBody(
      MasterConnectorProperties properties, MasterConnectorContext context) {

    return this.context.getWssEndpoints();
  }

  @Override
  protected ClusterRegisterHeader buildRegisterHeader(MasterConnectorProperties properties) {

    ClusterRegisterHeader header = new ClusterRegisterHeader();
    header.setNodeServerType(this.properties.getConnectorType().name());
    header.setNodeServerHost(
        this.properties.getConnectorHost() + ":" + this.properties.getConnectorPort());
    header.setNodeServerAddress(
        this.properties.getConnectorHost() + ":" + this.properties.getConnectorReplicaPort());

    header.setHasWssEndpoints(buildRegisterBody(this.properties, this.context) != null);

    return header;
  }
}
