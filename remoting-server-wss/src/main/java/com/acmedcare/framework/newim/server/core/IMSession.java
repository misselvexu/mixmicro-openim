package com.acmedcare.framework.newim.server.core;

import com.acmedcare.framework.kits.jackson.JacksonKit;
import com.acmedcare.framework.kits.thread.DefaultThreadFactory;
import com.acmedcare.framework.kits.thread.ThreadKit;
import com.acmedcare.framework.newim.Message;
import com.acmedcare.framework.newim.Message.MessageType;
import com.acmedcare.framework.newim.SessionBean;
import com.acmedcare.framework.newim.client.MessageAttribute;
import com.acmedcare.framework.newim.client.bean.Member;
import com.acmedcare.framework.newim.protocol.Command.ClusterClientCommand;
import com.acmedcare.framework.newim.protocol.Command.Retriable;
import com.acmedcare.framework.newim.protocol.request.ClusterForwardMessageHeader;
import com.acmedcare.framework.newim.server.core.SessionContextConstants.RemotePrincipal;
import com.acmedcare.framework.newim.server.core.connector.ClusterReplicaConnector;
import com.acmedcare.framework.newim.server.endpoint.WssSessionContext;
import com.acmedcare.framework.newim.server.event.AbstractEventHandler;
import com.acmedcare.framework.newim.server.processor.header.ServerPushMessageHeader;
import com.acmedcare.tiffany.framework.remoting.common.RemotingHelper;
import com.acmedcare.tiffany.framework.remoting.common.RemotingUtil;
import com.acmedcare.tiffany.framework.remoting.netty.NettyRemotingSocketServer;
import com.acmedcare.tiffany.framework.remoting.protocol.RemotingCommand;
import com.alibaba.fastjson.JSON;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import com.google.common.eventbus.AsyncEventBus;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFutureListener;
import lombok.Getter;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.beans.factory.InitializingBean;

import javax.annotation.concurrent.ThreadSafe;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.*;
import java.util.concurrent.ThreadPoolExecutor.CallerRunsPolicy;

import static com.acmedcare.framework.newim.server.ClusterLogger.imServerLog;
import static com.acmedcare.framework.newim.server.ClusterLogger.masterClusterLog;

/**
 * IM Client Session Context
 *
 * @author <a href="mailto:iskp.me@gmail.com">Elve.Xu</a>
 * @version ${project.version} - 12/11/2018.
 */
@ThreadSafe
public class IMSession implements InitializingBean, DisposableBean {

  private static final long DIFF_PERIOD = 2 * 60 * 1000;

  /**
   * 设备->远程连接(TCPs)
   *
   * <p>
   */
  private static Map<SessionBean, Map<String, Channel>> devicesTcpChannelContainer =
      Maps.newConcurrentMap();

  /**
   * 通行证->远程连接(TCPs)
   *
   * <p>
   */
  private static Map<SessionBean, Map<String, Channel>> passportsTcpChannelContainer =
      Maps.newConcurrentMap();

  // ----------------------------- Master 服务器同步过来的缓存数据------------------------

  private static volatile long lastDiffTimestamp = System.currentTimeMillis();
  private static Semaphore diffQuerySemaphore = new Semaphore(1);
  private static Set<SessionBean> masterPassportSessions = Sets.newConcurrentHashSet();
  private static Set<SessionBean> masterDeviceSessions = Sets.newConcurrentHashSet();

  // ---------------------------------------------------------------------------------

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
  private static WssSessionContext wssSessionContext;

  public Set<SessionBean> getOnlinePassports() {
    return passportsTcpChannelContainer.keySet();
  }

  public Set<SessionBean> getOnlineDevices() {
    return devicesTcpChannelContainer.keySet();
  }

  public void registerNewIMServer(NettyRemotingSocketServer imServer) {
    this.imServer = imServer;
  }

  /**
   * Destroy Method
   *
   * <p>
   */
  @Override
  public void destroy() {
    ThreadKit.gracefulShutdown(asyncExecutor, 10, 20, TimeUnit.SECONDS);
    ThreadKit.gracefulShutdown(cleaner, 10, 20, TimeUnit.SECONDS);
  }

