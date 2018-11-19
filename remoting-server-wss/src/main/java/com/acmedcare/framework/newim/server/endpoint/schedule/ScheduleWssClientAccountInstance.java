package com.acmedcare.framework.newim.server.endpoint.schedule;

import com.acmedcare.framework.aorp.beans.Principal;
import com.google.common.collect.Maps;
import java.io.Serializable;
import java.util.Map;
import lombok.Getter;
import lombok.Setter;

/**
 * Schedule Wss Client Account Instance
 *
 * @author <a href="mailto:iskp.me@gmail.com">Elve.Xu</a>
 * @version ${project.version} - 19/11/2018.
 */
@Getter
@Setter
public class ScheduleWssClientAccountInstance implements Serializable {

  private static final long serialVersionUID = 5656706549940066729L;

  /** 站点注册实例 */
  private ScheduleWssClientInstance scheduleWssClientInstance;

  /** 登录账号 */
  private Map<Long, Principal> principals = Maps.newConcurrentMap();
}
