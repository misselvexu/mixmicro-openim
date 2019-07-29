/*
 * Copyright 1999-2018 Acmedcare+ Holding Ltd.
 * Licensed under the Apache License, Version 2.0 (the "License");
 */

package com.acmedcare.framework.newim.deliver.connector.server;

import com.acmedcare.framework.newim.deliver.api.DefaultDelivererProperties;
import com.acmedcare.framework.newim.deliver.api.DelivererInitializer;
import com.acmedcare.framework.newim.deliver.api.context.event.DelivererServerInitedEvent;
import com.acmedcare.framework.newim.deliver.api.context.event.DelivererServerStartedEvent;
import com.acmedcare.framework.newim.deliver.api.context.event.DelivererServerStopedEvent;
import com.acmedcare.framework.newim.deliver.api.exception.InitializerException;
import com.acmedcare.framework.newim.spi.util.Assert;
import com.acmedcare.tiffany.framework.remoting.ChannelEventListener;
import com.acmedcare.tiffany.framework.remoting.RemotingSocketServer;
import com.acmedcare.tiffany.framework.remoting.netty.NettyRemotingSocketServer;
import com.acmedcare.tiffany.framework.remoting.netty.NettyServerConfig;
import io.netty.channel.Channel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.atomic.AtomicBoolean;

/**
 * {@link DelivererServerInitializer}
 *
 * @author <a href="mailto:iskp.me@gmail.com">Elve.Xu</a>
 * @version ${project.version} - 2019-07-29.
 */
public class DelivererServerInitializer extends DelivererInitializer {

  private static final Logger log = LoggerFactory.getLogger(DelivererServerInitializer.class);

  private final DelivererServerProperties properties;

  private RemotingSocketServer server;

  private AtomicBoolean started = new AtomicBoolean(false);

  private NettyServerConfig config;

  protected DelivererServerInitializer(DefaultDelivererProperties properties) {
    super(properties);
    Assert.notNull(properties, "Deliverer Server Config Properties Instance Must not be null.");
    this.properties = (DelivererServerProperties) properties;

    this.config = new NettyServerConfig();
    this.config.setListenPort(this.properties.getPort());
  }

  @Override
  public void init() throws InitializerException {
    log.info("[==] Deliverer Server Initializer - invoked init method .");

    this.server =
        new NettyRemotingSocketServer(
            this.config,
            new ChannelEventListener() {
              @Override
              public void onChannelConnect(String remoteAddr, Channel channel) {}

              @Override
              public void onChannelClose(String remoteAddr, Channel channel) {}

              @Override
              public void onChannelException(String remoteAddr, Channel channel) {}

              @Override
              public void onChannelIdle(String remoteAddr, Channel channel) {}
            });

    // publish init-ed event
    this.publisher.publishEvent(new DelivererServerInitedEvent(null));
  }

  @Override
  public void startup() throws InitializerException {
    log.info("[==] Deliverer Server Initializer - invoked startup method .");
    if (started.compareAndSet(false, true)) {
      this.server.start();
      this.publisher.publishEvent(new DelivererServerStartedEvent(this.server));
    }
  }

  @Override
  public void shutdown() throws InitializerException {
    log.info("[==] Deliverer Server Initializer - invoked shutdown method .");
    if (started.compareAndSet(true, false)) {
      this.server.shutdown();
    }

    this.publisher.publishEvent(new DelivererServerStopedEvent(null));
  }
}
