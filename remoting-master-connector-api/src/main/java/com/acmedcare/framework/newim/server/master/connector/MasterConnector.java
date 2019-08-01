/*
 * Copyright 1999-2018 Acmedcare+ Holding Ltd.
 * Licensed under the Apache License, Version 2.0 (the "License");
 */

package com.acmedcare.framework.newim.server.master.connector;

import com.acmedcare.framework.kits.event.EventBus;
import com.acmedcare.tiffany.framework.remoting.ChannelEventListener;
import com.acmedcare.tiffany.framework.remoting.netty.NettyClientConfig;
import com.acmedcare.tiffany.framework.remoting.netty.NettyRemotingSocketClient;
import com.google.common.collect.Lists;
import io.netty.util.internal.StringUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

/**
 * MasterConnector
 *
 * @author <a href="mailto:iskp.me@gmail.com">Elve.Xu</a>
 * @version ${project.version} - 2018-12-17.
 */
public abstract class MasterConnector {

  protected static Logger logger = LoggerFactory.getLogger(MasterConnector.class);
  protected MasterConnectorProperties masterConnectorProperties;
  protected MasterConnectorContext context;
  protected MasterConnectorSubscriber subscriber;
  protected List<MasterInstance> defaultMasterInstances = Lists.newArrayList();

  MasterConnector(MasterConnectorProperties properties, MasterConnectorContext context) {
    this.masterConnectorProperties = properties;
    this.context = context;
  }

  /** Init Method */
  public void init() {

    // init event bus
    if (EventBus.isEnable()) {
      this.subscriber = new MasterConnectorSubscriber(this.context, false);
      registerEvent(subscriber);
      logger.info("register master connector subscriber : {}", subscriber);
    }

    // create master instance(s)
    List<String> nodes = this.masterConnectorProperties.getNodes();
    if (nodes != null && !nodes.isEmpty()) {
      for (String node : nodes) {
        defaultMasterInstances.add(newMasterInstance(node));
      }
    } else {
      logger.warn("not config master server node(s) address.");
    }

    logger.info("master connector is inited.");
  }

  protected abstract void registerEvent(MasterConnectorSubscriber subscriber);

  protected MasterInstance newMasterInstance(String nodeAddress) {
    NettyClientConfig config = new NettyClientConfig();
    config.setUseTLS(this.masterConnectorProperties.isConnectorEnableTls());
    config.setClientChannelMaxIdleTimeSeconds(
        (int) this.masterConnectorProperties.getConnectorIdleTime());
    config.setEnableHeartbeat(!this.masterConnectorProperties.isHeartbeatEnabled());

    ChannelEventListener listener = null;

    if (!StringUtil.isNullOrEmpty(
        this.masterConnectorProperties.getConnectorChannelEventListener())) {

      try {
        listener =
            (ChannelEventListener)
                Class.forName(this.masterConnectorProperties.getConnectorChannelEventListener())
                    .getConstructor() // must have default constructor
                    .newInstance();
      } catch (Exception e) {
        logger.warn("master connector channel event listener init failed ,ignore.", e);
      }
    }

    // build client
    NettyRemotingSocketClient client = new NettyRemotingSocketClient(config, listener);

    client.updateNameServerAddressList(Lists.newArrayList(nodeAddress));

    MasterInstance masterInstance = registerClientProcessor(nodeAddress, config, client);

    return masterInstance;
  }

  protected abstract MasterInstance registerClientProcessor(
      String nodeAddress, NettyClientConfig config, NettyRemotingSocketClient client);
}
