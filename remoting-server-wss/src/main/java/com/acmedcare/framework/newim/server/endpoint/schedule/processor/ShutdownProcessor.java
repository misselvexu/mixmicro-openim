package com.acmedcare.framework.newim.server.endpoint.schedule.processor;

import com.acmedcare.framework.boot.web.socket.processor.WssSession;
import com.acmedcare.framework.newim.server.endpoint.WssMessageRequestProcessor;
import com.acmedcare.framework.newim.server.endpoint.schedule.ScheduleSysContext;
import com.acmedcare.framework.newim.wss.WssPayload.WssRequest;
import com.acmedcare.framework.newim.wss.WssPayload.WssResponse;

/**
 * Register Processor
 *
 * @author <a href="mailto:iskp.me@gmail.com">Elve.Xu</a>
 * @version ${project.version} - 19/11/2018.
 */
public class ShutdownProcessor implements WssMessageRequestProcessor {

  private final ScheduleSysContext context;

  public ShutdownProcessor(ScheduleSysContext context) {
    this.context = context;
  }

  /**
   * Processor Wss Client Request
   *
   * @param session session
   * @param request request
   * @return response
   * @throws Exception exception
   */
  @Override
  public WssResponse processRequest(WssSession session, WssRequest request) throws Exception {

    return null;
  }
}
