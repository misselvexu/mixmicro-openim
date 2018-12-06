package com.acmedcare.tiffany.framework.remoting.jlib.biz.request;

import com.acmedcare.tiffany.framework.remoting.jlib.biz.bean.Member;
import java.util.List;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Pull Group Members Online Status Request
 *
 * @author <a href="mailto:iskp.me@gmail.com">Elve.Xu</a>
 * @version alpha - 10/08/2018.
 */
@Getter
@Setter
@NoArgsConstructor
public class PullGroupMembersOnlineStatusRequest extends BaseRequest {

  private String groupId;

  public interface Callback {

    void onSuccess(List<Member> members);

    void onFailed(int code, String message);
  }
}
