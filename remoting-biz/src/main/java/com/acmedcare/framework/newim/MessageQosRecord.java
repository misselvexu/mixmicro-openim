/*
 * Copyright 1999-2018 Acmedcare+ Holding Ltd.
 * Licensed under the Apache License, Version 2.0 (the "License");
 */
package com.acmedcare.framework.newim;

import com.acmedcare.framework.newim.storage.IMStorageCollections;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

import java.io.Serializable;

/**
 * {@link MessageQosRecord}
 *
 * @author <a href="mailto:iskp.me@gmail.com">Elve.Xu</a>
 * @version ${project.version} - 2019-07-11.
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Document(value = IMStorageCollections.MESSAGE_QOS)
public class MessageQosRecord implements Serializable {

  /** 记录唯一标识 */
  @Indexed(name = "qos_record_id", unique = true)
  private Long recordId;

  /** 发送人 */
  private String sender;

  /** 接收人 */
  @Indexed(name = "qos_receiver_index")
  private String receiver;

  /** 消息编号 */
  private Long messageId;

  /** 处理时间 */
  private long processTime;
}
