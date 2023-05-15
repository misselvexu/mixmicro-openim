/*
 * Copyright 1999-2018 Acmedcare+ Holding Ltd.
 * Licensed under the Apache License, Version 2.0 (the "License");
 */

package com.acmedcare.framework.newim.deliver.api;

import com.google.common.annotations.Beta;

/**
 * {@link DelivererCommand}
 *
 * @author <a href="mailto:iskp.me@gmail.com">Elve.Xu</a>
 * @version ${project.version} - 2019-07-25.
 */
public final class DelivererCommand {

  /** 握手协议 */
  public static final int HANDSHAKE_COMMAND_VALUE = 0x09;

  /** 通讯节点注册协议 */
  public static final int REGISTER_COMMAND_VALUE = 0x10;

  /** 下线协议 */
  public static final int SHUTDOWN_COMMAND_VALUE = 0x11;

  /** 申请投递 */
  public static final int REQUEST_DELIVERER_VALUE = 0x12;

  /** 撤销投递请求 */
  public static final int REVOKE_DELIVERER_VALUE = 0x13;

  /** 获取客户端待投递消息 */
  public static final int FETCH_CLIENT_DELIVERER_MESSAGES_VALUE = 0x14;

  /**
   * 获取客户端待投递的消息总数量
   *
   * @since 2.3.1
   */
  @Beta 
  public static final int FETCH_CLIENT_DELIVERER_MESSAGES_COUNT_VALUE = 0x17;

  /** 定时投递消息 */
  public static final int TIMED_DELIVERY_MESSAGE_COMMAND_VALUE = 0x15;

  /** 确认投递消息Ack */
  public static final int DELIVERER_CLIENT_ACK_DELIVERER_VALUE = 0x16;

  /**
   * 投递服务器分发消息
   *
   * @since 2.3.1
   */
  public static final int DELIVERER_SERVER_DISTRIBUTE_MESSAGES = 0x18;
}
