package com.acmedcare.tiffany.framework.remoting.jlib.biz.body;

import com.acmedcare.tiffany.framework.remoting.jlib.biz.bean.Group;
import java.io.Serializable;
import java.util.List;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Group Return Response Body
 *
 * @author <a href="mailto:iskp.me@gmail.com">Elve.Xu</a>
 * @version v1.0 - 09/08/2018.
 */
@Getter
@Setter
@NoArgsConstructor
public class GroupBody implements Serializable {

  private static final long serialVersionUID = -7946780164235571201L;

  private List<Group> groups;

  @Builder
  public GroupBody(List<Group> groups) {
    this.groups = groups;
  }
}
