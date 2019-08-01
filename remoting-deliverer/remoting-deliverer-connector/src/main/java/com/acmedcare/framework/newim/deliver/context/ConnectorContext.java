/*
 * Copyright 1999-2018 Acmedcare+ Holding Ltd.
 * Licensed under the Apache License, Version 2.0 (the "License");
 */

package com.acmedcare.framework.newim.deliver.context;

import com.acmedcare.framework.kits.lang.NonNull;
import com.acmedcare.framework.kits.lang.Nullable;
import com.acmedcare.framework.newim.deliver.api.RemotingDelivererApi;
import com.acmedcare.framework.newim.spi.ExtensionLoader;
import com.acmedcare.framework.newim.spi.ExtensionLoaderFactory;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import io.netty.channel.Channel;
import io.netty.channel.ChannelHandlerContext;
import io.netty.util.Attribute;
import io.netty.util.AttributeKey;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.core.env.Environment;

import java.util.List;
import java.util.Map;

import static com.acmedcare.framework.newim.deliver.context.ConnectorInstance.Type.CLIENT;

/**
 * {@link ConnectorContext}
 *
 * @author <a href="mailto:iskp.me@gmail.com">Elve.Xu</a>
 * @version ${project.version} - 2019-07-29.
 */
@SuppressWarnings({"unchecked", "ConstantConditions"})
public class ConnectorContext {

  private static final Logger log = LoggerFactory.getLogger(ConnectorContext.class);

  // ===== Defined Properties ======

  public static final AttributeKey<? extends ConnectorInstance> CONNECTOR_REMOTING_ATTRIBUTE_KEY =
      AttributeKey.valueOf("CONNECTOR_REMOTING_INSTANCE_KEY");

  private static final String DELIVERER_API_DEFAULT_EXTENSION_NAME = "default";
  private ExtensionLoader<RemotingDelivererApi> remotingDelivererApiExtensionLoader;

  // ===== Session Core =====

  /**
   * Connector Connected Session Cache .
   *
   * <pre>
   *    Key:Type     -     Value:Map
   *    SERVER             Key:ConnectorServerInstanceA   ->  Channel Instance
   *                       Key:ConnectorServerInstanceB   ->  Channel Instance
   *
   *    CLIENT             Key:ConnectorClientInstanceA   ->  Channel Instance
   * </pre>
   */
  private Map<ConnectorInstance.Type, List<ConnectorInstance>> session =
      Maps.newConcurrentMap();

  // ===== Context Core =====

  public void register(ConnectorInstance instance) {

    if (instance != null) {

      if (instance instanceof ConnectorInstance.ConnectorClientInstance) {

        ConnectorInstance.ConnectorClientInstance clientInstance =
            (ConnectorInstance.ConnectorClientInstance) instance;

        if(session.containsKey(CLIENT)) {
          session.put(CLIENT, Lists.newArrayList(clientInstance));
        } else {
          if(!session.get(CLIENT).add(clientInstance)){
            log.warn("[==] Deliverer Context , register connector client instance failed , instance: {}" ,instance);
          }
        }
      }

      if (instance instanceof ConnectorInstance.ConnectorServerInstance) {
        // TODO ready to register server instance
      }
    }
  }

  /**
   * Release Deliverer Connector Instance
   *
   * @param instance instance of {@link ConnectorInstance}
   * @see com.acmedcare.framework.newim.deliver.context.ConnectorInstance.ConnectorServerInstance
   * @see com.acmedcare.framework.newim.deliver.context.ConnectorInstance.ConnectorClientInstance
   */
  public void release(ConnectorInstance instance) {

    if (instance != null) {

      if (instance instanceof ConnectorInstance.ConnectorServerInstance) {

        ConnectorInstance.ConnectorServerInstance serverInstance =
            (ConnectorInstance.ConnectorServerInstance) instance;

        // TODO release

      }
    }
  }

  // ===== Common Methods ======

  public static @Nullable <T> T parseChannel(
      @NonNull ChannelHandlerContext context,
      @NonNull AttributeKey<?> key,
      @NonNull Class<T> clazz) {
    if (context != null) {
      return parseChannel(context.channel(), key, clazz);
    }
    return null;
  }

  public static <T> T parseChannel(
      @NonNull Channel channel, @NonNull AttributeKey<?> key, @NonNull Class<T> clazz) {
    try {
      if (channel != null) {
        if (key != null) {
          Attribute<?> attribute = channel.attr(key);
          if (attribute != null) {
            return (T) attribute.get();
          }
        }
      }
    } catch (Exception ignore) {
    }
    return null;
  }

  private static class InstanceHolder {
    private static final ConnectorContext CONNECTOR_CONTEXT = new ConnectorContext();
  }

  private ConnectorContext() {}

  public static ConnectorContext context() {
    return InstanceHolder.CONNECTOR_CONTEXT;
  }

  // ==== Spring Context Instance ======

  private ConfigurableApplicationContext context;
  private BeanFactory beanFactory;
  private Environment environment;

  public void registerApplicationContext(
      ConfigurableApplicationContext context, BeanFactory beanFactory, Environment environment) {
    this.context = context;
    this.beanFactory = beanFactory;
    this.environment = environment;
    log.info("[==] Ready to build SPI Extension Bean(s)...");
    this.remotingDelivererApiExtensionLoader = this.buildRemotingDelivererApiExtensionLoader();
  }

  private ExtensionLoader<RemotingDelivererApi> buildRemotingDelivererApiExtensionLoader() {
    return ExtensionLoaderFactory.getExtensionLoader(
        RemotingDelivererApi.class,
        extensionClass -> {
          RemotingDelivererApi remotingDelivererApi = extensionClass.getExtInstance();
          log.info("[==] SPI Extension Bean: {} is init-ed.", remotingDelivererApi);
        });
  }

  // ===== SPI Factory Operations ======

  public @Nullable RemotingDelivererApi remotingDelivererApi() {
    return remotingDelivererApi(DELIVERER_API_DEFAULT_EXTENSION_NAME);
  }

  public @Nullable RemotingDelivererApi remotingDelivererApi(String extensionName) {
    return this.remotingDelivererApiExtensionLoader.getExtension(extensionName);
  }
}
