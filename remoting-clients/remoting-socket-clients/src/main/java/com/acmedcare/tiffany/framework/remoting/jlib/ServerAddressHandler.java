package com.acmedcare.tiffany.framework.remoting.jlib;

import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

/**
 * Server Address
 *
 * @author <a href="mailto:iskp.me@gmail.com">Elve.Xu</a>
 * @version alpha - 26/07/2018.
 */
public interface ServerAddressHandler {

  /**
   * Return Remoting Address List
   *
   * @return address list
   */
  List<RemotingAddress> remotingAddressList();

  /** Remoting Address Class */
  @Getter
  @Setter
  @AllArgsConstructor
  public static class RemotingAddress {

    private boolean https = false;
    /** address host * */
    private String host;

    /** address port * */
    private Integer port;

    /** data transform ssl encrpty * */
    private boolean ssl = false;

    @Override
    public int hashCode() {
      return super.hashCode();
    }

    @Override
    public boolean equals(Object obj) {
      if (obj instanceof RemotingAddress) {
        RemotingAddress temp = (RemotingAddress) obj;
        return temp.host.equals(this.host) && temp.port.equals(this.port) && temp.ssl == this.ssl;
      }
      return false;
    }

    @Override
    public String toString() {
      return this.host + ":" + this.port;
    }
  }
}
