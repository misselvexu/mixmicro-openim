/*
 * Copyright 1999-2018 Acmedcare+ Holding Ltd.
 * Licensed under the Apache License, Version 2.0 (the "License");
 */

package com.acmedcare.framework.newim.deliver.context;

import com.acmedcare.framework.kits.Assert;
import com.acmedcare.framework.kits.lang.NonNull;
import com.acmedcare.framework.kits.thread.ThreadKit;
import com.acmedcare.framework.newim.BizResult;
import com.acmedcare.framework.newim.deliver.api.exception.RemotingDelivererException;
import com.acmedcare.framework.newim.deliver.api.header.RegistryHeader;
import com.acmedcare.framework.newim.deliver.api.request.RegistryRequestBean;
import com.acmedcare.framework.newim.deliver.context.processor.TimedDeliveryMessageProcessor;
import com.acmedcare.tiffany.framework.remoting.ChannelEventListener;
import com.acmedcare.tiffany.framework.remoting.netty.NettyClientConfig;
import com.acmedcare.tiffany.framework.remoting.netty.NettyRemotingSocketClient;
import com.acmedcare.tiffany.framework.remoting.protocol.RemotingCommand;
import com.acmedcare.tiffany.framework.remoting.protocol.RemotingSysRequestCode;
import com.alibaba.fastjson.JSON;
import io.netty.channel.Channel;
import io.netty.util.concurrent.DefaultThreadFactory;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.NoArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.Serializable;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

import static com.acmedcare.framework.newim.deliver.api.DelivererCommand.*;

