package com.acmedcare.microservices.im.core;

import io.netty.channel.Channel;
import java.util.Objects;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Client Channel Holder
 *
 * @author Elve.Xu [iskp.me<at>gmail.com]
 * @version v1.0 - 09/08/2018.
 */
@Getter
@Setter
@NoArgsConstructor
public class ClientChannel {

  /** Client Remoting Channel */
  private Channel channel;

  @Builder
  public ClientChannel(Channel channel) {
    this.channel = channel;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (!(o instanceof ClientChannel)) {
      return false;
    }
    ClientChannel that = (ClientChannel) o;
    return Objects.equals(getChannel(), that.getChannel());
  }

  @Override
  public int hashCode() {
    return Objects.hash(getChannel());
  }

  @Override
  public String toString() {
    final StringBuffer sb = new StringBuffer("ClientChannel{");
    sb.append("channel=").append(channel);
    sb.append('}');
    return sb.toString();
  }
}
