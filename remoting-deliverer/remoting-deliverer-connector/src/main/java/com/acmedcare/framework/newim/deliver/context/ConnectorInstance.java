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

  public enum Type {
    /** */
    SERVER,
    CLIENT
  }

  private String uniqueId = UUID.randomUUID().toString().replace("-", "");

  @Getter
  @Setter
  @Builder
  @ToString
  @NoArgsConstructor
  @AllArgsConstructor
  public static class ConnectorServerInstance extends ConnectorInstance {

    private String serverAddr;

    private boolean ssl;

    private boolean heartbeat;

    private long heartbeatPeriod;

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

    private String host;

    private int port;

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
      return port == that.port && host.equals(that.host);
    }

    @Override
    public int hashCode() {
      return Objects.hash(host, port);
    }
  }
}
