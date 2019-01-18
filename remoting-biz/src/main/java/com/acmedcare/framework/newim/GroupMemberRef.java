package com.acmedcare.framework.newim;

import com.acmedcare.framework.newim.storage.IMStorageCollections;
import java.io.Serializable;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;
import org.springframework.data.mongodb.core.index.CompoundIndex;
import org.springframework.data.mongodb.core.mapping.Document;

/**
 * Group Member Ref
 *
 * @author <a href="mailto:iskp.me@gmail.com">Elve.Xu</a>
 * @version ${project.version} - 14/11/2018.
 */
@Getter
@Setter
@Document(value = IMStorageCollections.REF_GROUP_MEMBER)
@CompoundIndex(
    unique = true,
    name = "unique_index_4_group_id_and_member_id_and_namespace",
    def = "{'groupId': 1, 'memberId': -1, 'namespace': 1}")
public class GroupMemberRef implements Serializable {

  private static final long serialVersionUID = 3547117751363118726L;

  private String namespace;
  private String groupId;
  private String memberId;
  private String memberName;
  private String memberUserName;
  private String portrait;
  private String memberExt;

  @Builder
  public GroupMemberRef(String groupId, String memberId, String memberName, String namespace, String memberUserName, String portrait, String memberExt) {
    this.groupId = groupId;
    this.memberId = memberId;
    this.memberName = memberName;
    this.namespace = namespace;
    this.memberExt = memberExt;
    this.memberUserName = memberUserName;
    this.portrait = portrait;
  }
}
