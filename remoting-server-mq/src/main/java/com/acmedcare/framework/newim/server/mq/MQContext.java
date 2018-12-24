package com.acmedcare.framework.newim.server.mq;

import com.acmedcare.framework.aorp.beans.Principal;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import io.netty.channel.Channel;
import io.netty.util.AttributeKey;
import java.util.List;
import java.util.Map;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * MQContext
 *
 * @author <a href="mailto:iskp.me@gmail.com">Elve.Xu</a>
 * @version ${project.version} - 2018-12-14.
 */
public final class MQContext {

  /** Client Channel Session Key {@link ClientSession} */
  public static final AttributeKey<ClientSession> CLIENT_SESSION_ATTRIBUTE_KEY =
      AttributeKey.newInstance("CLIENT_SESSION_ATTRIBUTE_KEY");

  /** Register Sampling Clients Sessions */
  private static final Map<Long, List<Channel>> SAMPLING_SESSIONS = Maps.newConcurrentMap();

  /** Register Monitor Clients Sessions */
  private static final Map<Long, List<Channel>> MONITOR_SESSIONS = Maps.newConcurrentMap();

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
