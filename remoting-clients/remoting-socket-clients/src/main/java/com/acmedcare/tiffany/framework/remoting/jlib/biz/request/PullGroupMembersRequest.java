package com.acmedcare.tiffany.framework.remoting.jlib.biz.request;

import com.acmedcare.tiffany.framework.remoting.jlib.Constants;
import com.acmedcare.tiffany.framework.remoting.jlib.biz.bean.Member;
import java.util.List;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Pull Owner Group List request
 *
 * @author <a href="mailto:iskp.me@gmail.com">Elve.Xu</a>
 * @version alpha - 10/08/2018.
 */
@Getter
@Setter
@NoArgsConstructor
public class PullGroupMembersRequest extends BaseRequest {
  private String namespace = Constants.DEFAULT_NAMESPACE;

  private String groupId;

  public interface Callback {
    void onSuccess(List<Member> members);

    void onFailed(int code, String message);
  }
}
