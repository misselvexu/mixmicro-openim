package com.acmedcare.framework.newim.protocol.request;

import com.acmedcare.framework.newim.InstanceNode;
import java.io.Serializable;
import java.util.Map;
import java.util.Set;
import lombok.Getter;
import lombok.Setter;

/**
 * Data body
 *
 * @author <a href="mailto:iskp.me@gmail.com">Elve.Xu</a>
 * @version ${project.version} - 14/11/2018.
 */
@Getter
@Setter
public class MasterNoticeSessionDataBody implements Serializable {

  private static final long serialVersionUID = -5706666156157170632L;

  /** 同步登陆的通行证数据 */
  private Map<InstanceNode, Set<String>> passportsConnections;
  /** 同步登陆的设备数据 */
  private Map<InstanceNode, Set<String>> devicesConnections;
}
