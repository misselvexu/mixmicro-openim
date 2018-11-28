package com.acmedcare.framework.newim.server.core;

import static com.acmedcare.framework.newim.server.ClusterLogger.imServerLog;

import com.acmedcare.framework.kits.thread.DefaultThreadFactory;
import com.acmedcare.framework.kits.thread.ThreadKit;
import com.acmedcare.framework.newim.Message;
import com.acmedcare.framework.newim.Message.MessageType;
import com.acmedcare.framework.newim.client.MessageAttribute;
import com.acmedcare.framework.newim.protocol.Command.ClusterClientCommand;
import com.acmedcare.framework.newim.protocol.Command.Retriable;
import com.acmedcare.framework.newim.protocol.request.ClusterForwardMessageHeader;
import com.acmedcare.framework.newim.server.core.connector.ClusterReplicaConnector;
import com.acmedcare.framework.newim.server.endpoint.WssSessionContext;
import com.acmedcare.framework.newim.server.event.AbstractEventHandler;
import com.acmedcare.framework.newim.server.exception.SessionBindException;
import com.acmedcare.framework.newim.server.processor.header.ServerPushMessageHeader;
import com.acmedcare.tiffany.framework.remoting.netty.NettyRemotingSocketServer;
import com.acmedcare.tiffany.framework.remoting.protocol.RemotingCommand;
import com.alibaba.fastjson.JSON;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.eventbus.AsyncEventBus;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFutureListener;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.ThreadPoolExecutor.CallerRunsPolicy;
import java.util.concurrent.TimeUnit;
import javax.annotation.concurrent.ThreadSafe;
import lombok.Getter;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.beans.factory.InitializingBean;

/**
 * IM Client Session Context
 *
 * @author <a href="mailto:iskp.me@gmail.com">Elve.Xu</a>
 * @version ${project.version} - 12/11/2018.
 */
@ThreadSafe
public class IMSession implements InitializingBean, DisposableBean {

  /**
   * 设备->远程连接(TCPs)
   *
   * <p>
   */
  private static Map<String, List<Channel>> devicesTcpChannelContainer = Maps.newConcurrentMap();
  /**
   * 通行证->远程连接(TCPs)
   *
   * <p>
   */
  private static Map<String, List<Channel>> passportsTcpChannelContainer = Maps.newConcurrentMap();
  /**
   * 记录通行证登录在某台通讯服务器映射关系
   *
   * <p>passportId -> Cluster Server Addr
   *
   * <p>
   */
  private static Map<String, List<String>> passportsClusterConnectorMapping =
      Maps.newConcurrentMap();
  /** 发送消息线程池 */
  private static ExecutorService asyncExecutor =
      new ThreadPoolExecutor(
          8,
          32,
          5000,
          TimeUnit.MILLISECONDS,
          new LinkedBlockingQueue<>(64),
          new DefaultThreadFactory("new-im-send-async-executor-pool-"),
          new CallerRunsPolicy());

  private static ScheduledExecutorService cleaner =
      new ScheduledThreadPoolExecutor(1, new DefaultThreadFactory("channel-cleaner-thread"));

  @Getter private AsyncEventBus asyncEventBus;
  private NettyRemotingSocketServer imServer;
  @Getter private ClusterReplicaConnector clusterReplicaConnector;
  private WssSessionContext wssSessionContext;

  public void registerNewIMServer(NettyRemotingSocketServer imServer) {
    this.imServer = imServer;
  }

  /**
   * Destroy Method
   *
   * <p>
   */
  public void destroy() {
    ThreadKit.gracefulShutdown(asyncExecutor, 10, 20, TimeUnit.SECONDS);
    ThreadKit.gracefulShutdown(cleaner, 10, 20, TimeUnit.SECONDS);
  }

  // ================================ Session Bind Methods ========================================
  /**
   * 绑定链接
   *
   * @param deviceId 设备编号
   * @param passportId 通行证编号
   * @param channel 链接对象
   */
  public void bindTcpSession(String deviceId, String passportId, Channel channel) {

    imServerLog.debug("[NEW-IM-SESSION] Bind Session , {} {} {}", deviceId, passportId, channel);
    if (devicesTcpChannelContainer.containsKey(deviceId)) {
      // yes
      boolean result = devicesTcpChannelContainer.get(deviceId).add(channel);
      if (!result) {
        throw new SessionBindException("Channel:" + channel + " ,绑定失败");
      }
    } else {
      // nop
      devicesTcpChannelContainer.put(deviceId, Lists.newArrayList(channel));
    }

    if (passportsTcpChannelContainer.containsKey(passportId)) {
      // yes
      boolean result = passportsTcpChannelContainer.get(passportId).add(channel);
      if (!result) {
        throw new SessionBindException("Channel:" + channel + " ,绑定失败");
      }
    } else {
      // nop
      passportsTcpChannelContainer.put(passportId, Lists.newArrayList(channel));
    }
  }

  // ================================ Session Send Methods ===================================

