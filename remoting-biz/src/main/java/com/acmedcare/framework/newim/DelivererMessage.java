/*
 * Copyright 1999-2018 Acmedcare+ Holding Ltd.
 * Licensed under the Apache License, Version 2.0 (the "License");
 */

package com.acmedcare.framework.newim;

import com.acmedcare.framework.newim.storage.IMStorageCollections;
import lombok.*;
import org.springframework.data.mongodb.core.index.CompoundIndex;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Date;
import java.util.Objects;
import java.util.UUID;

/**
 * {@link DelivererMessage}
 *
 * @author <a href="mailto:iskp.me@gmail.com">Elve.Xu</a>
 * @version ${project.version} - 2019-08-01.
 */
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Document(value = IMStorageCollections.DELIVERER_MESSAGE)
@CompoundIndex(
    unique = true,
    name = "deliverer_message_record_compound_index",
    def = "{'mid': 1, 'namespace': 1, 'delivererType': 1, 'receiver': 1}")
public class DelivererMessage implements Serializable {

  private static final long serialVersionUID = 8751252674787722435L;

  @Indexed(unique = true)
  @Builder.Default private String uuid = UUID.randomUUID().toString();

  private String namespace;

  /** 消息编号 */
  private Long mid;

  /** 消息类型 */
  private Message.MessageType messageType;

  /** 投递时间 */
  @Builder.Default private Date delivererTime = new Date();

  /** 投递来源，服务器节点 */
  @Builder.Default private String delivererSource = "DEFAULT";

  /** 投递类型 */
  @Builder.Default private DelivererType delivererType = DelivererType.OFFLINE;

  /** 接收人 */
  private String receiver;

  @Builder.Default private String clientType = "DEFAULT";

  /** 消息体 */
  private byte[] payload;

  @Builder.Default private DelivererStatus delivererStatus = DelivererStatus.READY;

  /** 投递时间 */
  @Builder.Default private Date deliveringTime = Date.from(LocalDateTime.now().minusMinutes(5).atZone(ZoneId.systemDefault()).toInstant());

  /** 已投递时间 */
  private Date deliveredTime;

  @Builder.Default private String ukey = "";

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    DelivererMessage that = (DelivererMessage) o;
    return namespace.equals(that.namespace) &&
        mid.equals(that.mid) &&
        messageType == that.messageType &&
        receiver.equals(that.receiver);
  }

  @Override
  public int hashCode() {
    return Objects.hash(namespace, mid, messageType, receiver);
  }

  // ===== eXtension Defined ======

  /** 投递状态 */
  public enum DelivererStatus {

    /** 取消状态 */
    CANCEL,

    /** 初始半状态 */
    HALF,

    /** 待投递状态 */
    READY,

    /** 投递中 */
    DELIVERING,

    /** 已投递 */
    DELIVERED,
  }

  /** 投递类型 */
  public enum DelivererType {

    /** 客户端离线 */
    OFFLINE,

    /** 网络异常 */
    XNETWAORK,

    /** 手动投递 */
    MANUAL,

    /** 其他情况 */
    OTHER
  }
}
