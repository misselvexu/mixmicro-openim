/*
 * Copyright 1999-2018 Acmedcare+ Holding Ltd.
 * Licensed under the Apache License, Version 2.0 (the "License");
 */

package com.acmedcare.framework.newim.deliver.api.request;

import com.acmedcare.framework.newim.Message;
import lombok.*;

import java.io.Serializable;

/**
 * {@link MessageRequestBean}
 *
 * @author <a href="mailto:iskp.me@gmail.com">Elve.Xu</a>
 * @version ${project.version} - 2019-08-14.
 */
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MessageRequestBean implements Serializable {

  private static final long serialVersionUID = 1840072822307420243L;

  /** 通行证编号 */
  private String passportId;

  /** 消息类型 */
  private Message.MessageType messageType;

  /** 名称空间 */
  private String namespace;
}
