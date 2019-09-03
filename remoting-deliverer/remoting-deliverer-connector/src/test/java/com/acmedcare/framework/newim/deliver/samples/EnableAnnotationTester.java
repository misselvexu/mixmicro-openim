/*
 * Copyright 1999-2018 Acmedcare+ Holding Ltd.
 * Licensed under the Apache License, Version 2.0 (the "License");
 */

package com.acmedcare.framework.newim.deliver.samples;

import com.acmedcare.framework.newim.deliver.api.RemotingDelivererApi;
import com.acmedcare.framework.newim.deliver.connector.EnableDelivererConnector;
import com.acmedcare.framework.newim.deliver.context.ConnectorContext;
import org.springframework.boot.WebApplicationType;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.context.ConfigurableApplicationContext;

/**
 * {@link EnableAnnotationTester}
 *
 * @author <a href="mailto:iskp.me@gmail.com">Elve.Xu</a>
 * @version ${project.version} - 2019-07-29.
 */
@EnableDelivererConnector(enabledServer = false, enabledClient = false , listeners = SimpleDelivererConnectorListener.class)
public class EnableAnnotationTester {

  public static void main(String[] args) {
    ConfigurableApplicationContext context =
        new SpringApplicationBuilder(EnableAnnotationTester.class)
            .web(WebApplicationType.NONE)
            .run(args);


    // Api Instance
    RemotingDelivererApi api = ConnectorContext.context().remotingDelivererApi();

    // client apis
    // 1.

    // server apis


  }
}
