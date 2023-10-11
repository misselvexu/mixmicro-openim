/*
 * Copyright 1999-2018 Acmedcare+ Holding Ltd.
 * Licensed under the Apache License, Version 2.0 (the "License");
 */

package com.acmedcare.framework.newim.master.connector;

import com.acmedcare.framework.kits.lang.Nullable;
import com.acmedcare.tiffany.framework.remoting.netty.NettyClientConfig;
import com.acmedcare.tiffany.framework.remoting.netty.NettyRemotingSocketClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.atomic.AtomicBoolean;

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

  private static volatile AtomicBoolean startup = new AtomicBoolean(false);

  private static final Logger logger = LoggerFactory.getLogger(DelivererMasterConnector.class);

  /**
   * Register Master Connector Event Processor
   *
   * @param subscriber event subscribe
   */
  @Override
  protected void registerEvent(MasterConnectorSubscriber subscriber) {
    // empty
  }

  /**
   * Start up Connector
   *
   * @param handler handler instance of {@link MasterConnectorHandler}
   */
  @Override
  protected void doStartup(@Nullable MasterConnectorHandler handler) {

    if (startup.compareAndSet(false, true)) {

      // startup client connect
      if (!defaultMasterInstances.isEmpty()) {
        CountDownLatch latch = new CountDownLatch(defaultMasterInstances.size());
        long start = System.currentTimeMillis();
        for (MasterInstance masterInstance :
            DelivererMasterConnector.this.defaultMasterInstances) {
          try {
            logger.info(
                "\r\n >>>> Try starting up deliverer master connector - {}:{} ",
                masterInstance.getHost(),
                masterInstance.getPort());

            masterInstance.startup(latch);

          } catch (Exception e) {
            logger.error(
                "exception on connecting , self-thread will try daemon. - {}:{}",
                masterInstance.getHost(),
                masterInstance.getPort(),
                e);
          }
        }

        try {
          // await until startup success .
          latch.await();
          logger.info(
              "deliverer master connector(s) all executed startup ,Use Time :{} ms",
              (System.currentTimeMillis() - start));
        } catch (InterruptedException ignored) {
          // TODO process interrupt exception ...
        }

        logger.info("deliverer master connector(s) service is started. ");
      } else {
        logger.warn("not config master server node(s) address.");
      }
    } else {
      logger.warn("### deliverer server is already startup .");
    }
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

    DelivererMasterInstance delivererMasterInstance =
        DelivererMasterInstance.newInstance(nodeAddress);
    delivererMasterInstance.registerClientInstance(
        (DelivererMasterConnectorContext) context, masterConnectorProperties, config, client);

    return delivererMasterInstance;
  }
}
