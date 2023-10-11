/*
 * Copyright 2014-2019 Acmedcare+ Holding Ltd.
 * Licensed under the Apache License, Version 2.0 (the "License");
 */

package com.acmedcare.framework.newim;

import com.acmedcare.framework.kits.Assert;
import com.acmedcare.framework.newim.deliver.connector.EnableDelivererConnector;
import com.acmedcare.framework.newim.deliver.connector.server.DelivererServerInitializer;
import com.acmedcare.framework.newim.master.connector.DelivererMasterConnector;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeansException;
import org.springframework.boot.WebApplicationType;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.boot.context.event.ApplicationStartedEvent;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.context.ApplicationListener;

import static com.acmedcare.framework.newim.deliver.connector.server.DelivererServerMarkerConfiguration.DELIVERER_SERVER_INITIALIZER_BEAN_NAME;
import static com.acmedcare.framework.newim.master.connector.DelivererMasterConnectorAutoConfiguration.DELIVERER_MASTER_CONNECTOR_BEAN_NAME;

/**
 * {@link DelivererBootstrap}
 *
 * @author <a href="mailto:iskp.me@gmail.com">Elve.Xu</a>
 * @version ${project.version} - 2019-07-13.
 */
@SpringBootApplication
@EnableDelivererConnector(enabledServer = true)
public class DelivererBootstrap {

  public static void main(String[] args) {
    new SpringApplicationBuilder()
        .sources(DelivererBootstrap.class)
        .properties("spring.profiles.active=production")
        .web(WebApplicationType.NONE)
        .run(args);
  }

  // ======= bootstrap event listener ========

  public static class DelivererApplicationContextListener
      implements ApplicationListener<ApplicationStartedEvent>, ApplicationContextAware {

    private static final Logger log =
        LoggerFactory.getLogger(DelivererApplicationContextListener.class);

    private ApplicationContext applicationContext;

    @Override
    public void onApplicationEvent(ApplicationStartedEvent event) {

      Assert.isTrue(applicationContext.containsBean(DELIVERER_SERVER_INITIALIZER_BEAN_NAME));

      DelivererServerInitializer serverInitializer =
          applicationContext.getBean(
              DELIVERER_SERVER_INITIALIZER_BEAN_NAME, DelivererServerInitializer.class);

      log.info("[==] Deliverer Server Initializer instance : {}", serverInitializer);

      serverInitializer.startup();

      Assert.isTrue(applicationContext.containsBean(DELIVERER_MASTER_CONNECTOR_BEAN_NAME));

      DelivererMasterConnector delivererMasterConnector = this.applicationContext.getBean(DELIVERER_MASTER_CONNECTOR_BEAN_NAME,DelivererMasterConnector.class);

      log.info("[==] Deliverer Master Connector instance : {}", serverInitializer);

      // 
      delivererMasterConnector.startup(null);
    }

    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
      this.applicationContext = applicationContext;
    }
  }
}
