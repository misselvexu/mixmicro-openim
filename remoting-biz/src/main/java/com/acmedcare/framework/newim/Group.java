package com.acmedcare.framework.newim;

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

  @Builder
  public Group(
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

  @Getter
  @Setter
  @NoArgsConstructor
  public static class GroupMembers implements Serializable {

    private static final long serialVersionUID = 1474652111556171928L;

    private String groupId;
    private List<String> memberIds;

    @Builder
    public GroupMembers(String groupId, List<String> memberIds) {
      this.groupId = groupId;
      this.memberIds = memberIds;
    }
  }
}
