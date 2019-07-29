/*
 * Copyright 1999-2018 Acmedcare+ Holding Ltd.
 * Licensed under the Apache License, Version 2.0 (the "License");
 */

package com.acmedcare.framework.newim.deliver.connector;

import org.springframework.context.annotation.Import;

import java.lang.annotation.*;

/**
 * {@link EnableDelivererConnector}
 *
 * @author <a href="mailto:iskp.me@gmail.com">Elve.Xu</a>
 * @version ${project.version} - 2019-07-29.
 */
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Documented
@Import(DelivererConnectorImportSelector.class)
public @interface EnableDelivererConnector {

  /**
   * Enabled Deliverer Client Flag, Default: <code>false</code>
   *
   * @return true /false
   */
  boolean enabledClient() default false;

  /**
   * Enabled Deliverer Server Flag, Default: <code>false</code>
   *
   * @return true /false
   */
  boolean enabledServer() default false;
}
