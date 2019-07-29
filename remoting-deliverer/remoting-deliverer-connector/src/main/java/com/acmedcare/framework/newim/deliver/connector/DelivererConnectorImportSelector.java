/*
 * Copyright 1999-2018 Acmedcare+ Holding Ltd.
 * Licensed under the Apache License, Version 2.0 (the "License");
 */

package com.acmedcare.framework.newim.deliver.connector;

import com.acmedcare.framework.newim.deliver.DefaultDelivererConfiguration;
import com.acmedcare.framework.newim.deliver.connector.client.DelivererClientMarkerConfiguration;
import com.acmedcare.framework.newim.deliver.connector.server.DelivererServerMarkerConfiguration;
import com.acmedcare.framework.newim.spi.util.Assert;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.BeanClassLoaderAware;
import org.springframework.context.EnvironmentAware;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.ImportSelector;
import org.springframework.core.env.ConfigurableEnvironment;
import org.springframework.core.env.Environment;
import org.springframework.core.env.MapPropertySource;
import org.springframework.core.type.AnnotationMetadata;

import java.util.List;
import java.util.Map;

/**
 * {@link DelivererConnectorImportSelector}
 *
 * @author <a href="mailto:iskp.me@gmail.com">Elve.Xu</a>
 * @version ${project.version} - 2019-07-29.
 */
public class DelivererConnectorImportSelector
    implements ImportSelector, BeanClassLoaderAware, EnvironmentAware {

  private static final Logger log = LoggerFactory.getLogger(DelivererConnectorImportSelector.class);

  /** classLoader the owning class loader */
  private ClassLoader classLoader;

  /** Set the {@code Environment} that this component runs in. */
  private Environment environment;

  private static final String DELIVERER_CLIENT_ENABLED_FIELD_NAME = "enabledClient";

  private static final String DELIVERER_SERVER_ENABLED_FIELD_NAME = "enabledServer";

  private static final String DELIVERER_CLIENT_ENV_PROPERTIES_KEY = "remoting.deliverer.client.enabled";

  private static final String DELIVERER_SERVER_ENV_PROPERTIES_KEY = "remoting.deliverer.server.enabled";

  private static final String DELIVERER_PROPERTIES_ENV_NAME = "remotingDelivererAutoConfigureProperties";

  /**
   * Select and return the names of which class(es) should be imported based on the {@link
   * AnnotationMetadata} of the importing @{@link Configuration} class.
   */
  @Override
  public String[] selectImports(AnnotationMetadata annotationMetadata) {

    log.info("[==] Deliverer Auto Configure Starting ...");

    Map<String, Object> map =
        annotationMetadata.getAnnotationAttributes(EnableDelivererConnector.class.getName());

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

    return imports.toArray(new String[0]);
  }

  @Override
  public void setBeanClassLoader(ClassLoader classLoader) {
    this.classLoader = classLoader;
  }

  @Override
  public void setEnvironment(Environment environment) {
    this.environment = environment;
  }
}