  /**
   * 发送消息给通行证
   *
   * @param passportId 通行证编号
   * @param message 消息
   */
  public void sendMessageToPassport(String passportId, MessageType messageType, byte[] message) {

    try {
      asyncExecutor.execute(
          () ->
              IMSession.this.wssSessionContext.sendMessageToPassports(
                  Lists.newArrayList(passportId), message));

      imServerLog.info("[TCP-WSS] 提交转发消息任务成功");
    } catch (Exception e) {
      imServerLog.error("[TCP-WSS] 转发消息到 WebSocket 异常", e);
    }

    if (passportsTcpChannelContainer.containsKey(passportId)) {
      List<Channel> channels = passportsTcpChannelContainer.get(passportId);
      ServerPushMessageHeader serverPushMessageHeader = new ServerPushMessageHeader();
      serverPushMessageHeader.setMessageType(messageType.name());
      if (channels.size() > 0) {
        RemotingCommand command =
            RemotingCommand.createRequestCommand(
                ClusterClientCommand.SERVER_PUSH_MESSAGE, serverPushMessageHeader);
        command.setBody(message);

        // foreach send
        for (Channel channel : channels) {
          try {
            if (channel.isWritable()) {
              // send
              channel
                  .writeAndFlush(command)
                  .addListener(
                      (ChannelFutureListener)
                          future -> {
                            if (future.isSuccess()) {
                              // TODO 根据客户端的消息类型进行结果回执

                            }
                          });
            } else {
              imServerLog.warn("[IM-SESSION-SEND] 客户端:{}链接异常", channel);
            }
          } catch (Exception e) {
            imServerLog.warn("[IM-SESSION-SEND] 发送消息给客户端:" + channel + "失败", e);
          }
        }
      }
    }
  }

  /**
   * 批量发送消息
   *
   * @param passportIds 多个通行证
   * @param message 消息
   */
  public void sendMessageToPassport(
      List<String> passportIds, MessageType messageType, byte[] message) {
    if (passportIds != null && passportIds.size() > 0) {
      CountDownLatch countDownLatch = new CountDownLatch(passportIds.size());

      imServerLog.info("[NEW-IM-SEND] 批量提交异步发送任务");
      for (String passportId : passportIds) {
        asyncExecutor.execute(
            () -> {
              try {
                sendMessageToPassport(passportId, messageType, message);
              } catch (Exception e) {
                imServerLog.error("[NEW-IM-SEND-TASK] 异步发送消息任务执行异常");
              } finally {
                countDownLatch.countDown();
              }
            });
      }
      // wait
      try {
        countDownLatch.await();
        imServerLog.info("[NEW-IM-SEND] 多线程批量发送消息发送完成");
      } catch (InterruptedException e) {
        imServerLog.error("[NEW-IM-SEND] 多线程批量发送消息等待异常", e);
      }
    }
  }

  /**
   * 发送消息给设备
   *
   * @param deviceId 设备编号
   * @param message 消息
   */
  public void sendMessageToDevice(String deviceId, MessageType messageType, byte[] message) {
    // TODO 针对设备进行推送消息
  }

  public void registerClusterReplicasConnector(ClusterReplicaConnector clusterReplicaConnector) {
    this.clusterReplicaConnector = clusterReplicaConnector;
  }

  /**
   * 分发消息到其他通讯服务器
   *
   * @param attribute 消息属性
   * @param message 消息内容
   */
  public void distributeMessage(MessageAttribute attribute, Message message) {

    ClusterForwardMessageHeader header = new ClusterForwardMessageHeader();
    header.setMessageType(message.getMessageType().name());
    header.setInnerType(message.getInnerType().name());
    header.setMaxRetryTimes(attribute.getMaxRetryTimes());
    header.setPersistent(attribute.isPersistent());
    header.setQos(attribute.isQos());
    header.setRetryPeriod(attribute.getRetryPeriod());

    imServerLog.info("服务器分发消息,请求头信息:{}", JSON.toJSONString(header));
    this.clusterReplicaConnector.forwardMessage(
        header,
        message.bytes(),
        Retriable.builder()
            .maxCounts(attribute.getMaxRetryTimes())
            .fastFail(true)
            .period(attribute.getRetryPeriod())
            .retry(true)
            .timeUnit(TimeUnit.MILLISECONDS)
            .build());
  }

  public void registerWssSessionContext(WssSessionContext wssSessionContext) {
    this.wssSessionContext = wssSessionContext;
  }

  public <T> void registerEventHandler(AbstractEventHandler<T> handler) {
    this.asyncEventBus.register(handler);
  }

  public <T> void unRegisterEventHandler() {
    this.asyncEventBus.unregister(this);
  }

  @Override
  public void afterPropertiesSet() throws Exception {
    this.asyncEventBus = new AsyncEventBus(Executors.newFixedThreadPool(8));
    imServerLog.info("异步事件初始化完成:{}", this.asyncEventBus);

    // startup cleaner
    cleaner.scheduleWithFixedDelay(
        () -> {
          if (imServerLog.isDebugEnabled()) {
            imServerLog.debug(
                "Time to clean local channel cache, remove unavailable channel instance");
          }
          try {
            devicesTcpChannelContainer.forEach(
                (s, channels) ->
                    channels.removeIf(
                        channel ->
                            channel == null || !channel.isActive() || !channel.isWritable()));

          } catch (Exception ignore) {
          }

          try {

            passportsTcpChannelContainer.forEach(
                (s, channels) ->
                    channels.removeIf(
                        channel ->
                            channel == null || !channel.isActive() || !channel.isWritable()));

          } catch (Exception ignore) {
          }
        },
        30,
        30,
        TimeUnit.SECONDS);
  }
}
