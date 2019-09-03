/*
 * Copyright 1999-2018 Acmedcare+ Holding Ltd.
 * Licensed under the Apache License, Version 2.0 (the "License");
 */

package com.acmedcare.framework.newim.deliver.api.bean;

import com.acmedcare.framework.newim.Message;
import lombok.*;

import java.io.Serializable;
import java.util.Objects;

/**
 * {@link DelivererMessageBean}
 *
 * @author <a href="mailto:iskp.me@gmail.com">Elve.Xu</a>
 * @version ${project.version} - 2019-08-15.
 */
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DelivererMessageBean implements Serializable {

  private static final long serialVersionUID = 2549465869525092131L;

  private boolean half;

  /** 名称空间 */
  private String namespace;

  /** 接收人通行证标识 */
  private String passportId;

  @Builder.Default private String clientType = "DEFAULT";

  /** 消息标识 */
  private String messageId;

  /** 消息类型 */
  private Message.MessageType messageType;

  /** 消息内容 */
  private byte[] message;

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    DelivererMessageBean that = (DelivererMessageBean) o;
    return namespace.equals(that.namespace)
        && passportId.equals(that.passportId)
        && messageId.equals(that.messageId);
  }

  @Override
  public int hashCode() {
    return Objects.hash(namespace, passportId, messageId);
  }
}
