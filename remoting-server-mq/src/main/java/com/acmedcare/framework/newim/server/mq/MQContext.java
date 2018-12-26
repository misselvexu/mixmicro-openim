package com.acmedcare.framework.newim.server.mq;

import com.acmedcare.framework.aorp.beans.Principal;
import com.acmedcare.framework.kits.executor.AsyncRuntimeExecutor;
import com.acmedcare.framework.newim.Message.MQMessage;
import com.acmedcare.framework.newim.Topic.TopicSubscribe;
import com.acmedcare.framework.newim.server.Context;
import com.acmedcare.framework.newim.server.mq.MQCommand.Common;
import com.acmedcare.tiffany.framework.remoting.protocol.RemotingCommand;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import io.netty.channel.Channel;
import io.netty.util.AttributeKey;
import java.util.List;
import java.util.Map;
import java.util.Set;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * MQContext
 *
 * @author <a href="mailto:iskp.me@gmail.com">Elve.Xu</a>
 * @version ${project.version} - 2018-12-14.
 */
public final class MQContext implements Context {

  /** Client Channel Session Key {@link ClientSession} */
  public static final AttributeKey<ClientSession> CLIENT_SESSION_ATTRIBUTE_KEY =
      AttributeKey.newInstance("CLIENT_SESSION_ATTRIBUTE_KEY");

  private static final Logger logger = LoggerFactory.getLogger(MQContext.class);
  /** Register Sampling Clients Sessions */
  private static final Map<Long, List<Channel>> SAMPLING_SESSIONS = Maps.newConcurrentMap();

  /** Register Monitor Clients Sessions */
  private static final Map<Long, List<Channel>> MONITOR_SESSIONS = Maps.newConcurrentMap();

  @Getter private Set<String> replicas = Sets.newConcurrentHashSet();

  private MQServerProperties mqServerProperties;

  MQContext(MQServerProperties mqServerProperties) {
    this.mqServerProperties = mqServerProperties;
  }

  void refreshReplicas(Set<String> replicas) {
    this.replicas = replicas;
  }

  String selfAddress() {
    return mqServerProperties.getHost();
  }

  public void registerSamplingClient(
      io.netty.channel.Channel channel, ClientSession clientSession) {
    if (SAMPLING_SESSIONS.containsKey(clientSession.getPassportUid())) {
      SAMPLING_SESSIONS.get(clientSession.getPassportUid()).add(channel);
    } else {
      SAMPLING_SESSIONS.put(clientSession.getPassportUid(), Lists.newArrayList(channel));
    }
  }

  public void registerMonitorClient(io.netty.channel.Channel channel, ClientSession clientSession) {
    if (MONITOR_SESSIONS.containsKey(clientSession.getPassportUid())) {
      MONITOR_SESSIONS.get(clientSession.getPassportUid()).add(channel);
    } else {
      MONITOR_SESSIONS.put(clientSession.getPassportUid(), Lists.newArrayList(channel));
    }
  }

  public void unRegisterMonitorClient(
      io.netty.channel.Channel channel, ClientSession clientSession) {
    if (clientSession != null) {
      if (MONITOR_SESSIONS.containsKey(clientSession.getPassportUid())) {
        MONITOR_SESSIONS.get(clientSession.getPassportUid()).remove(channel);
      }
    }
  }

  public void unRegisterSamplingClient(
      io.netty.channel.Channel channel, ClientSession clientSession) {
    if (clientSession != null) {
      if (SAMPLING_SESSIONS.containsKey(clientSession.getPassportUid())) {
        SAMPLING_SESSIONS.get(clientSession.getPassportUid()).remove(channel);
      }
    }
  }

  public void broadcastTopicMessages(List<TopicSubscribe> subscribes, MQMessage mqMessage) {

    AsyncRuntimeExecutor.getAsyncThreadPool()
        .execute(
            () -> {
              for (TopicSubscribe subscribe : subscribes) {
                Long passportId = subscribe.getPassportId();
                if (MONITOR_SESSIONS.containsKey(passportId)) {
                  List<Channel> channels = MONITOR_SESSIONS.get(passportId);
                  if (channels != null && !channels.isEmpty()) {
                    for (Channel channel : channels) {
                      if (channel != null && channel.isWritable()) {
                        RemotingCommand pushRequest =
                            RemotingCommand.createRequestCommand(Common.TOPIC_MESSAGE_PUSH, null);
                        // set body
                        pushRequest.setBody(mqMessage.bytes());

                        // write
                        channel
                            .writeAndFlush(pushRequest)
                            .addListener(
                                future -> {
                                  if (!future.isDone()) {
                                    logger.warn(
                                        "broadcast mq message failed, {},{}",
                                        mqMessage.getTopicId(),
                                        passportId);
                                  }
                                });
                      }
                    }
                  }
                }
              }
            });
  }

  public void broadcastMessage(MQMessage mqMessage) {
    // todo 转发到 replica 服务器
  }

  /**
   * Return Current Context
   *
   * @return context
   */
  @Override
  public Context context() {
    return this;
  }

  @Getter
  @Setter
  @NoArgsConstructor
  public static class ClientSession extends Principal {
    private String namespace;
    private String deviceId;
    private String areaNo;
    private String orgId;
  }
}
