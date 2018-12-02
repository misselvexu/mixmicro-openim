package com.acmedcare.framework.newim.master.endpoint.client;

import java.util.List;

/**
 * Master Endpoint Properties
 *
 * @author <a href="mailto:iskp.me@gmail.com">Elve.Xu</a>
 * @version ${project.version} - 29/11/2018.
 */
public class MasterEndpointProperties {

  /**
   * Remoting Address
   *
   * <p>host:port
   */
  private List<String> remotingAddresses;

  private boolean https = false;

  public MasterEndpointProperties(List<String> remotingAddresses) {
    this.remotingAddresses = remotingAddresses;
  }

  public MasterEndpointProperties(List<String> remotingAddresses, boolean https) {
    this.remotingAddresses = remotingAddresses;
    this.https = https;
  }

  public List<String> getRemotingAddresses() {
    return remotingAddresses;
  }

  public void setRemotingAddresses(List<String> remotingAddresses) {
    this.remotingAddresses = remotingAddresses;
  }

  public boolean isHttps() {
    return https;
  }

  public void setHttps(boolean https) {
    this.https = https;
  }
}
