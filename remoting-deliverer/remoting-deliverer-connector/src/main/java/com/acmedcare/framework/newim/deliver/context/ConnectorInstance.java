/*
 * Copyright 1999-2018 Acmedcare+ Holding Ltd.
 * Licensed under the Apache License, Version 2.0 (the "License");
 */

package com.acmedcare.framework.newim.deliver.context;

import io.netty.channel.Channel;
import lombok.*;

import java.util.Objects;
import java.util.UUID;

/**
 * {@link ConnectorInstance}
 *
 * @author <a href="mailto:iskp.me@gmail.com">Elve.Xu</a>
 * @version ${project.version} - 2019-07-30.
 */
@Getter
@Setter
@NoArgsConstructor
@ToString
public class ConnectorInstance {

  protected long requestTimeout = 5000;

  protected String application;

  public enum Type {
    /** */
    SERVER,
    CLIENT
  }

  private String uniqueId = UUID.randomUUID().toString().replace("-", "");

  @Getter
  @Setter
  @ToString
  public static class ConnectorServerInstance extends ConnectorInstance {

    private String serverAddr;

    private boolean ssl;

    private boolean heartbeat;

    private long heartbeatPeriod;

    private long connectDelay = 5000;

    private int maxHeartbeatFailedTimes = 3;

    @Builder(toBuilder = true)
    public ConnectorServerInstance(
        String application,
        String serverAddr,
        boolean ssl,
        boolean heartbeat,
        long heartbeatPeriod,
        long connectDelay,
        long requestTimeout,
        int maxHeartbeatFailedTimes) {
      this.serverAddr = serverAddr;
      this.ssl = ssl;
      this.heartbeat = heartbeat;
      this.heartbeatPeriod = heartbeatPeriod;
      this.connectDelay = connectDelay;
      this.requestTimeout = requestTimeout;
      this.application = application;
      this.maxHeartbeatFailedTimes = maxHeartbeatFailedTimes;
    }

    @Override
    public boolean equals(Object o) {
      if (this == o) {
        return true;
      }
      if (o == null || getClass() != o.getClass()) {
        return false;
      }
      ConnectorServerInstance that = (ConnectorServerInstance) o;
      return serverAddr.equals(that.serverAddr);
    }

    @Override
    public int hashCode() {
      return Objects.hash(serverAddr);
    }
  }

  @Getter
  @Setter
  @Builder
  @ToString
  @NoArgsConstructor
  @AllArgsConstructor
  public static class ConnectorClientInstance extends ConnectorInstance {

    private String clientId;

    private Channel channel;

    @Override
    public boolean equals(Object o) {
      if (this == o) {
        return true;
      }
      if (o == null || getClass() != o.getClass()) {
        return false;
      }
      ConnectorClientInstance that = (ConnectorClientInstance) o;
      return clientId.equals(that.clientId);
    }

    @Override
    public int hashCode() {
      return Objects.hash(clientId);
    }
  }
}
