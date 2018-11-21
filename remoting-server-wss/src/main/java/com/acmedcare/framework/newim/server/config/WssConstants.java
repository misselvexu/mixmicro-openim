package com.acmedcare.framework.newim.server.config;

/**
 * Wss Constants
 *
 * @author <a href="mailto:iskp.me@gmail.com">Elve.Xu</a>
 * @version ${project.version} - 19/11/2018.
 */
public interface WssConstants {

  String WSS_ENDPOINTS = "wss.endpoints";

  /**
   * WebSocket Config Port Key
   *
   * <p>
   */
  String WSS_PORT_KEY = "%s.port";

  String WSS_HOST_KEY = "%s.host";

  /**
   * Default Port For WebSocket
   *
   * <p>
   */
  int WSS_PORT_KEY_DEFAULT_VALUE = 8888;
}
