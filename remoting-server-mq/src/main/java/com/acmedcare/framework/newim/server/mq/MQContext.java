package com.acmedcare.framework.newim.server.mq;

import com.acmedcare.framework.aorp.beans.Principal;
import com.google.common.collect.Maps;
import io.netty.util.AttributeKey;
import java.nio.channels.Channel;
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
  protected static final AttributeKey<ClientSession> CLIENT_SESSION_ATTRIBUTE_KEY =
      AttributeKey.newInstance("CLIENT_SESSION_ATTRIBUTE_KEY");

  /** Register Sampling Clients Sessions */
  private static final Map<Long, List<Channel>> SAMPLING_SESSIONS = Maps.newConcurrentMap();

  /** Register Monitor Clients Sessions */
  private static final Map<Long, List<Channel>> MONITOR_SESSIONS = Maps.newConcurrentMap();

  public void registerSamplingClient() {}

  public void registerMonitorClient() {}

  @Getter
  @Setter
  @NoArgsConstructor
  public static class ClientSession extends Principal {
    private String devicesId;
    private String areaNo;
    private String orgId;
  }
}
