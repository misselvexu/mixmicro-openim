package com.acmedcare.framework.newim.server.endpoint.schedule;

import com.alibaba.fastjson.JSONObject;
import lombok.Getter;

/**
 * Schedule Command
 *
 * @author <a href="mailto:iskp.me@gmail.com">Elve.Xu</a>
 * @version ${project.version} - 19/11/2018.
 */
@Getter
public enum ScheduleCommand {

  /**
   * 拉取在线子机构列表
   *
   * <p>
   */
  PULL_ONLINE_SUB_ORGS(0x31001, null, null);

  private static final String BIZ_CODE = "bizCode";
  int bizCode;
  Class<?> headerClass;
  Class<?> requestClass;

  ScheduleCommand(int bizCode, Class<?> headerClass, Class<?> requestClass) {
    this.bizCode = bizCode;
    this.headerClass = headerClass;
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
}
