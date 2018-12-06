package com.acmedcare.framework.newim.client.bean.request;

import com.acmedcare.framework.newim.client.bean.Member;
import java.io.Serializable;
import java.util.List;
import lombok.Getter;
import lombok.Setter;

/**
 * {@link AddGroupMembersRequest}
 *
 * @author <a href="mailto:iskp.me@gmail.com">Elve.Xu</a>
 * @version ${project.version} - 21/11/2018.
 */
@Getter
@Setter
public class AddGroupMembersRequest implements Serializable {

  private static final long serialVersionUID = 4499697628571057249L;

  private String groupId;
  private List<Member> members;
}
