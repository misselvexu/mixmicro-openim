package com.acmedcare.tiffany.framework.remoting.jlib.biz.request;

import com.acmedcare.tiffany.framework.remoting.jlib.biz.bean.Group;
import java.util.List;
import lombok.Builder;
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
public class PullOwnerGroupListRequest extends BaseRequest {

  @Builder
  public PullOwnerGroupListRequest(String username, String passportId) {
    super(username, passportId);
  }

  public interface Callback {

    void onSuccess(List<Group> groups);

    void onFailed(int code, String message);
  }
}
