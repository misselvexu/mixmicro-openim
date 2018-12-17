package com.acmedcare.framework.newim.server.master.connector;

import com.acmedcare.framework.newim.Message;
import com.acmedcare.framework.newim.Message.MessageType;
import com.acmedcare.framework.newim.SessionBean;
import java.util.List;
import java.util.Set;

/**
 * MasterConnectorContext
 *
 * @author <a href="mailto:iskp.me@gmail.com">Elve.Xu</a>
 * @version ${project.version} - 2018-12-17.
 */
public class MasterConnectorContext {

  private MasterConnectorHandler handler;

  void registerMasterConnectorHandler(MasterConnectorHandler handler) {
    this.handler = handler;
  }

  public void diff(Set<SessionBean> passportsConnections, Set<SessionBean> devicesConnections) {
    this.handler.processOnlineConnections(passportsConnections, devicesConnections);
  }

  public void onMasterMessage(String namespace, MessageType messageType, Message message) {
    this.handler.processMasterForwardMessage(namespace, messageType, message);
  }

  public void onPullClusterReplicas(Set<String> clusterReplicas) {
    this.handler.onClusterReplicas(clusterReplicas);
  }

  public List<SessionBean> getOnlinePassports() {
    return this.handler.getOnlinePassports();
  }

  public List<SessionBean> getOnlineDevices() {
    return this.handler.getOnlineDevices();
  }

  public Object getWssEndpoints() {
    return this.handler.getWssEndpoints();
  }
}
