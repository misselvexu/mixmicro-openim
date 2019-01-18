package com.acmedcare.tiffany.framework.remoting.jlib.biz.request;

import com.acmedcare.tiffany.framework.remoting.jlib.Constants;
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

  private String namespace = Constants.DEFAULT_NAMESPACE;
  private String groupId;
  private String passportId;
  private String memberName;
  private String memberUserName;
  private String portrait;
  private String memberExt;
  private OperateType operateType;

  @Builder
  public JoinOrLeaveGroupRequest(
      String groupId,
      String passportId,
      String memberName,
      String memberExt,
      String memberUserName,
      String portrait,
      OperateType operateType,
      String namespace) {
    this.groupId = groupId;
    this.passportId = passportId;
    this.memberName = memberName;
    this.operateType = operateType;
    this.namespace = namespace;
    this.memberExt = memberExt;
    this.memberUserName = memberUserName;
    this.portrait = portrait;
  }

  public interface Callback {
    void onSuccess();

    void onFailed(int code, String message);
  }
}