  // ================================ Session Bind Methods ========================================
  /**
   * 绑定链接
   *
   * @param remotePrincipal 通行证相关信息
   * @param deviceId 设备编号
   * @param passportId 通行证编号
   * @param channel 链接对象
   */
  public void bindTcpSession(
      RemotePrincipal remotePrincipal,
      String deviceId,
      String deviceType,
      String passportId,
      Channel channel) {

    SessionBean deviceSession =
        SessionBean.builder().sessionId(deviceId).namespace(remotePrincipal.getNamespace()).build();
    imServerLog.debug("[NEW-IM-SESSION] Bind Session , {} {} {}", deviceId, passportId, channel);

    imServerLog.info(" == 设备 SESSION: {} ", JacksonKit.objectToJson(deviceSession));
    if (devicesTcpChannelContainer.containsKey(deviceSession)) {
      // yes
      Channel originChannel =
          devicesTcpChannelContainer.get(deviceSession).put(deviceType, channel);
//      if (originChannel != null) {
//        pushOfflineMessage(originChannel);
//      }
    } else {
      // nop
      Map<String, Channel> channelMap = Maps.newHashMap();
      channelMap.put(deviceType, channel);
      devicesTcpChannelContainer.put(deviceSession, channelMap);
    }

    SessionBean passportSession =
        SessionBean.builder()
            .sessionId(passportId)
            .namespace(remotePrincipal.getNamespace())
            .build();

    imServerLog.info(" == 通行证 SESSION: {} ", JacksonKit.objectToJson(passportSession));
    if (passportsTcpChannelContainer.containsKey(passportSession)) {
      // yes
      Channel originChannel =
          passportsTcpChannelContainer.get(passportSession).put(deviceType, channel);
      if (originChannel != null) {
        imServerLog.info(
            " == 通行证异地登录 {},剔除下线:{} ",
            passportId,
            RemotingHelper.parseChannelRemoteAddr(originChannel));

        pushOfflineMessage(originChannel);
      }
    } else {
      // nop
      Map<String, Channel> channelMap = Maps.newHashMap();
      channelMap.put(deviceType, channel);
      passportsTcpChannelContainer.put(passportSession, channelMap);
    }
  }

  /**
   * 分发下线指令
   *
   * @param channel 通道
   */
  private void pushOfflineMessage(Channel channel) {
    if (channel != null && channel.isActive() & channel.isWritable()) {
      RemotingCommand command =
          RemotingCommand.createRequestCommand(ClusterClientCommand.SERVER_PUSH_FOCUS_LOGOUT, null);
      command.markOnewayRPC();
      channel
          .writeAndFlush(command)
          .addListener(
              future -> {
                if (future.isSuccess()) {
                  if (imServerLog.isDebugEnabled()) {
                    imServerLog.debug(
                        "[IM-BROADCAST-SEND] 发送下线指令完成; Channel: {}",
                        RemotingHelper.parseChannelRemoteAddr(channel));
                  }
                  asyncExecutor.submit(
                      () -> {
                        // 延迟2S关闭
                        ThreadKit.sleep(2000, TimeUnit.MILLISECONDS);
                        RemotingUtil.closeChannel(channel);
                      });
                }
              });
    }
  }

  // ================================ Session Send Methods ===================================

