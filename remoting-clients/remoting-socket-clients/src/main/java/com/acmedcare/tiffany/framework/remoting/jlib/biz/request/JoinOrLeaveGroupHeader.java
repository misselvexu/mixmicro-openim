package com.acmedcare.tiffany.framework.remoting.jlib.biz.request;

import com.acmedcare.tiffany.framework.remoting.android.core.CFNotNull;
import com.acmedcare.tiffany.framework.remoting.android.core.exception.RemotingCommandException;
import com.acmedcare.tiffany.framework.remoting.android.core.protocol.CommandCustomHeader;
import lombok.Getter;
import lombok.Setter;

/**
 * Join Group Header
 *
 * @author <a href="mailto:iskp.me@gmail.com">Elve.Xu</a>
 * @version ${project.version} - 2018-12-03.
 */
@Getter
@Setter
public class JoinOrLeaveGroupHeader implements CommandCustomHeader {

  @CFNotNull private String groupId;
  @CFNotNull private String passportId;
  @CFNotNull private String memberName;
  @CFNotNull private OperateType operateType;

  @Override
  public void checkFields() throws RemotingCommandException {}
}
