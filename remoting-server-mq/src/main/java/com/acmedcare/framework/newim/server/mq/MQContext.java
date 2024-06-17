package com.acmedcare.framework.newim.server.mq;

import com.acmedcare.framework.aorp.beans.Principal;
import com.acmedcare.framework.aorp.utils.Base64;
import com.acmedcare.framework.kits.executor.AsyncRuntimeExecutor;
import com.acmedcare.framework.newim.InstanceType;
import com.acmedcare.framework.newim.Message.MQMessage;
import com.acmedcare.framework.newim.RemotingEvent;
import com.acmedcare.framework.newim.Topic.TopicSubscribe;
import com.acmedcare.framework.newim.server.Context;
import com.acmedcare.framework.newim.server.mq.MQCommand.Common;
import com.acmedcare.framework.newim.server.mq.MQCommand.ProducerClient;
import com.acmedcare.framework.newim.server.mq.event.AcmedcareEvent;
import com.acmedcare.framework.newim.server.mq.event.AcmedcareEvent.BizEvent;
import com.acmedcare.framework.newim.server.mq.event.AcmedcareEvent.Event;
import com.acmedcare.framework.newim.server.mq.processor.header.OnTopicRemovedHeader;
import com.acmedcare.framework.newim.server.replica.NodeReplicaBeanFactory;
import com.acmedcare.tiffany.framework.remoting.protocol.RemotingCommand;
import com.alibaba.fastjson.JSON;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFutureListener;
import io.netty.util.AttributeKey;

import java.io.UnsupportedEncodingException;
import java.util.Arrays;
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
  private NodeReplicaBeanFactory nodeReplicaBeanFactory;

  MQContext(MQServerProperties mqServerProperties, NodeReplicaBeanFactory nodeReplicaBeanFactory) {
    this.mqServerProperties = mqServerProperties;
    this.nodeReplicaBeanFactory = nodeReplicaBeanFactory;
  }

  void refreshReplicas(Set<String> replicas) {
    this.replicas = replicas;
  }

  String selfAddress() {
    return mqServerProperties.getHost() + ":" + mqServerProperties.getPort();
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

    // Async broadcast messages to topic .
    AsyncRuntimeExecutor.getAsyncThreadPool()
        .execute(
            () -> {
              for (TopicSubscribe subscribe : subscribes) {
                Long passportId = subscribe.getPassportId();
                if (MONITOR_SESSIONS.containsKey(passportId)) {
                  List<Channel> channels = MONITOR_SESSIONS.get(passportId);
                  if (channels != null && !channels.isEmpty()) {
                    for (Channel channel : channels) {
                      // add exception cache process
                      try {
                        if (channel != null && channel.isWritable()) {
                          RemotingCommand pushRequest = RemotingCommand.createRequestCommand(Common.TOPIC_MESSAGE_PUSH, null);
                          // set body
                          pushRequest.setBody(mqMessage.bytes());

                          // write
                          channel.writeAndFlush(pushRequest)
                              .addListener(
                                  future -> {
                                    if (!future.isDone()) {
                                      logger.warn("broadcast mq message failed, {},{}", mqMessage.getTopicId(), passportId);
                                    }
                                  });
                        }
                      } catch (Exception e) {
                        // ignore exception
                      }
                    }
                  }
                }
              }
            });
  }

  public void broadcastMessage(MQMessage mqMessage) {
    nodeReplicaBeanFactory.postMessage(InstanceType.MQ_SERVER, mqMessage, null);
  }

  public void broadcastEvent(RemotingEvent remotingEvent) {
    nodeReplicaBeanFactory.postEvent(InstanceType.MQ_SERVER, remotingEvent);
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

  public void broadcastEvent(AcmedcareEvent event) {
    if (event != null) {
      if (event.eventType() != null) {
        Event e = event.eventType();
        if (e instanceof BizEvent) {
          switch ((BizEvent) e) {
            case ON_TOPIC_EMPTY_SUBSCRIBED_EVENT:
              AsyncRuntimeExecutor.getAsyncThreadPool()
                  .execute(
                      () ->
                          SAMPLING_SESSIONS.forEach(
                              (aLong, channels) -> {
                                if (!channels.isEmpty()) {
                                  for (Channel channel : channels) {
                                    if (channel != null && channel.isWritable()) {
                                      RemotingCommand command =
                                          RemotingCommand.createRequestCommand(
                                              ProducerClient.ON_TOPIC_UNSUBSCRIBE_EVENT, null);
                                      command.setBody(event.data());
                                      channel
                                          .writeAndFlush(command)
                                          .addListener(
                                              (ChannelFutureListener)
                                                  future -> {
                                                    if (!future.isSuccess()) {
                                                      logger.warn(
                                                          "broadcast un-subscribe-event message failed, {},{}",
                                                          aLong,
                                                          event.data().toString());
                                                    }
                                                  });
                                    }
                                  }
                                }
                              }));
              break;

            case ON_TOPIC_UB_SUBSCRIBE_EVENT:
              AsyncRuntimeExecutor.getAsyncThreadPool()
                  .execute(
                      () ->
                          SAMPLING_SESSIONS.forEach(
                              (aLong, channels) -> {
                                if (!channels.isEmpty()) {
                                  for (Channel channel : channels) {
                                    if (channel != null && channel.isWritable()) {
                                      RemotingCommand command =
                                          RemotingCommand.createRequestCommand(
                                              ProducerClient.ON_TOPIC_SUBSCRIBED_EMPTY_EVENT, null);
                                      command.setBody(event.data());
                                      channel
                                          .writeAndFlush(command)
                                          .addListener(
                                              (ChannelFutureListener)
                                                  future -> {
                                                    if (!future.isSuccess()) {
                                                      logger.warn(
                                                          "broadcast topic-empty-subscribe-event message failed, {},{}",
                                                          aLong,
                                                          event.data().toString());
                                                    }
                                                  });
                                    }
                                  }
                                }
                              }));

              break;

            case ON_TOPIC_REMOVED_EVENT:
              logger.info("[EVENT] 请求广播事件:{}", e);
              AsyncRuntimeExecutor.getAsyncThreadPool()
                  .execute(
                      () ->
                          MONITOR_SESSIONS.forEach(
                              (aLong, channels) -> {
                                if (!channels.isEmpty()) {

                                  OnTopicRemovedHeader onTopicRemovedHeader =
                                      new OnTopicRemovedHeader();
                                  try {
                                    onTopicRemovedHeader.setTopicId(
                                        Long.parseLong(new String(event.data(), "UTF-8")));

                                    for (Channel channel : channels) {
                                      if (channel != null && channel.isWritable()) {

                                        RemotingCommand command =
                                            RemotingCommand.createRequestCommand(
                                                Common.ON_TOPIC_REMOVED_EVENT,
                                                onTopicRemovedHeader);
                                        command.setBody(event.data());

                                        channel
                                            .writeAndFlush(command)
                                            .addListener(
                                                (ChannelFutureListener)
                                                    future -> {
                                                      if (!future.isSuccess()) {
                                                        logger.warn(
                                                            "broadcast topic-removed-event message failed, {},{}",
                                                            aLong,
                                                            Arrays.toString(event.data()));
                                                      }
                                                    });
                                      }
                                    }

                                  } catch (UnsupportedEncodingException e1) {
                                    logger.error(
                                        "broadcast topic-removed-event message ,param process error",
                                        e1);
                                  }
                                }
                              }));

              break;
            default:
              logger.warn("ignore invalid event:{}", e);
              break;
          }
        }
      }
    }
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
