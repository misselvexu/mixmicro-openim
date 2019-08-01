package com.acmedcare.framework.newim.server.master.connector;

import com.acmedcare.framework.kits.lang.Nullable;
import com.acmedcare.framework.newim.Message;
import com.acmedcare.framework.newim.Message.MessageType;
import com.acmedcare.framework.newim.SessionBean;
import com.google.common.collect.Lists;

import java.util.List;
import java.util.Set;

/**
 * MasterConnectorContext
 *
 * @author <a href="mailto:iskp.me@gmail.com">Elve.Xu</a>
 * @version ${project.version} - 2018-12-17.
 */
public class DefaultMasterConnectorContext extends MasterConnectorContext{

  private DefaultMasterConnectorHandler handler;

  void registerMasterConnectorHandler(@Nullable DefaultMasterConnectorHandler handler) {
    this.handler = handler;
  }

  public void diff(Set<SessionBean> passportsConnections, Set<SessionBean> devicesConnections) {
    if (handler != null) {
      this.handler.processOnlineConnections(passportsConnections, devicesConnections);
    }
  }

  public void onMasterMessage(String namespace, MessageType messageType, Message message) {
    if (handler != null) {
      this.handler.processMasterForwardMessage(namespace, messageType, message);
    }
  }

  public void onPullClusterReplicas(Set<String> clusterReplicas) {
    if (handler != null) {
      this.handler.onServerNodeReplicas(clusterReplicas);
    }
  }

  public List<SessionBean> getOnlinePassports() {
    if (handler != null) {
      return this.handler.getOnlinePassports();
    }
    return Lists.newArrayList();
  }

  public List<SessionBean> getOnlineDevices() {
    if (handler != null) {
      return this.handler.getOnlineDevices();
    }
    return Lists.newArrayList();
  }

  public Object getWssEndpoints() {
    if (handler != null) {
      return this.handler.getWssEndpoints();
    }
    return null;
  }
}
