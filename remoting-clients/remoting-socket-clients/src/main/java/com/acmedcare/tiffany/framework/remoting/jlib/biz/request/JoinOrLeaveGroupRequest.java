package com.acmedcare.tiffany.framework.remoting.jlib.biz.request;

import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Join Or Leave Group Request
 *
 * @author <a href="mailto:iskp.me@gmail.com">Elve.Xu</a>
 * @version ${project.version} - 2018-12-03.
 */
@Getter
@Setter
@NoArgsConstructor
public class JoinOrLeaveGroupRequest {

  private String groupId;
  private String passportId;
  private String memberName;
  private OperateType operateType;

  @Builder
  public JoinOrLeaveGroupRequest(String groupId, String passportId, String memberName,OperateType operateType) {
    this.groupId = groupId;
    this.passportId = passportId;
    this.memberName = memberName;
    this.operateType = operateType;
  }

  public interface Callback {
    void onSuccess();

    void onFailed(int code, String message);
  }
}
