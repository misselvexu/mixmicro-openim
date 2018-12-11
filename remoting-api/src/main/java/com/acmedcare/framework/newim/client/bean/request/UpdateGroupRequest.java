package com.acmedcare.framework.newim.client.bean.request;

import com.acmedcare.framework.newim.client.MessageConstants;
import java.io.Serializable;
import lombok.Getter;
import lombok.Setter;

/**
 * {@link UpdateGroupRequest}
 *
 * @author <a href="mailto:iskp.me@gmail.com">Elve.Xu</a>
 * @version ${project.version} - 21/11/2018.
 */
@Getter
@Setter
public class UpdateGroupRequest implements Serializable {

  private static final long serialVersionUID = 5788630159245539329L;

  private String namespace = MessageConstants.DEFAULT_NAMESPACE;

  private String groupId;
  private String groupName;
  private String groupOwner;
  /** 业务标识 */
  private String groupBizTag;
  /** 群组扩展信息 */
  private String groupExt;
}
