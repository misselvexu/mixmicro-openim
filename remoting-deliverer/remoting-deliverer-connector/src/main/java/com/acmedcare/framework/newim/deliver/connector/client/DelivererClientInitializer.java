/*
 * Copyright 1999-2018 Acmedcare+ Holding Ltd.
 * Licensed under the Apache License, Version 2.0 (the "License");
 */

package com.acmedcare.framework.newim.deliver.connector.client;

import com.acmedcare.framework.kits.Assert;
import com.acmedcare.framework.newim.deliver.api.DefaultDelivererProperties;
import com.acmedcare.framework.newim.deliver.api.DelivererInitializer;
import com.acmedcare.framework.newim.deliver.api.exception.InitializerException;
import com.acmedcare.framework.newim.deliver.context.ConnectorContext;
import com.acmedcare.framework.newim.deliver.context.ConnectorInstance;
import com.google.common.collect.Lists;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

/**
 * {@link DelivererClientInitializer}
 *
 * @author <a href="mailto:iskp.me@gmail.com">Elve.Xu</a>
 * @version ${project.version} - 2019-07-29.
 */
public class DelivererClientInitializer extends DelivererInitializer {

  private static final Logger log = LoggerFactory.getLogger(DelivererClientInitializer.class);

  private final DelivererClientProperties delivererClientProperties;

  protected DelivererClientInitializer(DefaultDelivererProperties properties) {
    super(properties);
    this.delivererClientProperties = (DelivererClientProperties) properties;
  }

  private List<ConnectorInstance.ConnectorServerInstance> serverInstances = Lists.newArrayList();

  @Override
  public void init() throws InitializerException {

    long sts = System.currentTimeMillis();

    log.info("[==] Deliverer Client Connector is initializing ...");

    Assert.notEmpty(this.delivererClientProperties.getRemotingAddr(), "[==] Deliverer Server Configured Address List Must Not Be null and empty.");

    List<String> addrs = this.delivererClientProperties.getRemotingAddr();

    addrs.forEach(
        addr -> {
          ConnectorInstance.ConnectorServerInstance instance =
              ConnectorInstance.ConnectorServerInstance.builder()
                  .serverAddr(addr)
                  .heartbeat(this.delivererClientProperties.isHeartbeatEnabled())
                  .heartbeatPeriod(this.delivererClientProperties.getHeartbeatPeriod())
                  .ssl(this.delivererClientProperties.isSsl())
                  .build();

          log.debug("[==] Deliverer Client Connector Server-Instance : {}", instance);

          if(this.serverInstances.contains(instance)) {
            log.warn("[==] Deliverer Client Connector is init-ed , ignore .");
          } else {

            log.info("[==] Deliverer Client Connector Register into context.");
            ConnectorContext.context().register(instance);

            log.info("[==] Deliverer Client Connector Saved into cache.");
            this.serverInstances.add(instance);
          }
        });

    log.info("[==] Deliverer Client Connector is initialized . time: {} ms" , (System.currentTimeMillis() - sts));
  }

  @Override
  public void startup() throws InitializerException {



  }

  @Override
  public void shutdown() throws InitializerException {


  }
}
