/*
 * Copyright 1999-2018 Acmedcare+ Holding Ltd.
 * Licensed under the Apache License, Version 2.0 (the "License");
 */

package com.acmedcare.framework.newim.deliver.api.request;

import com.acmedcare.framework.newim.Message;
import com.google.common.collect.Lists;
import lombok.*;

import java.io.Serializable;
import java.util.List;
import java.util.Objects;

/**
 * {@link TimedDelivererMessageRequestBean}
 *
 * @author <a href="mailto:iskp.me@gmail.com">Elve.Xu</a>
 * @version ${project.version} - 2019-08-13.
 */
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TimedDelivererMessageRequestBean implements Serializable {

  private static final long serialVersionUID = -5694977199066571172L;

  /** 时间戳 */
  @Builder.Default private long timestamp = System.currentTimeMillis();

  /** 消息列表 */
  @Builder.Default private List<TimedMessage> messages = Lists.newLinkedList();

  // ===== Timed Message Bean =====

  @Getter
  @Setter
  @Builder
  @ToString
  @NoArgsConstructor
  @AllArgsConstructor
  public static class TimedMessage implements Serializable {

    /** 名称空间 */
    private String namespace;

    /** 接收人通行证标识 */
    private String passportId;

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
      TimedMessage that = (TimedMessage) o;
      return Objects.equals(namespace, that.namespace)
          && Objects.equals(passportId, that.passportId)
          && Objects.equals(messageId, that.messageId)
          && messageType == that.messageType;
    }

    @Override
    public int hashCode() {
      return Objects.hash(namespace, passportId, messageId, messageType);
    }
  }
}
