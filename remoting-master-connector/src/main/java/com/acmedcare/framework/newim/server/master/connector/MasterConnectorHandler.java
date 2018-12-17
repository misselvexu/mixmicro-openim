package com.acmedcare.framework.newim.server.master.connector;

import com.acmedcare.framework.newim.Message;
import com.acmedcare.framework.newim.Message.MessageType;
import com.acmedcare.framework.newim.SessionBean;
import com.google.common.collect.Lists;
import java.util.List;
import java.util.Set;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * MasterConnectorHandler
 *
 * @author <a href="mailto:iskp.me@gmail.com">Elve.Xu</a>
 * @version ${project.version} - 2018-12-17.
 */
public interface MasterConnectorHandler {

  Logger logger = LoggerFactory.getLogger(MasterConnectorHandler.class);

  /**
   * Processor Master Sync Online Connections Data
   *
   * @param passportsConnections mapping-ed by passportId
   * @param devicesConnections mapping-ed by devicesId
   */
  default void processOnlineConnections(
      Set<SessionBean> passportsConnections, Set<SessionBean> devicesConnections) {
    logger.info(
        "Rvd Master Connections : {} , {}", passportsConnections.size(), devicesConnections.size());
  }

  /**
   * Processor Master Sync Forward Message
   *
   * @param namespace namespace
   * @param messageType message type
   * @param message message instance of {@link Message}
   * @see MessageType
   * @see Message
   * @see com.acmedcare.framework.newim.Message.SingleMessage
   * @see com.acmedcare.framework.newim.Message.GroupMessage
   * @see com.acmedcare.framework.newim.Message.PushMessage
   */
  default void processMasterForwardMessage(
      String namespace, MessageType messageType, Message message) {
    logger.info("Rvd Master Message : {} , {} ,{}", namespace, messageType, message.toString());
  }

  /**
   * On Cluster Replicas Data from Master Server
   *
   * @param clusterReplicas response replicas
   */
  default void onClusterReplicas(Set<String> clusterReplicas) {
    logger.info("Rvd CLuster Replicas Data : {}", clusterReplicas);
  }

  /**
   * Get Current CLuster All Connected Client Passports
   *
   * @return Passport Ids list
   */
  default List<SessionBean> getOnlinePassports() {
    return Lists.newArrayList();
  }

  /**
   * Get Current CLuster All Connected Client Devices
   *
   * @return Device Ids List
   */
  default List<SessionBean> getOnlineDevices() {
    return Lists.newArrayList();
  }

  /**
   * Get Web Socket Endpoints
   *
   * @return o
   */
  default Object getWssEndpoints() {
    return null;
  }
}
