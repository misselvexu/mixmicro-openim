/*
 * Copyright 1999-2018 Acmedcare+ Holding Ltd.
 * Licensed under the Apache License, Version 2.0 (the "License");
 */

package com.acmedcare.framework.newim;

import com.acmedcare.framework.newim.client.MessageConstants;
import com.acmedcare.framework.newim.storage.IMStorageCollections;
import lombok.*;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

import java.io.Serializable;

/**
 * {@link Account}
 *
 * @author <a href="mailto:iskp.me@gmail.com">Elve.Xu</a>
 * @version ${project.version} - 2019-08-16.
 */
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Document(value = IMStorageCollections.ACCOUNT)
public class Account implements Serializable {

  private static final long serialVersionUID = 3409224271752437286L;

  @Builder.Default private String namespace = MessageConstants.DEFAULT_NAMESPACE;

  /** 账号类型 */
  @Builder.Default private AccountType accountType = AccountType.NORMAL;

  /** 账号 */
  @Indexed(unique = true)
  private String account;

  /** 账号扩展信息 */
  private String ext;

  /** 创建时间 */
  @Builder.Default private Long cts = System.currentTimeMillis();

  /** 账号状态 */
  @Builder.Default private Status status = Status.ENABLED;

  public enum AccountType {

    /** 普通账号 */
    NORMAL,

    /** 推送类型账号 */
    PUSHER,

    /** 系统账号 */
    SYSTEM
  }
}
