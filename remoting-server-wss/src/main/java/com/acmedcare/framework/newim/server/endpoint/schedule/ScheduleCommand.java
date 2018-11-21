package com.acmedcare.framework.newim.server.endpoint.schedule;

import com.acmedcare.framework.newim.protocol.Command.WebSocketClusterCommand;
import com.acmedcare.framework.newim.wss.WssPayload.WssRequest;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import lombok.Getter;
import lombok.Setter;

/**
 * Schedule Command
 *
 * @author <a href="mailto:iskp.me@gmail.com">Elve.Xu</a>
 * @version ${project.version} - 19/11/2018.
 */
@Getter
public enum ScheduleCommand {
  AUTH(0x30000, AuthRequest.class),
  PULL_ONLINE_SUB_ORGS(0x31001, PullOnlineSubOrgsRequest.class),
  PUSH_ORDER(0x31002, PushOrderRequest.class),

  WS_REGISTER(WebSocketClusterCommand.WS_REGISTER, RegisterRequest.class),
  WS_SHUTDOWN(WebSocketClusterCommand.WS_HEARTBEAT, DefaultRequest.class),
  WS_HEARTBEAT(WebSocketClusterCommand.WS_HEARTBEAT, DefaultRequest.class);

  private static final String BIZ_CODE = "bizCode";
  int bizCode;
  Class<?> requestClass;

  ScheduleCommand(int bizCode, Class<?> requestClass) {
    this.bizCode = bizCode;
    this.requestClass = requestClass;
  }

  public static ScheduleCommand parseCommand(String message) {
    JSONObject temp = JSONObject.parseObject(message);
    if (temp.containsKey(BIZ_CODE)) {
      return parseCommand(temp.getInteger(BIZ_CODE));
    }
    throw new IllegalArgumentException("[WSS] 无效的业务请求,为携带协议编码");
  }

  public static ScheduleCommand parseCommand(int bizCode) {
    for (ScheduleCommand value : ScheduleCommand.values()) {
      if (value.getBizCode() == bizCode) {
        return value;
      }
    }
    throw new IllegalArgumentException("[WSS] 无效的业务参数编码:" + bizCode);
  }

  public WssRequest parseRequest(String message) {
    return (WssRequest) JSON.parseObject(message, getRequestClass());
  }

  @Getter
  @Setter
  public static class PullOnlineSubOrgsRequest extends DefaultRequest {}

  @Getter
  @Setter
  public static class AuthRequest extends WssRequest {
    private String accessToken;
    private String wssClientType;
  }

  @Getter
  @Setter
  public static class RegisterRequest extends DefaultRequest {

    /** 机构名称 */
    private String orgName;
    /** 父机构编号 */
    private String parentOrgId;
  }

  @Getter
  @Setter
  public static class DefaultRequest extends WssRequest {

    /** 通行证编号 */
    private String passportId;

    private String areaNo;

    private String orgId;
  }

  @Getter
  @Setter
  public static class PushOrderRequest extends DefaultRequest {

    /** 订单详情信息 */
    private String orderDetail;
    /** 接受分站标识 */
    private String subOrgId;
  }
}
