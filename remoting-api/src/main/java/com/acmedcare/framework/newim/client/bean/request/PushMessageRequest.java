/*
 * Copyright 1999-2018 Acmedcare+ Holding Ltd.
 * Licensed under the Apache License, Version 2.0 (the "License");
 */

package com.acmedcare.framework.newim.client.bean.request;

import com.acmedcare.framework.newim.client.MessageConstants;
import com.acmedcare.framework.newim.client.PushMessageType;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.io.Serializable;
import java.util.List;

/**
 * {@link PushMessageRequest}
 *
 * @author <a href="mailto:iskp.me@gmail.com">Elve.Xu</a>
 * @version ${project.version} - 2019-08-16.
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class PushMessageRequest implements Serializable {

  private static final long serialVersionUID = 125141968086878047L;

  private String namespace = MessageConstants.DEFAULT_NAMESPACE;

  /** 发送消息发送者账号 */
  private String senderAccount;

  /** 内容 */
  private String content;

  /** 扩展信息 */
  private String ext;

  /** 时间戳 */
  private String timestamp;

  /** 接收通知的列表 */
  private List<String> receivers;

  /** 推送消息接收类型 */
  private PushMessageType pushMessageType = PushMessageType.PASSPORT;
}
