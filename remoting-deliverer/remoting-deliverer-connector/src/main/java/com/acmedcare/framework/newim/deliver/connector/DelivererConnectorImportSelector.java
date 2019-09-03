/*
 * Copyright 1999-2018 Acmedcare+ Holding Ltd.
 * Licensed under the Apache License, Version 2.0 (the "License");
 */

package com.acmedcare.framework.newim.deliver.connector;

import com.acmedcare.framework.newim.deliver.DefaultDelivererConfiguration;
import com.acmedcare.framework.newim.deliver.connector.client.DelivererClientMarkerConfiguration;
import com.acmedcare.framework.newim.deliver.connector.listener.DelivererConnectorListener;
import com.acmedcare.framework.newim.deliver.connector.server.DelivererServerMarkerConfiguration;
import com.acmedcare.framework.newim.deliver.context.ConnectorContext;
import com.acmedcare.framework.newim.spi.util.Assert;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.BeanClassLoaderAware;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.beans.factory.BeanFactoryAware;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.context.EnvironmentAware;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.ImportSelector;
import org.springframework.core.env.ConfigurableEnvironment;
import org.springframework.core.env.Environment;
import org.springframework.core.env.MapPropertySource;
import org.springframework.core.type.AnnotationMetadata;
import org.springframework.util.ClassUtils;
import org.springframework.util.ReflectionUtils;

import java.util.List;
import java.util.Map;

/**
 * {@link DelivererConnectorImportSelector}
 *
 * @author <a href="mailto:iskp.me@gmail.com">Elve.Xu</a>
 * @version ${project.version} - 2019-07-29.
 */
public class DelivererConnectorImportSelector
    implements ImportSelector, BeanClassLoaderAware, EnvironmentAware, BeanFactoryAware {

  private static final Logger log = LoggerFactory.getLogger(DelivererConnectorImportSelector.class);

  /** classLoader the owning class loader */
  private ClassLoader classLoader;

  /** Set the {@code Environment} that this component runs in. */
  private Environment environment;

  /** Set the {@code BeanFactory} that this component runs in. */
  private BeanFactory beanFactory;

  private static final String DELIVERER_CLIENT_ENABLED_FIELD_NAME = "enabledClient";

  private static final String DELIVERER_SERVER_ENABLED_FIELD_NAME = "enabledServer";

  private static final String DELIVERER_LISTENERS_FIELD_NAME = "listeners";

  private static final String DELIVERER_CLIENT_ENV_PROPERTIES_KEY = "remoting.deliverer.client.enabled";

  private static final String DELIVERER_SERVER_ENV_PROPERTIES_KEY = "remoting.deliverer.server.enabled";

  private static final String DELIVERER_PROPERTIES_ENV_NAME = "remotingDelivererAutoConfigureProperties";

  /**
   * Select and return the names of which class(es) should be imported based on the {@link
   * AnnotationMetadata} of the importing @{@link Configuration} class.
   */
  @SuppressWarnings("unchecked")
  @Override
  public String[] selectImports(AnnotationMetadata annotationMetadata) {

    log.info("[==] Deliverer Auto Configure Starting ...");

    Map<String, Object> map = annotationMetadata.getAnnotationAttributes(EnableDelivererConnector.class.getName());

    Assert.notNull(map, "Remoting Deliverer Annotation Properties Must Not Be NULL.");

    List<String> imports = Lists.newArrayList(DefaultDelivererConfiguration.class.getName());

    Map<String, Object> objectMap = Maps.newHashMap();
    map.keySet()
        .parallelStream()
        .forEach(
            s -> {
              if (s.equalsIgnoreCase(DELIVERER_CLIENT_ENABLED_FIELD_NAME)) {
                boolean delivererClientEnabled =
                    Boolean.parseBoolean(
                        map.getOrDefault(DELIVERER_CLIENT_ENABLED_FIELD_NAME, "false").toString());
                if (delivererClientEnabled) {
                  imports.add(DelivererClientMarkerConfiguration.class.getName());
                }
                objectMap.put(DELIVERER_CLIENT_ENV_PROPERTIES_KEY, delivererClientEnabled);
              }

              if (s.equalsIgnoreCase(DELIVERER_SERVER_ENABLED_FIELD_NAME)) {
                boolean delivererServerEnabled =
                    Boolean.parseBoolean(
                        map.getOrDefault(DELIVERER_SERVER_ENABLED_FIELD_NAME, "false").toString());
                if (delivererServerEnabled) {
                  imports.add(DelivererServerMarkerConfiguration.class.getName());
                }
                objectMap.put(DELIVERER_SERVER_ENV_PROPERTIES_KEY, delivererServerEnabled);
              }
            });

    MapPropertySource source = new MapPropertySource(DELIVERER_PROPERTIES_ENV_NAME, objectMap);
    ConfigurableEnvironment configurableEnvironment = (ConfigurableEnvironment) environment;
    configurableEnvironment.getPropertySources().addLast(source);

    log.info("[==] Deliverer Register Listener Bean Starting ...");

    Assert.notNull(map, " Deliverer Register Bean Definition's Annotation Properties Must Not Be NULL.");

    if (map.containsKey(DELIVERER_LISTENERS_FIELD_NAME)) {

      Class<? extends DelivererConnectorListener>[] listeners = (Class<? extends DelivererConnectorListener>[]) map.get(DELIVERER_LISTENERS_FIELD_NAME);

      for (Class<? extends DelivererConnectorListener> listener : listeners) {
        ConnectorContext.context().registerConnectorListener(instantiateListener(listener.getName(),listener,classLoader));
      }

      log.info("[==] Deliverer Register Listener Bean finished .");
    }

    return imports.toArray(new String[0]);
  }

  /**
   * Instantiate Listener
   * @param instanceClassName instance class name
   * @param listenerClass listener class
   * @param classLoader classloader
   * @param <T> object type
   * @return instance
   */
  @SuppressWarnings("unchecked")
  private <T> T instantiateListener(String instanceClassName, Class<T> listenerClass, ClassLoader classLoader) {
    try {
      Class<?> instanceClass = ClassUtils.forName(instanceClassName, classLoader);
      if (!listenerClass.isAssignableFrom(instanceClass)) {
        throw new IllegalArgumentException(
            "[==] Class [" + instanceClassName + "] is not assignable to [" + listenerClass.getName() + "]");
      }
      return (T) ReflectionUtils.accessibleConstructor(instanceClass).newInstance(this.beanFactory);
    }
    catch (Throwable ex) {
      throw new IllegalArgumentException("[==] Unable to instantiate listener class: " + listenerClass.getName(), ex);
    }
  }

  @Override
  public void setBeanClassLoader(ClassLoader classLoader) {
    this.classLoader = classLoader;
  }

  @Override
  public void setEnvironment(Environment environment) {
    this.environment = environment;
  }

  /**
   * Callback that supplies the owning factory to a bean instance.
   * <p>Invoked after the population of normal bean properties
   * but before an initialization callback such as
   * {@link InitializingBean#afterPropertiesSet()} or a custom init-method.
   *
   * @param beanFactory owning BeanFactory (never {@code null}).
   *                    The bean can immediately call methods on the factory.
   * @throws BeansException in case of initialization errors
   * @see org.springframework.beans.factory.BeanInitializationException
   */
  @Override
  public void setBeanFactory(BeanFactory beanFactory) throws BeansException {
    this.beanFactory = beanFactory;
  }
}
