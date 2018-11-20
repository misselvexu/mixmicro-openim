package com.acmedcare.framework.newim.server.endpoint.schedule.processor;

import static com.acmedcare.framework.newim.server.ClusterLogger.wssServerLog;

import com.acmedcare.framework.aorp.beans.Principal;
import com.acmedcare.framework.boot.web.socket.processor.WssSession;
import com.acmedcare.framework.kits.Assert;
import com.acmedcare.framework.newim.protocol.Command.WebSocketClusterCommand;
import com.acmedcare.framework.newim.server.endpoint.WssMessageRequestProcessor;
import com.acmedcare.framework.newim.server.endpoint.schedule.ScheduleCommand.DefaultRequest;
import com.acmedcare.framework.newim.server.endpoint.schedule.ScheduleSysContext;
import com.acmedcare.framework.newim.server.exception.InvalidBizCodeException;
import com.acmedcare.framework.newim.server.exception.InvalidRequestParamsException;
import com.acmedcare.framework.newim.wss.WssPayload.WssResponse;
import com.acmedcare.tiffany.framework.remoting.common.Pair;
import com.acmedcare.tiffany.framework.remoting.common.RemotingHelper;
import org.apache.commons.lang3.StringUtils;

/**
 * Register Processor
 *
 * @author <a href="mailto:iskp.me@gmail.com">Elve.Xu</a>
 * @version ${project.version} - 19/11/2018.
 */
public class HeartbeatProcessor implements WssMessageRequestProcessor {

  private final ScheduleSysContext context;

  public HeartbeatProcessor(ScheduleSysContext context) {
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
    try {

      if (request instanceof DefaultRequest) {
        DefaultRequest defaultRequest = (DefaultRequest) request;

        if (StringUtils.isAnyBlank(
            defaultRequest.getOrgId(),
            defaultRequest.getAreaNo(),
            defaultRequest.getPassportId())) {
          throw new InvalidRequestParamsException("请求参数[orgId,areaNo,passportId]不能为空");
        }

        wssServerLog.info("[WSS] Schedule web client heartbeat params: {}", defaultRequest.json());
        Pair<Principal, WssSession> pair =
            context.getLocalSession(Long.parseLong(defaultRequest.getPassportId()));

        Assert.notNull(pair, "用户通行证编号不能为空");

        wssServerLog.info(
            "[WSS] Schedule web client:{} heartbeat succeed.",
            RemotingHelper.parseChannelRemoteAddr(session.channel()));
        return WssResponse.successResponse();

      } else {
        throw new InvalidBizCodeException("无效的请求指令");
      }
    } catch (Exception e) {
      wssServerLog.error("[WSS] Schedule web client heartbeat failed ", e);
      return WssResponse.failResponse(WebSocketClusterCommand.WS_HEARTBEAT, e.getMessage());
    }
  }
}
