/*
 * Copyright 1999-2018 Acmedcare+ Holding Ltd.
 * Licensed under the Apache License, Version 2.0 (the "License");
 */

package com.acmedcare.framework.newim.deliver.connector.listener;

import com.acmedcare.framework.newim.deliver.connector.listener.event.DelivererEvent;
import org.springframework.beans.factory.BeanFactory;

/**
 * {@link DelivererConnectorListener}
 *
 * @author <a href="mailto:iskp.me@gmail.com">Elve.Xu</a>
 * @version ${project.version} - 2019/9/3.
 */
public abstract class DelivererConnectorListener<T> {

  protected BeanFactory beanFactory;

  public DelivererConnectorListener(BeanFactory beanFactory) {
    this.beanFactory = beanFactory;
  }

  /**
   * Deliverer Connector Publish Event(s)
   *
   * @param delivererEvent instance of {@link DelivererEvent}
   * @param payload event payload
   * @see DelivererEvent#TIMED_DELIVERER_MESSAGES_EVENT 投递服务器定时推送待投递消息列表事件
   */
  public abstract void invoke(DelivererEvent delivererEvent, T payload);
}
