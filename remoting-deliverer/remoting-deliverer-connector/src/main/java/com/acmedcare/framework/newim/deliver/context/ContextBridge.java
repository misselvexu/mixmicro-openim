/*
 * Copyright 1999-2018 Acmedcare+ Holding Ltd.
 * Licensed under the Apache License, Version 2.0 (the "License");
 */

package com.acmedcare.framework.newim.deliver.context;

import org.springframework.context.ConfigurableApplicationContext;

/**
 * {@link ContextBridge}
 *
 * @author <a href="mailto:iskp.me@gmail.com">Elve.Xu</a>
 * @version ${project.version} - 2019-07-29.
 */
public class ContextBridge {

  private static class InstanceHolder {
    private static final ContextBridge CONTEXT_BRIDGE = new ContextBridge();
  }

  private ContextBridge() {}

  public static ContextBridge context() {
    return InstanceHolder.CONTEXT_BRIDGE;
  }

  private static ConfigurableApplicationContext context;

  public void registerApplicationContext(ConfigurableApplicationContext context) {
    ContextBridge.context = context;
  }
}
