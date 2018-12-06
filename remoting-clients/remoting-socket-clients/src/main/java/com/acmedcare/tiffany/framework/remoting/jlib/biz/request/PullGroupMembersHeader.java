package com.acmedcare.tiffany.framework.remoting.jlib.biz.request;

import com.acmedcare.tiffany.framework.remoting.android.core.CFNotNull;
import com.acmedcare.tiffany.framework.remoting.android.core.exception.RemotingCommandException;
import com.acmedcare.tiffany.framework.remoting.android.core.protocol.CommandCustomHeader;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Pull Group Members
 *
 * @author <a href="mailto:iskp.me@gmail.com">Elve.Xu</a>
 * @version ${project.version} - 2018-12-05.
 * @since 2.2.0
 */
@Getter
@Setter
@NoArgsConstructor
public class PullGroupMembersHeader implements CommandCustomHeader {

  /**
   * 群组编号
   *
   * <p>
   */
  @CFNotNull private String groupId;

  @Builder
  public PullGroupMembersHeader(String groupId) {
    this.groupId = groupId;
  }

  @Override
  public void checkFields() throws RemotingCommandException {}
}
