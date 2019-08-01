/*
 * Copyright 1999-2018 Acmedcare+ Holding Ltd.
 * Licensed under the Apache License, Version 2.0 (the "License");
 */

package com.acmedcare.framework.newim;

import com.acmedcare.framework.newim.storage.IMStorageCollections;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.data.mongodb.core.index.CompoundIndex;
import org.springframework.data.mongodb.core.mapping.DBRef;
import org.springframework.data.mongodb.core.mapping.Document;

import java.io.Serializable;
import java.util.Date;

/**
 * {@link DelivererMessage}
 *
 * @author <a href="mailto:iskp.me@gmail.com">Elve.Xu</a>
 * @version ${project.version} - 2019-08-01.
 */
@Getter
@Setter
@NoArgsConstructor
@Document(value = IMStorageCollections.DELIVERER_MESSAGE)
@CompoundIndex(
    unique = true,
    name = "deliverer_message_record_compound_index",
    def = "{'mid': 1, 'delivererSource': -1, 'delivererType': 1}")
public class DelivererMessage implements Serializable {

  /** 消息编号 */
  private Long mid;

  /** 消息类型 */
  private Message.MessageType messageType;

  /** 投递时间 */
  private Date delivererTime;

  /** 投递来源，服务器节点 */
  private String delivererSource;

  /** 投递类型 */
  private DelivererType delivererType;

  @DBRef
  private Message message;


  // ===== eXtenstion Defined ======

  /** 投递类型 */
  public enum DelivererType {

    /** 客户端离线 */
    OFFLINE,

    /** 网络异常 */
    XNETWAORK,

    /** 手动投递 */
    MANUAL
  }
}
