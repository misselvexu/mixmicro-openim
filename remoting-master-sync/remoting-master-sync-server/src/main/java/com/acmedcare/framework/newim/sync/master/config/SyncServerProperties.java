/*
 * Copyright 2014-2019 Acmedcare+ Holding Ltd.
 * Licensed under the Apache License, Version 2.0 (the "License");
 */

package com.acmedcare.framework.newim.sync.master.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

/**
 * {@link SyncServerProperties}
 *
 * @author <a href="mailto:iskp.me@gmail.com">Elve.Xu</a>
 * @version ${project.version} - 2019-03-17.
 */
@Getter
@Setter
@Configuration
@ConfigurationProperties(prefix = "sync.server")
public class SyncServerProperties {

  /** Sync Server port */
  private int port = 43301;

  /** Sync Server host */
  private String host = "127.0.0.1";

  /** Sync Server zone */
  private String zone = "default";
}
