/*
 * Copyright 1999-2018 Acmedcare+ Holding Ltd.
 * Licensed under the Apache License, Version 2.0 (the "License");
 */

package com.acmedcare.framework.newim.deliver;

import com.acmedcare.framework.newim.deliver.context.ConnectorContext;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.beans.factory.BeanFactoryAware;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.EnvironmentAware;
import org.springframework.core.env.Environment;

/**
 * {@link DefaultDelivererConfiguration}
 *
 * @author <a href="mailto:iskp.me@gmail.com">Elve.Xu</a>
 * @version ${project.version} - 2019-07-29.
 */
public class DefaultDelivererConfiguration
    implements ApplicationContextAware, BeanFactoryAware, EnvironmentAware {

  private Environment environment;

  private BeanFactory beanFactory;

  private ApplicationContext applicationContext;

  @Override
  public void setBeanFactory(BeanFactory beanFactory) throws BeansException {
    this.beanFactory = beanFactory;
  }

  @Override
  public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
    this.applicationContext = applicationContext;
    ConnectorContext.context()
        .registerApplicationContext(
            (ConfigurableApplicationContext) applicationContext, beanFactory, environment);
  }

  /**
   * Set the {@code Environment} that this component runs in.
   *
   * @param environment
   */
  @Override
  public void setEnvironment(Environment environment) {
    this.environment = environment;
  }
}
