package com.acmedcare.framework.newim.server.endpoint.schedule;

import java.io.Serializable;
import java.util.Objects;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

/**
 * Schedule Wss Client Instance
 *
 * @author <a href="mailto:iskp.me@gmail.com">Elve.Xu</a>
 * @version ${project.version} - 19/11/2018.
 */
@Getter
@Setter
@Builder
public class ScheduleWssClientInstance implements Serializable {

  private static final long serialVersionUID = -5814623669676970073L;

  /** 区域编号 */
  private String areaNo;

  /** 机构编码 */
  private String orgId;

  /** 机构名称 */
  private String orgName;

  /** 父机构编号 */
  private String parentOrgId;

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (!(o instanceof ScheduleWssClientInstance)) {
      return false;
    }
    ScheduleWssClientInstance that = (ScheduleWssClientInstance) o;
    return Objects.equals(getAreaNo(), that.getAreaNo())
        && Objects.equals(getOrgId(), that.getOrgId());
  }

  @Override
  public int hashCode() {
    return Objects.hash(getAreaNo(), getOrgId());
  }
}
