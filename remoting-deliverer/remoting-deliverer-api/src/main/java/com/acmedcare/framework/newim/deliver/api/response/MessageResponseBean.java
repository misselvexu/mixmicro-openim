/*
 * Copyright 1999-2018 Acmedcare+ Holding Ltd.
 * Licensed under the Apache License, Version 2.0 (the "License");
 */

package com.acmedcare.framework.newim.deliver.api.response;

import com.acmedcare.framework.newim.deliver.api.bean.DelivererMessageBean;
import com.google.common.collect.Lists;
import lombok.*;

import java.io.Serializable;
import java.util.List;

/**
 * {@link MessageResponseBean}
 *
 * @author <a href="mailto:iskp.me@gmail.com">Elve.Xu</a>
 * @version ${project.version} - 2019-08-15.
 */
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MessageResponseBean implements Serializable {

  private static final long serialVersionUID = 1368181506935914841L;


  /** 消息列表 */
  @Builder.Default private List<DelivererMessageBean> messages = Lists.newArrayList();
}
