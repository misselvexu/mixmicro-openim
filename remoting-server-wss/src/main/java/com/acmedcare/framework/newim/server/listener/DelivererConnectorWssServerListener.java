/*
 * Copyright 1999-2018 Acmedcare+ Holding Ltd.
 * Licensed under the Apache License, Version 2.0 (the "License");
 */

package com.acmedcare.framework.newim.server.listener;

import com.acmedcare.framework.kits.executor.AsyncRuntimeExecutor;
import com.acmedcare.framework.newim.deliver.api.request.TimedDelivererMessageRequestBean;
import com.acmedcare.framework.newim.deliver.connector.listener.DelivererConnectorListener;
import com.acmedcare.framework.newim.deliver.connector.listener.event.DelivererEvent;
import com.acmedcare.framework.newim.server.core.IMSession;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.BeanFactory;

import java.util.List;

/**
 * {@link DelivererConnectorWssServerListener}
 *
 * @author <a href="mailto:iskp.me@gmail.com">Elve.Xu</a>
 * @version ${project.version} - 2019/9/3.
 */
public class DelivererConnectorWssServerListener extends DelivererConnectorListener<Object> {

  private static final Logger log =
      LoggerFactory.getLogger(DelivererConnectorWssServerListener.class);

  public DelivererConnectorWssServerListener(BeanFactory beanFactory) {
    super(beanFactory);
  }

  /**
   * Deliverer Connector Publish Event(s)
   *
   * @param delivererEvent instance of {@link DelivererEvent}
   * @param payload event payload
   */
  @SuppressWarnings("unchecked")
  @Override
  public void invoke(DelivererEvent delivererEvent, Object payload) {

    log.info("[IM-WSS-SERVER] Received deliverer published event: {}", delivererEvent);

    switch (delivererEvent) {
      case TIMED_DELIVERER_MESSAGES_EVENT:
        List<TimedDelivererMessageRequestBean.TimedMessage> messages = (List<TimedDelivererMessageRequestBean.TimedMessage>) payload;

        if (messages != null && messages.size() > 0) {
          final IMSession imSession = this.beanFactory.getBean(IMSession.class);
          AsyncRuntimeExecutor.getAsyncThreadPool()
              .execute(
                  () -> {
                    for (TimedDelivererMessageRequestBean.TimedMessage message : messages) {
                      try{
                        imSession.sendMessageToPassport(false,message.getNamespace(),message.getPassportId(),message.getMessageType(),message.getMessage());
                      } catch (Exception e) {
                        e.printStackTrace();
                        log.warn("[IM-WSS-SERVER] send deliverer message: {}-{} to client: {} failed." , message.getMessageType(), message.getMessageId(), message.getPassportId());
                      }
                    }
                  });
          break;
        }

      default:
        log.warn("[IM-WSS-SERVER] Un-implements deliverer event : {}", delivererEvent);
        break;
    }
  }
}
