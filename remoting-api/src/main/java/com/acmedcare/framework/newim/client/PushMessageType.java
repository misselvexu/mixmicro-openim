/*
 * Copyright 1999-2018 Acmedcare+ Holding Ltd.
 * Licensed under the Apache License, Version 2.0 (the "License");
 */

package com.acmedcare.framework.newim.client;

/**
 * {@link PushMessageType}
 *
 * @author <a href="mailto:iskp.me@gmail.com">Elve.Xu</a>
 * @version ${project.version} - 2019-08-16.
 */
public enum PushMessageType {

  /** 默认类型 */
  DEFAULT,

  /** 账号 */
  PASSPORT,

  /** 设备 */
  DEVICE,

  /** 区域 */
  @Deprecated
  AREA
}
