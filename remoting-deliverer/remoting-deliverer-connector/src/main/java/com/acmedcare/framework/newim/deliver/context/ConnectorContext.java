/*
 * Copyright 1999-2018 Acmedcare+ Holding Ltd.
 * Licensed under the Apache License, Version 2.0 (the "License");
 */

package com.acmedcare.framework.newim.deliver.context;

import com.acmedcare.framework.kits.Assert;
import com.acmedcare.framework.kits.executor.AsyncRuntimeExecutor;
import com.acmedcare.framework.kits.lang.NonNull;
import com.acmedcare.framework.kits.lang.Nullable;
import com.acmedcare.framework.newim.BizResult;
import com.acmedcare.framework.newim.deliver.api.RemotingDelivererApi;
import com.acmedcare.framework.newim.deliver.api.bean.DelivererMessageBean;
import com.acmedcare.framework.newim.deliver.api.exception.NoAvailableDelivererServerInstanceException;
import com.acmedcare.framework.newim.deliver.api.request.TimedDelivererMessageRequestBean;
import com.acmedcare.framework.newim.deliver.connector.listener.DelivererConnectorListener;
import com.acmedcare.framework.newim.deliver.connector.listener.event.DelivererEvent;
import com.acmedcare.framework.newim.deliver.connector.server.DelivererServerProperties;
import com.acmedcare.framework.newim.spi.ExtensionLoader;
import com.acmedcare.framework.newim.spi.ExtensionLoaderFactory;
import com.acmedcare.tiffany.framework.remoting.RemotingSocketServer;
import com.acmedcare.tiffany.framework.remoting.protocol.RemotingCommand;
import com.alibaba.fastjson.JSON;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import io.netty.channel.Channel;
import io.netty.channel.ChannelHandlerContext;
import io.netty.util.Attribute;
import io.netty.util.AttributeKey;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.core.env.Environment;

import java.util.List;
import java.util.Map;
import java.util.Set;

import static com.acmedcare.framework.newim.deliver.api.DelivererCommand.TIMED_DELIVERY_MESSAGE_COMMAND_VALUE;
import static com.acmedcare.framework.newim.deliver.context.ConnectorInstance.Type.CLIENT;

/**
 * {@link ConnectorContext}
 *
 * @author <a href="mailto:iskp.me@gmail.com">Elve.Xu</a>
 * @version ${project.version} - 2019-07-29.
 */
@SuppressWarnings({"unchecked", "ConstantConditions", "unused"})
public class ConnectorContext {

  private static final Logger log = LoggerFactory.getLogger(ConnectorContext.class);

  // ===== Defined Properties ======

  public static final AttributeKey<ConnectorInstance.ConnectorClientInstance> CONNECTOR_REMOTING_ATTRIBUTE_KEY = AttributeKey.valueOf("CONNECTOR_REMOTING_INSTANCE_KEY");

  private static final String DELIVERER_API_DEFAULT_EXTENSION_NAME = "normal";

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
  private Map<ConnectorInstance.Type, Set<ConnectorInstance>> session = Maps.newConcurrentMap();

  /**
   * Server Connection Instance
   *
   * <pre>
   *
   *
   * </pre>
   */
  private Map<ConnectorInstance.ConnectorServerInstance, ConnectorConnection> serverConnections = Maps.newConcurrentMap();

  /**
   * Deliverer Connector Registered Event Listener(s)
   *
   * <pre>
   *
   * </pre>
   */
  private List<DelivererConnectorListener<Object>> connectorListeners = Lists.newLinkedList();

  // ===== Context Core =====

