/*
 * Copyright 1999-2018 Acmedcare+ Holding Ltd.
 * Licensed under the Apache License, Version 2.0 (the "License");
 */

package com.acmedcare.framework.newim.sync.master.config;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;

import java.util.List;
import java.util.Map;

/**
 * {@link SyncProperties}
 *
 * @author <a href="mailto:iskp.me@gmail.com">Elve.Xu</a>
 * @version ${project.version} - 2019-03-17.
 */
@Getter
@Setter
@Configuration
@ConfigurationProperties(prefix = "sync.master")
@PropertySource("classpath:sync.properties")
public class SyncProperties {

  private Map<String, MasterNodeProperties> zones = Maps.newHashMap();

  @Getter
  @Setter
  public static class MasterNodeProperties {

    /** Master node server address */
    private List<String> serverAddr = Lists.newArrayList();

    /** sync and server heartbeat period, default: 20 s */
    private int heartbeatPeriod = 20;
  }
}
