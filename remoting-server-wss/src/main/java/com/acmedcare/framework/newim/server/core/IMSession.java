package com.acmedcare.framework.newim.server.core;

import static com.acmedcare.framework.newim.server.ClusterLogger.imServerLog;

import com.acmedcare.framework.kits.thread.DefaultThreadFactory;
import com.acmedcare.framework.kits.thread.ThreadKit;
import com.acmedcare.framework.newim.Message.MessageType;
import com.acmedcare.framework.newim.protocol.Command.ClusterClientCommand;
import com.acmedcare.framework.newim.server.core.connector.ClusterReplicaConnector;
import com.acmedcare.framework.newim.server.exception.SessionBindException;
import com.acmedcare.framework.newim.server.processor.header.ServerPushMessageHeader;
import com.acmedcare.tiffany.framework.remoting.netty.NettyRemotingSocketClient;
import com.acmedcare.tiffany.framework.remoting.netty.NettyRemotingSocketServer;
import com.acmedcare.tiffany.framework.remoting.protocol.RemotingCommand;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFutureListener;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.ThreadPoolExecutor.CallerRunsPolicy;
import java.util.concurrent.TimeUnit;
import javax.annotation.concurrent.ThreadSafe;

/**
 * IM Client Session Context
 *
 * @author <a href="mailto:iskp.me@gmail.com">Elve.Xu</a>
 * @version ${project.version} - 12/11/2018.
 */
@ThreadSafe
public class IMSession {

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

  private NettyRemotingSocketServer imServer;
  private ClusterReplicaConnector clusterReplicaConnector;

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

  /**
   * 判断接受者是否在本机器登录
   *
   * @param receiverPassportId 接收人
   * @return true/false
   */
  public boolean isPassportimServerLoginLocalServer(String receiverPassportId) {
    return passportsTcpChannelContainer.containsKey(receiverPassportId);
  }

  /**
   * 查询接受者此刻登录的服务器 CLuster
   *
   * @param receiverPassportId 接受者
   * @return client
   */
  public NettyRemotingSocketClient findClientConnectedClusterConnector(String receiverPassportId) {
    // TODO
    return null;
  }

  public void registerClusterReplicasConnector(ClusterReplicaConnector clusterReplicaConnector) {
    this.clusterReplicaConnector = clusterReplicaConnector;
  }
}