  /**
   * 发送消息给通行证
   *
   * @param passportId 通行证编号
   * @param message 消息
   */
  public void sendMessageToPassport(
      String namespace, String passportId, MessageType messageType, byte[] message) {

    try {
      asyncExecutor.execute(
          () -> {
            Message messageInstance = JSON.parseObject(message, Message.class);
            IMSession.wssSessionContext.sendMessageToPassports(
                Lists.newArrayList(passportId), message);
          });

      imServerLog.info("[TCP-WSS] 提交转发消息任务成功");
    } catch (Exception e) {
      imServerLog.error("[TCP-WSS] 转发消息到 WebSocket 异常", e);
    }

    SessionBean sessionBean =
        SessionBean.builder().namespace(namespace).sessionId(passportId).build();
    if (passportsTcpChannelContainer.containsKey(sessionBean)) {
      Collection<Channel> channels = passportsTcpChannelContainer.get(sessionBean).values();
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
            if (channel != null && channel.isWritable()) {
              // send
              channel
                  .writeAndFlush(command)
                  .addListener(
                      (ChannelFutureListener)
                          future -> {
                            if (future.isSuccess()) {
                              // TODO 根据客户端的消息类型进行结果回执
                              if (imServerLog.isDebugEnabled()) {
                                imServerLog.debug(
                                    "[IM-SESSION-SEND] 消息请求发送成功; Channel:{}",
                                    RemotingHelper.parseChannelRemoteAddr(channel));
                              }
                            }
                          });
            } else {
              imServerLog.warn("[IM-SESSION-SEND] 客户端:{} 链接异常", channel);
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
      String namespace, List<String> passportIds, MessageType messageType, byte[] message) {
    if (passportIds != null && passportIds.size() > 0) {

      CountDownLatch countDownLatch = new CountDownLatch(passportIds.size());
      imServerLog.info("[NEW-IM-SEND] 批量提交异步发送任务");
      for (String passportId : passportIds) {
        asyncExecutor.execute(
            () -> {
              try {
                sendMessageToPassport(namespace, passportId, messageType, message);
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
    header.setNamespace(attribute.getNamespace());

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
    IMSession.wssSessionContext = wssSessionContext;
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
                (sessionBean, stringChannelMap) -> {
                  for (Map.Entry<String, Channel> entry : stringChannelMap.entrySet()) {
                    String key = entry.getKey();
                    Channel channel = entry.getValue();
                    if (channel == null || !channel.isActive() || !channel.isWritable()) {
                      stringChannelMap.remove(key);
                    }
                  }
                });

            devicesTcpChannelContainer
                .entrySet()
                .removeIf(entry -> entry.getValue() == null || entry.getValue().isEmpty());

          } catch (Exception ignore) {
          }

          try {

            passportsTcpChannelContainer.forEach(
                (sessionBean, stringChannelMap) -> {
                  for (Map.Entry<String, Channel> entry : stringChannelMap.entrySet()) {
                    String key = entry.getKey();
                    Channel channel = entry.getValue();
                    if (channel == null || !channel.isActive() || !channel.isWritable()) {
                      stringChannelMap.remove(key);
                    }
                  }
                });

            passportsTcpChannelContainer
                .entrySet()
                .removeIf(entry -> entry.getValue() == null || entry.getValue().isEmpty());

          } catch (Exception ignore) {
          }
        },
        30,
        30,
        TimeUnit.SECONDS);
  }

  public List<Member> getOnlineMemberList(String namespace, List<Member> members, String groupId) {

    try {
      diffQuerySemaphore.acquire(1);
      members.removeIf(
          member ->
              !masterPassportSessions.contains(
                  SessionBean.builder()
                      .sessionId(member.getMemberId().toString())
                      .namespace(namespace)
                      .build()));
      return members;
    } catch (Exception e) {
      imServerLog.warn("get online member list exception ", e);
      return Lists.newArrayList();
    } finally {
      diffQuerySemaphore.release(1);
    }
  }

  public void diff(Set<SessionBean> passportsConnections, Set<SessionBean> devicesConnections) {

    // 2分钟 DIFF 一次
    if (System.currentTimeMillis() - lastDiffTimestamp > DIFF_PERIOD) {
      asyncExecutor.execute(
          () -> {
            try {

              diffQuerySemaphore.acquire(1);
              masterPassportSessions.clear();
              masterPassportSessions.addAll(passportsConnections);

              masterDeviceSessions.clear();
              masterDeviceSessions.addAll(devicesConnections);

              lastDiffTimestamp = System.currentTimeMillis();
            } catch (Exception e) {
              masterClusterLog.error("cluster process master session data failed,ignore");
            } finally {
              diffQuerySemaphore.release(1);
            }
          });
    }
  }
}
