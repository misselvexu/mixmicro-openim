/*
 * Copyright 2014-2019 Acmedcare+ Holding Ltd.
 * Licensed under the Apache License, Version 2.0 (the "License");
 */

package com.acmedcare.framework.newim.sync.master.autoconfigure;

import com.acmedcare.framework.newim.sync.master.config.SyncProperties;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Configuration;

/**
 * {@link SyncClientAutoConfiguration}
 *
 * @author <a href="mailto:iskp.me@gmail.com">Elve.Xu</a>
 * @version ${project.version} - 2019-03-21.
 */
@Configuration
@EnableConfigurationProperties(SyncProperties.class)
public class SyncClientAutoConfiguration {}
