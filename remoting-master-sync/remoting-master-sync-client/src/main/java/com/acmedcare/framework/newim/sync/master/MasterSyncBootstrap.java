/*
 * Copyright 1999-2018 Acmedcare+ Holding Ltd.
 * Licensed under the Apache License, Version 2.0 (the "License");
 */

package com.acmedcare.framework.newim.sync.master;

import com.acmedcare.framework.newim.sync.master.config.SyncProperties;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;

/**
 * {@link MasterSyncBootstrap}
 *
 * @author <a href="mailto:iskp.me@gmail.com">Elve.Xu</a>
 * @version ${project.version} - 2019-03-17.
 */
@SpringBootApplication
@EnableConfigurationProperties(SyncProperties.class)
public class MasterSyncBootstrap {

  public static void main(String[] args) {
    SpringApplication.run(MasterSyncBootstrap.class, args);
  }
}
