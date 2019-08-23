/*
 * Copyright 2014-2019 Acmedcare+ Holding Ltd.
 * Licensed under the Apache License, Version 2.0 (the "License");
 */

package com.acmedcare.framework.newim.sync.api;

/**
 * {@link MasterSyncCommand}
 *
 * @author <a href="mailto:iskp.me@gmail.com">Elve.Xu</a>
 * @version ${project.version} - 2019-03-17.
 */
public class MasterSyncCommand {

  /** 同步握手 */
  public static final int SYNC_HANDSHAKE = 0x50000;

  /** 同步注册 */
  public static final int SYNC_REGISTER = 0x50001;

  /** 同步推出 */
  public static final int SYNC_SHUTDOWN = 0x50002;

  /** 同步心跳 */
  public static final int SYNC_HEARTBEAT = 0x50003;

  /** 同步转发 */
  public static final int SYNC_FORWARD = 0x50004;
}
