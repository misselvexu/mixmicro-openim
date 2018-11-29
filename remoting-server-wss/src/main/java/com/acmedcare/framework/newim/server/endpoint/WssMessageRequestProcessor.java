package com.acmedcare.framework.newim.server.endpoint;

import com.acmedcare.framework.boot.web.socket.processor.WssSession;
import com.acmedcare.framework.newim.wss.WssPayload.WssResponse;

/**
 * Schedule Message Request Processor
 *
 * @author <a href="mailto:iskp.me@gmail.com">Elve.Xu</a>
 * @version ${project.version} - 19/11/2018.
 */
public interface WssMessageRequestProcessor {

  /**
   * Processor Wss Client Request
   *
   * @param session session
   * @param request request
   * @return response
   * @throws Exception exception
   */
  WssResponse processRequest(WssSession session, Object request) throws Exception;
}
