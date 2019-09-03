/*
 * Copyright 1999-2018 Acmedcare+ Holding Ltd.
 * Licensed under the Apache License, Version 2.0 (the "License");
 */

package com.acmedcare.framework.newim.deliver.samples;

import com.acmedcare.framework.newim.deliver.connector.listener.DelivererConnectorListener;
import com.acmedcare.framework.newim.deliver.connector.listener.event.DelivererEvent;
import org.springframework.beans.factory.BeanFactory;

/**
 * {@link SimpleDelivererConnectorListener}
 *
 * @author <a href="mailto:iskp.me@gmail.com">Elve.Xu</a>
 * @version ${project.version} - 2019/9/3.
 */
public class SimpleDelivererConnectorListener extends DelivererConnectorListener<Object> {

  public SimpleDelivererConnectorListener(BeanFactory beanFactory) {
    super(beanFactory);
  }

  /**
   * Deliverer Connector Publish Event(s)
   *
   * @param delivererEvent instance of {@link DelivererEvent}
   * @param payload        event payload
   */
  @Override
  public void invoke(DelivererEvent delivererEvent, Object payload) {

  }
}
