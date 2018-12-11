package com.acmedcare.framework.newim;

import com.acmedcare.framework.newim.client.MessageConstants;
import com.acmedcare.framework.newim.client.bean.Member;
import com.acmedcare.framework.newim.storage.IMStorageCollections;
import java.io.Serializable;
import java.util.List;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

/**
 * Group
 *
 * @author <a href="mailto:iskp.me@gmail.com">Elve.Xu</a>
 * @version v1.0 - 09/08/2018.
 */
@Getter
@Setter
@NoArgsConstructor
@Document(value = IMStorageCollections.GROUP)
public class Group implements Serializable {

  private static final long serialVersionUID = -7613061811845762121L;

  @Indexed(unique = true)
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

  private String namespace = MessageConstants.DEFAULT_NAMESPACE;

  @Builder
  public Group(
      String groupId,
      String groupOwner,
      String groupName,
      String groupBizTag,
      String groupExt,
      Status groupStatus,
      String namespace) {
    this.groupId = groupId;
    this.groupOwner = groupOwner;
    this.groupName = groupName;
    this.groupBizTag = groupBizTag;
    this.groupExt = groupExt;
    this.groupStatus = groupStatus;
    if (this.groupStatus == null) {
      this.groupStatus = Status.ENABLED;
    }
    this.namespace = namespace;
    if (this.namespace == null) {
      this.namespace = MessageConstants.DEFAULT_NAMESPACE;
    }
  }

  @Getter
  @Setter
  @NoArgsConstructor
  public static class GroupMembers implements Serializable {

    private static final long serialVersionUID = 1474652111556171928L;

    private String namespace;
    private String groupId;
    private List<Member> members;

    @Builder
    public GroupMembers(String namespace, String groupId, List<Member> members) {
      this.namespace = namespace;
      this.groupId = groupId;
      this.members = members;
    }
  }
}
