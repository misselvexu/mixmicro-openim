package com.acmedcare.framework.newim.server.endpoint.schedule.processor;

import com.acmedcare.framework.boot.web.socket.processor.WssSession;
import com.acmedcare.framework.newim.server.endpoint.WssMessageRequestProcessor;
import com.acmedcare.framework.newim.server.endpoint.schedule.ScheduleCommand;
import com.acmedcare.framework.newim.server.endpoint.schedule.ScheduleSysContext;
import com.acmedcare.framework.newim.wss.WssPayload.WssResponse;

/**
 * Register Processor
 *
 * @author <a href="mailto:iskp.me@gmail.com">Elve.Xu</a>
 * @version ${project.version} - 19/11/2018.
 */
@Deprecated
public class PushOrderProcessor implements WssMessageRequestProcessor {

  private final ScheduleSysContext context;

  public PushOrderProcessor(ScheduleSysContext context) {
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
  public WssResponse processRequest(WssSession session, Object request) throws Exception {
    return WssResponse.failResponse(
        ScheduleCommand.PUSH_ORDER.getBizCode(), "Deprecated request command .");
  }
}
