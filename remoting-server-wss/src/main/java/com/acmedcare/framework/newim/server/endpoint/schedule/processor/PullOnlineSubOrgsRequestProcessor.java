package com.acmedcare.framework.newim.server.endpoint.schedule.processor;

import static com.acmedcare.framework.newim.server.ClusterLogger.wssServerLog;

import com.acmedcare.framework.boot.web.socket.processor.WssSession;
import com.acmedcare.framework.newim.server.endpoint.WssMessageRequestProcessor;
import com.acmedcare.framework.newim.server.endpoint.schedule.ScheduleCommand;
import com.acmedcare.framework.newim.server.endpoint.schedule.ScheduleCommand.PullOnlineSubOrgsRequest;
import com.acmedcare.framework.newim.server.endpoint.schedule.ScheduleSysContext;
import com.acmedcare.framework.newim.server.endpoint.schedule.ScheduleWssClientInstance;
import com.acmedcare.framework.newim.server.exception.InvalidBizCodeException;
import com.acmedcare.framework.newim.server.exception.InvalidRequestParamsException;
import com.acmedcare.framework.newim.wss.WssPayload.WssResponse;
import com.alibaba.fastjson.JSON;
import java.util.List;
import org.apache.commons.lang3.StringUtils;

/**
 * Pull Processor
 *
 * @author <a href="mailto:iskp.me@gmail.com">Elve.Xu</a>
 * @version ${project.version} - 19/11/2018.
 */
public class PullOnlineSubOrgsRequestProcessor implements WssMessageRequestProcessor {

  private final ScheduleSysContext context;

  public PullOnlineSubOrgsRequestProcessor(ScheduleSysContext context) {
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
      if (request instanceof PullOnlineSubOrgsRequest) {
        PullOnlineSubOrgsRequest pullOnlineSubOrgsRequest = (PullOnlineSubOrgsRequest) request;

        if (StringUtils.isAnyBlank(
            pullOnlineSubOrgsRequest.getOrgId(),
            pullOnlineSubOrgsRequest.getPassportId(),
            pullOnlineSubOrgsRequest.getAreaNo())) {
          throw new InvalidRequestParamsException("请求参数[orgId,passportId,areaNo]不能为空");
        }

        //
        String orgId = pullOnlineSubOrgsRequest.getOrgId();
        List<ScheduleWssClientInstance> instances = context.querySubOrgs(orgId);
        wssServerLog.info("[WSS] 查询子站点在线列表返回值:{} ", JSON.toJSONString(instances));
        return WssResponse.successResponse(pullOnlineSubOrgsRequest.getBizCode(), instances);
      } else {
        throw new InvalidBizCodeException("无效的请求指令");
      }
    } catch (Exception e) {
      wssServerLog.error("[WSS] Schedule web client register failed ", e);
      return WssResponse.failResponse(
          ScheduleCommand.PULL_ONLINE_SUB_ORGS.getBizCode(), e.getMessage());
    }
  }
}
