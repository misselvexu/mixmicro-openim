/*
 * Copyright 2014-2019 Acmedcare+ Holding Ltd.
 * Licensed under the Apache License, Version 2.0 (the "License");
 */

package com.acmedcare.framework.newim.env.core;

import io.netty.util.concurrent.DefaultThreadFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.WebApplicationType;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.ApplicationListener;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * Awaiting NonWebApplication Listener
 *
 * @author <a href="mailto:iskp.me@gmail.com">Elve.Xu</a>
 * @version ${project.version} - 2018-12-12.
 */
@SuppressWarnings({"AlibabaAvoidManuallyCreateThread"})
public class AwaitingNonWebApplicationListener implements ApplicationListener<ApplicationReadyEvent> {

  private static final Logger logger = LoggerFactory.getLogger(AwaitingNonWebApplicationListener.class);

  private static final ExecutorService THREAD_POOL_EXECUTOR =
      new ThreadPoolExecutor(1, 1, 0L, TimeUnit.MILLISECONDS, new LinkedBlockingQueue<>(1),
          new DefaultThreadFactory("THREAD_POOL_EXECUTOR_POOL"));

  private static final AtomicBoolean SHUTDOWN_HOOK_REGISTERED = new AtomicBoolean(false);

  private static final AtomicBoolean ATOMIC_BOOLEAN = new AtomicBoolean(false);

  @Override
  public void onApplicationEvent(ApplicationReadyEvent event) {

    final SpringApplication springApplication = event.getSpringApplication();

    // Check Application Type , if none , just return.
    if (!WebApplicationType.NONE.equals(springApplication.getWebApplicationType())) {
      return;
    }

    THREAD_POOL_EXECUTOR.execute(
        () -> {
          synchronized (springApplication) {
            if (logger.isInfoEnabled()) {
              logger.info(" [Remoting] Current Spring Boot Application is await...");
            }
            while (!ATOMIC_BOOLEAN.get()) {
              try {
                springApplication.wait();
              } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
              }
            }
          }
        });

    // register ShutdownHook
    if (SHUTDOWN_HOOK_REGISTERED.compareAndSet(false, true)) {
      registerShutdownHook(
          new Thread(
              () -> {
                synchronized (springApplication) {
                  if (ATOMIC_BOOLEAN.compareAndSet(false, true)) {
                    springApplication.notifyAll();
                    if (logger.isInfoEnabled()) {
                      logger.info(
                          " [Remoting] Current Spring Boot Application is about to shutdown...");
                    }
                    // Shutdown THREAD_POOL_EXECUTOR
                    THREAD_POOL_EXECUTOR.shutdown();
                  }
                }
              }));
    }
  }

  private void registerShutdownHook(Thread thread) {
    Runtime.getRuntime().addShutdownHook(thread);
  }
}