  public void register(ConnectorInstance instance) {

    if (instance != null) {

      if (instance instanceof ConnectorInstance.ConnectorClientInstance) {

        ConnectorInstance.ConnectorClientInstance clientInstance = (ConnectorInstance.ConnectorClientInstance) instance;

        if(!session.containsKey(CLIENT)) {
          session.put(CLIENT, Sets.newHashSet(clientInstance));
        } else {
          if(!session.get(CLIENT).add(clientInstance)){
            log.warn("[==] Deliverer Context , register connector client instance failed , instance: {}" ,instance);
          }
        }
      }

      if (instance instanceof ConnectorInstance.ConnectorServerInstance) {

        ConnectorInstance.ConnectorServerInstance serverInstance =(ConnectorInstance.ConnectorServerInstance) instance;

        if(serverConnections.containsKey(serverInstance)) {

          log.warn("[==] Deliverer Context , server connector instance is register-ed , instance: {} , ignore ." ,instance);

        } else {
          ConnectorConnection originalConnection = serverConnections.put( serverInstance, ConnectorConnection.builder().serverInstance(serverInstance).build());

          if(originalConnection != null) {
            originalConnection.release();
          }

        }

      }
    }
  }

  public ConnectorConnection getConnection(ConnectorInstance.ConnectorServerInstance instance) {
    return serverConnections.get(instance);
  }

  /**
   * Get Available Deliverer Server Instance From cache
   * @return instance of {@link ConnectorConnection}
   * @throws NoAvailableDelivererServerInstanceException maybe thrown no-available-deliverer-server-instance exception
   */
  public ConnectorConnection getAvailableConnection() throws NoAvailableDelivererServerInstanceException {
    for(Map.Entry<ConnectorInstance.ConnectorServerInstance, ConnectorConnection> entry : serverConnections.entrySet()) {
      if(entry.getValue().isRunning()) {
        return entry.getValue();
      }
    }
    throw new NoAvailableDelivererServerInstanceException();
  }

  /**
   * Check Deliverer Server Context is available .
   *
   * @return true / false
   */
  public boolean isDelivererContextAvailable() {
    Set<?> clients = this.session.get(CLIENT);
    return this.server != null && clients != null && !clients.isEmpty();
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

        ConnectorInstance.ConnectorServerInstance serverInstance = (ConnectorInstance.ConnectorServerInstance) instance;

        ConnectorConnection connection = serverConnections.get(serverInstance);

        if(connection != null) {

          try{
            connection.disconnect();
          } catch (Exception e) {
            log.warn("[==] Deliverer Context , server instance:[{}] connection:[{}] -> disconnect exception :{} " , serverInstance , connection ,e.getMessage());
          } finally{
            connection.release();
          }
        }
      }

