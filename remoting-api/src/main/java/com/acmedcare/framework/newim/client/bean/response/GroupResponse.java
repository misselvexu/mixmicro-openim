package com.acmedcare.framework.newim.client.bean.response;

import com.acmedcare.framework.newim.client.bean.Status;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Group Response
 *
 * @author <a href="mailto:iskp.me@gmail.com">Elve.Xu</a>
 * @version ${project.version} - 29/11/2018.
 */
@Getter
@Setter
@NoArgsConstructor
public class GroupResponse {

  private String groupId;

  /** 群主 */
  private String groupOwner;

  /** 群组名称 */
  private String groupName;
  /** 业务标识 */
  private String groupBizTag;

  /** 群组扩展信息 */
  private String groupExt;

  private Status groupStatus;

  @Builder
  public GroupResponse(
      String groupId,
      String groupOwner,
      String groupName,
      String groupBizTag,
      String groupExt,
      Status groupStatus) {
    this.groupId = groupId;
    this.groupOwner = groupOwner;
    this.groupName = groupName;
    this.groupBizTag = groupBizTag;
    this.groupExt = groupExt;
    this.groupStatus = groupStatus;
    if (this.groupStatus == null) {
      this.groupStatus = Status.ENABLED;
    }
  }
}
