/*
 * Copyright 1999-2018 Acmedcare+ Holding Ltd.
 * Licensed under the Apache License, Version 2.0 (the "License");
 */

package com.acmedcare.framework.newim.client.bean;

import com.acmedcare.framework.newim.client.MessageConstants;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.io.Serializable;

/**
 * Group
 *
 * @author <a href="mailto:iskp.me@gmail.com">Elve.Xu</a>
 * @version ${project.version} - 2019-01-18.
 */
@Getter
@Setter
@NoArgsConstructor
public class Group implements Serializable {

  private String groupId;

  /**
   * 群主
   */
  private String groupOwner;

  /**
   * 群组名称
   */
  private String groupName;
  /**
   * 业务标识
   */
  private String groupBizTag;

  /**
   * 群组扩展信息
   */
  private String groupExt;

  private Status groupStatus;

  private String namespace = MessageConstants.DEFAULT_NAMESPACE;

}
