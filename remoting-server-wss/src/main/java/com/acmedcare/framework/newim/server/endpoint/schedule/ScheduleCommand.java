package com.acmedcare.framework.newim.server.endpoint.schedule;

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

  int bizCode;

  Class<?> headerClass;

  Class<?> requestClass;

  ScheduleCommand(int bizCode, Class<?> headerClass, Class<?> requestClass) {
    this.bizCode = bizCode;
    this.headerClass = headerClass;
    this.requestClass = requestClass;
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
