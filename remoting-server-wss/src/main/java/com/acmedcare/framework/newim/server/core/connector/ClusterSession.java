package com.acmedcare.framework.newim.server.core.connector;

import com.google.common.collect.Maps;
import io.netty.channel.Channel;
import java.util.Map;

/**
 * Cluster Session
 *
 * @author <a href="mailto:iskp.me@gmail.com">Elve.Xu</a>
 * @version ${project.version} - 13/11/2018.
 */
public class ClusterSession {

  /**
   * 通讯服务器链接池
   *
   * <p>
   */
  private static Map<String, Channel> clusterChannelMappings = Maps.newConcurrentMap();


}