/**
 * {@link ConnectorConnection}
 *
 * @author <a href="mailto:iskp.me@gmail.com">Elve.Xu</a>
 * @version ${project.version} - 2019-08-07.
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class ConnectorConnection implements Serializable {

  private static final Logger log = LoggerFactory.getLogger(ConnectorConnection.class);

  private ConnectorInstance.ConnectorServerInstance serverInstance;

  @Builder(toBuilder = true)
  public ConnectorConnection(@NonNull ConnectorInstance.ConnectorServerInstance serverInstance) {
    Assert.notNull(serverInstance);
    this.serverInstance = serverInstance;
  }

  // ===== private local fields ========

  private AtomicBoolean initialized = new AtomicBoolean(false);

  private AtomicInteger requestFailedTimes = new AtomicInteger(0);

  private volatile boolean connecting = false;

  private ScheduledExecutorService heartbeatExecutor;

  private ScheduledExecutorService connectExecutor;

  private NettyRemotingSocketClient client;

  private NettyClientConfig config;

  // ===== Operations ======

  /**
   * Client connect remoting server instance
   *
   * @throws RemotingDelivererException maybe thrown {@link RemotingDelivererException}
   */
  public void connect() throws RemotingDelivererException {

    try{

      if (initialized.compareAndSet(false, true)) {

        if (config == null) {
          // build
          this.config = new NettyClientConfig();
          this.config.setUseTLS(this.serverInstance.isSsl());
          this.config.setEnableHeartbeat(this.serverInstance.isHeartbeat());
          // config other properties is not already exported for custom value.
        }

        if (client == null) {
          this.client =
              new NettyRemotingSocketClient(
                  // network config
                  this.config,
                  // network event listener
                  new ChannelEventListener() {

                    @Override
                    public void onChannelConnect(String remoteAddr, Channel channel) {
                      log.info("[{}][==]Deliverer Connector Remoting :{} is connected . Channel:{} ",serverInstance.getServerAddr(),remoteAddr,channel);
                    }

                    @Override
                    public void onChannelClose(String remoteAddr, Channel channel) {
                      log.info("[{}][==]Deliverer Connector Remoting :{} is closed . Channel:{} ",serverInstance.getServerAddr(),remoteAddr,channel);
                    }

                    @Override
                    public void onChannelException(String remoteAddr, Channel channel) {
                      log.info("[{}][==]Deliverer Connector Remoting :{} is exception-ed . Channel:{} ",serverInstance.getServerAddr(),remoteAddr,channel);
                    }

                    @Override
                    public void onChannelIdle(String remoteAddr, Channel channel) {
                      log.info("[{}][==]Deliverer Connector Remoting :{} is idle . Channel:{} ",serverInstance.getServerAddr(),remoteAddr,channel);
                    }
                  });
        }

        log.info("[{}][==]Deliverer Connector Connection register processor.",serverInstance.getServerAddr());

        this.client.registerProcessor(TIMED_DELIVERY_MESSAGE_COMMAND_VALUE, new TimedDeliveryMessageProcessor(), null);

        this.client.start();

        if(connectExecutor == null) {
          this.connectExecutor = new ScheduledThreadPoolExecutor(1, new DefaultThreadFactory(serverInstance.getServerAddr().concat("-connect-thread")));
        }

        // ===== startup async connect ======
        doConnect();
        // ===== end =====

        // ===== startup schedule time thread =====
        connectExecutor.scheduleWithFixedDelay(this::doConnect,1000,this.serverInstance.getConnectDelay(), TimeUnit.MILLISECONDS);

        // if framework auto heartbeat is forbidden, then startup Manual keepalive.
        if (!this.config.isEnableHeartbeat()) {
          if(heartbeatExecutor == null) {
            this.heartbeatExecutor = new ScheduledThreadPoolExecutor(1, new DefaultThreadFactory(serverInstance.getServerAddr().concat("-heartbeat-thread")));
          }
          log.info("[{}][==]Deliverer Connector ready to startup heartbeat service .",serverInstance.getServerAddr());
          this.heartbeatExecutor.scheduleWithFixedDelay(this::doHeartbeat,1000,this.serverInstance.getHeartbeatPeriod(),TimeUnit.MILLISECONDS);
        }

      } else {
        log.warn("[{}][==]Deliverer Connector Connection is already connected .",serverInstance.getServerAddr());
      }
    } catch (Exception e) {
      throw new RemotingDelivererException(e);
    }
  }

  private void doConnect() {
    try{

      if(connecting) {
        return;
      }

      log.info("[{}][==]Deliverer Connector ready to send handshake request .",serverInstance.getServerAddr());

      RemotingCommand handshakeRequest = RemotingCommand.createRequestCommand(HANDSHAKE_COMMAND_VALUE,null);

      this.client.invokeOneway(
          serverInstance.getServerAddr(),
          handshakeRequest,
          this.serverInstance.getRequestTimeout(),
          // send request to network result.
          isSendOk -> {
            if (isSendOk) {
              doRegister();
            } else {
              // sleep sts, wait next time delay
              ThreadKit.sleep(this.serverInstance.getConnectDelay());
            }
          });

    } catch (Exception e) {
      e.printStackTrace();
      log.warn("[{}][==]Deliverer Connector connect stage throw exception ,ignore",serverInstance.getServerAddr());
    }

  }

  private void doRegister() {

    try{
      log.info("[{}][==]Deliverer Connector ready to send register request .",serverInstance.getServerAddr());

      RegistryHeader header = new RegistryHeader();

      RemotingCommand registerRequest = RemotingCommand.createRequestCommand(REGISTER_COMMAND_VALUE,header);

      RegistryRequestBean requestBean = new RegistryRequestBean();
      requestBean.setServiceId(serverInstance.getApplication());

      registerRequest.setBody(JSON.toJSONBytes(requestBean));

      RemotingCommand response = this.client.invokeSync(serverInstance.getServerAddr(),registerRequest,serverInstance.getRequestTimeout());

      if(response != null) {

        BizResult result = JSON.parseObject(response.getBody(),BizResult.class);

        if(result.getCode() == 0) {
          // register success
          this.connecting = true;
          log.info("[{}][==]Deliverer Connector Client register succeed .",serverInstance.getServerAddr());
          // reset cached values
          requestFailedTimes.set(0);
        }

      }

    } catch (Exception e) {
      e.printStackTrace();
      log.warn("[{}][==]Deliverer Connector register stage throw exception ,ignore",serverInstance.getServerAddr());
    }

  }

  private void doHeartbeat() {
    try{

      if(!connecting) {
        log.warn("[{}][==]Deliverer Connector Network disconnection ,skipping heartbeat. ",serverInstance.getServerAddr());
        return;
      }

      // send heartbeat request
      RemotingCommand heartbeat = RemotingCommand.createRequestCommand(RemotingSysRequestCode.HEARTBEAT, null);

      this.client.invokeOneway(
          serverInstance.getServerAddr(),
          heartbeat,
          serverInstance.getRequestTimeout(),
          // send request
          isSendOk -> {

            if (isSendOk) {

              if(log.isDebugEnabled()) {
                log.debug("[{}][==]Deliverer Connector heartbeat send success. ",serverInstance.getServerAddr());
              }

            } else {
              // sleep sts, wait next time delay
              if(requestFailedTimes.getAndIncrement() >= serverInstance.getMaxHeartbeatFailedTimes()) {
                this.connecting = false;
              }
            }
          });

    } catch (Exception e) {
      e.printStackTrace();
      log.warn("[{}][==]Deliverer Connector heartbeat executor throw exception ,ignore",serverInstance.getServerAddr());
    }
  }

  /**
   * dis-connect remoting server instance's connection
   *
   * @throws RemotingDelivererException maybe thrown {@link RemotingDelivererException}
   */
  public void disconnect() throws RemotingDelivererException {

    try{

      log.info("[{}][==]Deliverer Connector ready to shutdown connect executor ." ,serverInstance.getServerAddr());
      ThreadKit.gracefulShutdown(connectExecutor,5,10,TimeUnit.SECONDS);

      log.info("[{}][==]Deliverer Connector ready to shutdown heartbeat executor ." ,serverInstance.getServerAddr());
      ThreadKit.gracefulShutdown(heartbeatExecutor,5,10,TimeUnit.SECONDS);

      if(connecting) {
        if(client != null) {
          client.shutdown();
        }
      }

    } catch (Exception e) {
      throw new RemotingDelivererException(e);
    } finally{
      release();
    }
  }

  /**
   * Release remoting server instance's connection
   *
   * @throws RemotingDelivererException maybe thrown {@link RemotingDelivererException}
   */
  public void release() throws RemotingDelivererException {
    log.info("[{}][==]Deliverer Connector ready to release instance ." ,serverInstance.getServerAddr());
    doRelease();
  }

  private void doRelease() {
    initialized.set(false);
    connecting = false;
    requestFailedTimes.set(0);
    config = null;
    client = null;
    connectExecutor = null;
    heartbeatExecutor = null;
    serverInstance = null;
  }
}
