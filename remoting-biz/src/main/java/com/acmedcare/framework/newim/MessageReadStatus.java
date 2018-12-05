package com.acmedcare.framework.newim;

import com.acmedcare.framework.newim.storage.IMStorageCollections;
import java.io.Serializable;
import java.util.Date;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.data.mongodb.core.index.CompoundIndex;
import org.springframework.data.mongodb.core.mapping.Document;

/**
 * Message Read Status Record
 *
 * @author <a href="mailto:iskp.me@gmail.com">Elve.Xu</a>
 * @version ${project.version} - 2018-12-05.
 */
@Getter
@Setter
@NoArgsConstructor
@Document(value = IMStorageCollections.MESSAGE_READ_STATUS)
@CompoundIndex(
    unique = true,
    name = "unique_index_4_group_id_and_member_id",
    def = "{'groupId': 1, 'messageId': 1, 'memberId': -1}")
public class MessageReadStatus implements Serializable {

  private static final long serialVersionUID = -2060600405103812899L;

  private String groupId;

  private String messageId;

  private Long memberId;

  private Date readTimestamp = new Date();
}
