/*
 * Copyright 1999-2018 Acmedcare+ Holding Ltd.
 * Licensed under the Apache License, Version 2.0 (the "License");
 */

package com.acmedcare.framework.newim.deliver.connector.server;

import com.acmedcare.framework.kits.thread.ThreadKit;
import com.acmedcare.framework.newim.deliver.api.DefaultDelivererProperties;
import com.acmedcare.framework.newim.deliver.api.DelivererInitializer;
import com.acmedcare.framework.newim.deliver.api.context.event.DelivererServerInitedEvent;
import com.acmedcare.framework.newim.deliver.api.context.event.DelivererServerStartedEvent;
import com.acmedcare.framework.newim.deliver.api.context.event.DelivererServerStopedEvent;
import com.acmedcare.framework.newim.deliver.api.exception.InitializerException;
import com.acmedcare.framework.newim.deliver.context.ConnectorContext;
import com.acmedcare.framework.newim.deliver.context.ConnectorInstance;
import com.acmedcare.framework.newim.deliver.context.processor.*;
import com.acmedcare.framework.newim.spi.util.Assert;
import com.acmedcare.tiffany.framework.remoting.ChannelEventListener;
import com.acmedcare.tiffany.framework.remoting.RemotingSocketServer;
import com.acmedcare.tiffany.framework.remoting.common.RemotingUtil;
import com.acmedcare.tiffany.framework.remoting.netty.NettyRemotingSocketServer;
import com.acmedcare.tiffany.framework.remoting.netty.NettyServerConfig;
import io.netty.channel.Channel;
import io.netty.util.concurrent.DefaultThreadFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

import static com.acmedcare.framework.newim.deliver.api.DelivererCommand.*;
import static com.acmedcare.framework.newim.deliver.context.ConnectorContext.CONNECTOR_REMOTING_ATTRIBUTE_KEY;

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

  private ExecutorService defaultExecutor =
      new ThreadPoolExecutor(
          Runtime.getRuntime().availableProcessors(),
          Runtime.getRuntime().availableProcessors(),
          0,
          TimeUnit.MILLISECONDS,
          new LinkedBlockingQueue<>(64),
          new DefaultThreadFactory("new-im-netty-default-processor-executor-"),
          new ThreadPoolExecutor.CallerRunsPolicy());

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
              public void onChannelConnect(String remoteAddr, Channel channel) {
                log.info("[==] Remoting Deliverer Connector Client :{} is connected . Channel: {}", remoteAddr, channel);
              }

              @Override
              public void onChannelClose(String remoteAddr, Channel channel) {
                log.info("[==] Remoting Deliverer Connector Client :{} is closed . Channel: {}", remoteAddr, channel);

                if(channel != null) {
                  ConnectorInstance.ConnectorClientInstance instance =
                      ConnectorContext.parseChannel(channel,CONNECTOR_REMOTING_ATTRIBUTE_KEY, ConnectorInstance.ConnectorClientInstance.class);

                  try{
                    RemotingUtil.closeChannel(channel);
                  } catch (Exception ignore) {
                    // ignore
                  } finally{
                    // release channel instance
                    ConnectorContext.context().release(instance);
                  }
                }
              }

              @Override
              public void onChannelException(String remoteAddr, Channel channel) {
                log.info("[==] Remoting Deliverer Connector Client :{} happened Exception . Channel: {}", remoteAddr, channel);
              }

              @Override
              public void onChannelIdle(String remoteAddr, Channel channel) {
                log.info("[==] Remoting Deliverer Connector Client :{} is Idle . Channel: {}", remoteAddr, channel);
              }
            });

    log.info("[==] Remoting Deliverer Server Register-ing processors . ");
    this.server.registerProcessor(REGISTER_COMMAND_VALUE,new RegisterProcessor(),null);
    this.server.registerProcessor(SHUTDOWN_COMMAND_VALUE,new ShutdownProcessor(),null);
    this.server.registerProcessor(REQUEST_DELIVERER_VALUE,new DelivererMessageProcessor(true),null);
    this.server.registerProcessor(REVOKE_DELIVERER_VALUE,new RevokeDelivererProcessor(),null);
    this.server.registerProcessor(FETCH_CLIENT_DELIVERER_MESSAGES_VALUE,new MessageProcessor(),null);
    this.server.registerProcessor(DELIVERER_CLIENT_ACK_DELIVERER_VALUE,new AckProcessor(),null);

    // default processor
    this.server.registerDefaultProcessor(new DefaultDelivererProcessor(),defaultExecutor);

    // publish init-ed event
    this.publisher.publishEvent(new DelivererServerInitedEvent(this));
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
      this.publisher.publishEvent(new DelivererServerStopedEvent(this));

      if(defaultExecutor != null) {
        ThreadKit.gracefulShutdown(defaultExecutor,5,5,TimeUnit.SECONDS);
      }
    }
  }
}
