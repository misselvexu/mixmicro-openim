/*
 * Copyright 1999-2018 Acmedcare+ Holding Ltd.
 * Licensed under the Apache License, Version 2.0 (the "License");
 */

package com.acmedcare.framework.newim.deliver.connector.client.executor;

import com.acmedcare.framework.kits.retry.AsyncCallExecutor;
import com.acmedcare.framework.kits.retry.CallExecutorBuilder;
import com.acmedcare.framework.kits.retry.config.RetryConfig;
import com.acmedcare.framework.kits.retry.config.RetryConfigBuilder;
import com.acmedcare.framework.kits.thread.ThreadKit;
import com.acmedcare.framework.newim.Message;
import com.acmedcare.framework.newim.deliver.api.RemotingDelivererApi;
import com.acmedcare.framework.newim.deliver.api.bean.DelivererMessageBean;
import com.acmedcare.framework.newim.deliver.api.exception.RemotingDelivererException;
import com.acmedcare.framework.newim.deliver.connector.client.DelivererClientProperties;
import com.acmedcare.framework.newim.deliver.context.ConnectorContext;
import com.alibaba.fastjson.JSON;
import io.netty.util.concurrent.DefaultThreadFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.Duration;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

/**
 * {@link DelivererMessageExecutor}
 *
 * @author <a href="mailto:iskp.me@gmail.com">Elve.Xu</a>
 * @version ${project.version} - 2019-08-15.
 */
public class DelivererMessageExecutor {

  private static final Logger log =
      LoggerFactory.getLogger(DelivererMessageExecutor.class);

  private final DelivererClientProperties delivererClientProperties;

  private LinkedBlockingQueue<DelivererMessageBean> messageBeanLinkedBlockingQueue;

  private final RemotingDelivererApi remotingDelivererApi;

  private ExecutorService postExecutor;

  public DelivererMessageExecutor(DelivererClientProperties delivererClientProperties) {
    this.delivererClientProperties = delivererClientProperties;

    // remoting deliverer api
    this.remotingDelivererApi =
        Optional.ofNullable(ConnectorContext.context().remotingDelivererApi())
            .orElseThrow(RemotingDelivererException::new);
  }

  public void init() {
    log.info("[==] Startup Submit-Deliverer executor ");
    this.messageBeanLinkedBlockingQueue = new LinkedBlockingQueue<>(this.delivererClientProperties.getPostQueueCapacity());

    log.info("[==] init deliverer message cached queue , {} ,capacity: {}", this.messageBeanLinkedBlockingQueue, this.delivererClientProperties.getPostQueueCapacity());

    this.buildExecutor();
  }

  public void destroy() {
    log.info("[==] Shutdown Submit-Deliverer executor ");
    if(postExecutor != null) {
      ThreadKit.gracefulShutdown(postExecutor,60,60 * 2,TimeUnit.SECONDS);
    }
  }

  private void buildExecutor() {
    if(postExecutor == null) {
      this.postExecutor =
          new ThreadPoolExecutor(
              this.delivererClientProperties.getPostQueueThreadNum(),
              this.delivererClientProperties.getPostQueueThreadNum(),
              0L,
              TimeUnit.MILLISECONDS,
              new LinkedBlockingQueue<>(),
              new DefaultThreadFactory("POST-DELIVERER-MESSAGE-EXECUTOR"));
    }

    for (int i = 0; i < this.delivererClientProperties.getPostQueueThreadNum(); i++) {
      this.postExecutor.execute(this::doPost);
    }
  }

  private void doPost() {
    while (true) {
      DelivererMessageBean messageBean = null;
      try {
        messageBean = messageBeanLinkedBlockingQueue.take();

        // do post
        invokeRemoting(messageBean);

      } catch (Exception e) {
        log.warn("[==] post failed, try next round.");

        if(messageBean == null) {
          return;
        }

        if(!this.delivererClientProperties.isPostRequestFastFailEnabled()) {
          try {
            messageBeanLinkedBlockingQueue.put(messageBean);
          } catch (InterruptedException ignored) {
          }
        } else {
          // fast fail
          RetryConfig retryConfig =
              new RetryConfigBuilder()
                  .retryOnAnyException()
                  .withDelayBetweenTries(
                      Duration.of(
                          this.delivererClientProperties.getFastFailRetryDelay(),
                          ChronoUnit.MILLIS))
                  .withMaxNumberOfTries(this.delivererClientProperties.getMaxFastFailedRetryTimes())
                  .build();

          AsyncCallExecutor<Object> executor =
              new CallExecutorBuilder<>()
                  .config(retryConfig)
                  .onCompletionListener(
                      status -> log.info("[==] Post Retry Executor , post result: {}", status))
                  .buildAsync();

          DelivererMessageBean finalMessageBean = messageBean;
          executor.execute(
              () -> {
                invokeRemoting(finalMessageBean);
                // ignore result
                return null;
              });
        }
      }
    }
  }

  private void invokeRemoting(DelivererMessageBean messageBean) {
    this.remotingDelivererApi.postDelivererMessage(
        messageBean.isHalf(),
        messageBean.getNamespace(),
        messageBean.getPassportId(),
        messageBean.getMessageType(),
        messageBean.getMessage());
  }


  // ===== Deliverer Service Process Methods ======

  /**
   * 上报头投递消息
   */
  public void submitDelivererMessage(
      boolean half,
      String namespace,
      String passportId,
      Message.MessageType messageType,
      byte[] message) {
    try {

      Message originMessage = JSON.parseObject(message, Message.class);

      this.messageBeanLinkedBlockingQueue.put(
          DelivererMessageBean.builder()
              .half(half)
              .message(message)
              .messageType(messageType)
              .namespace(namespace)
              .passportId(passportId)
              .messageId(originMessage.getMid().toString())
              .build());

      log.info("[==] Deliverer Client submit-ed message content :{}", JSON.toJSONString(originMessage));

    } catch (Exception e) {
      log.warn("[==] Deliverer Client submit deliverer message failed.", e);
    }
  }

  /**
   * 提交投递消息
   */
  public void commitDelivererMessage(String namespace, String messageId, String passportId) {

    // fast fail
    RetryConfig retryConfig =
        new RetryConfigBuilder()
            .retryOnAnyException()
            .withDelayBetweenTries(
                Duration.of(
                    this.delivererClientProperties.getFastFailRetryDelay(),
                    ChronoUnit.MILLIS))
            .withMaxNumberOfTries(this.delivererClientProperties.getMaxFastFailedRetryTimes())
            .build();

    AsyncCallExecutor<Object> executor =
        new CallExecutorBuilder<>()
            .config(retryConfig)
            .onCompletionListener(
                status -> log.info("[==] Submit Deliverer Ack Retry Executor , Submit Deliverer Ack result: {}", status))
            .buildAsync();

    executor.execute(
        () -> {
          DelivererMessageExecutor.this.remotingDelivererApi.commitDelivererMessage(namespace,messageId,passportId);
          // ignore result
          return null;
        });

  }

  public List<DelivererMessageBean> fetchClientDelivererMessage(String namespace, String passportId, Message.MessageType messageType) {
    return this.remotingDelivererApi.fetchClientDelivererMessage(namespace,passportId,messageType);
  }
}