      if (instance instanceof ConnectorInstance.ConnectorClientInstance) {

        ConnectorInstance.ConnectorClientInstance clientInstance = (ConnectorInstance.ConnectorClientInstance) instance;

        boolean removed = session.get(CLIENT).remove(clientInstance);

        log.info("[==] Deliverer Context , released connector instance: {} , result: {}" , clientInstance,removed);
      }
    }
  }

  /**
   * Publish Event
   * @param event event type
   * @param payload event payload
   * @see DelivererEvent
   */
  public void publishEvent(@NonNull DelivererEvent event, @Nullable Object payload) {
    if(!connectorListeners.isEmpty() && event != null) {
      for (DelivererConnectorListener<Object> connectorListener : connectorListeners) {
        try{
          connectorListener.invoke(event,payload);
        } catch (Exception e) {
          log.warn("[==] Deliverer Context publish event , real biz process execute failed. ",e);
        }
      }
    }
  }

  // ===== Common Methods ======

  public static @Nullable <T> T parseChannel(@NonNull ChannelHandlerContext context, @NonNull AttributeKey<?> key, @NonNull Class<T> clazz) {
    if (context != null) {
      return parseChannel(context.channel(), key, clazz);
    }
    return null;
  }

  public static <T> T parseChannel(@NonNull Channel channel, @NonNull AttributeKey<?> key, @NonNull Class<T> clazz) {
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

  /**
   * Positive Deliverer Messages
   *
   * @param delivererMessages message's list
   */
  public void positiveDelivererMessages(List<DelivererMessageBean> delivererMessages) {

    try{

      Assert.notNull(this.server, "[==] Deliverer Context's server instance must not be NULL.");

      if(session.containsKey(CLIENT)){

        Set<?> connections = session.get(CLIENT);

        if(!connections.isEmpty()) {

          // async
          AsyncRuntimeExecutor.getAsyncThreadPool().execute(()->{

            // transform beans
            List<TimedDelivererMessageRequestBean.TimedMessage> messages = Lists.newArrayList();
            delivererMessages.forEach(delivererMessageBean -> {
              TimedDelivererMessageRequestBean.TimedMessage message = TimedDelivererMessageRequestBean.TimedMessage.builder()
                  .message(delivererMessageBean.getMessage())
                  .messageId(delivererMessageBean.getMessageId())
                  .messageType(delivererMessageBean.getMessageType())
                  .namespace(delivererMessageBean.getNamespace())
                  .passportId(delivererMessageBean.getPassportId())
                  .build();
              messages.add(message);
            });

            for (Object connection : connections) {

              try{

                if(connection instanceof ConnectorInstance.ConnectorClientInstance) {

                  ConnectorInstance.ConnectorClientInstance clientInstance = (ConnectorInstance.ConnectorClientInstance) connection;

                  RemotingCommand command = RemotingCommand.createRequestCommand(TIMED_DELIVERY_MESSAGE_COMMAND_VALUE,null);

                  TimedDelivererMessageRequestBean bean = TimedDelivererMessageRequestBean.builder().messages(messages).build();

                  command.setBody(JSON.toJSONBytes(bean));

                  Channel channel = clientInstance.getChannel();

                  ConnectorContext.this.server.invokeAsync(
                      channel,
                      command,
                      ConnectorContext.this.properties.getRequestTimeout(),
                      responseFuture -> {

                        // response execute
                        if(responseFuture.isSendRequestOK()) {

                          RemotingCommand response = responseFuture.getResponseCommand();

                          byte[] body = response.getBody();

                          if(body != null) {
                            BizResult bizResult = BizResult.fromBytes(body,BizResult.class);
                            log.info("[==] Deliverer Context Timed Distribute Response,{} : {}" ,channel, bizResult.json());
                          } else {
                            log.warn("[==] Deliverer Context Timed Distribute, response,{} body is empty ." ,channel);
                          }

                        } else {
                          log.warn("[==] Deliverer Context Timed Distribute, response,{} : {}" ,channel ,responseFuture.toString());
                        }
                  });

                }

              } catch (Exception e) {
                log.warn("[==] Deliverer Context , positive distribute messages failed with exception ",e);
              }
            }
          });
        }
      }
    } catch (Exception e) {
      log.warn("[==] Deliverer Context , positive distribute messages failed with exception ",e);
    }

  }

  // ===== Deliverer Server Context Instance Defined =====

  private RemotingSocketServer server;
  private DelivererServerProperties properties;

  public void registerServerInstance(RemotingSocketServer server, DelivererServerProperties properties) {
    this.server = server;
    this.properties = properties;
  }

  public void registerConnectorListener(DelivererConnectorListener listener) {
    this.connectorListeners.add(listener);
  }

  //  ===== Bean Private Constructor Defined  =====

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

  public void registerApplicationContext(ConfigurableApplicationContext context, BeanFactory beanFactory, Environment environment) {
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

  // ===== Spring Bean Factory =====

  public <T> T getBean(String name, Class<T> clazz) throws BeansException {
    return this.context.getBean(name,clazz);
  }

  public <T> T getBean(Class<T> clazz) throws BeansException {
    return this.context.getBean(clazz);
  }

  // ===== SPI Factory Operations ======

  public @Nullable RemotingDelivererApi remotingDelivererApi() {
    return remotingDelivererApi(DELIVERER_API_DEFAULT_EXTENSION_NAME);
  }

  public @Nullable RemotingDelivererApi remotingDelivererApi(String extensionName) {
    return this.remotingDelivererApiExtensionLoader.getExtension(extensionName);
  }
}
