package com.acmedcare.tiffany.framework.remoting.jlib.biz.request;

import com.acmedcare.tiffany.framework.remoting.android.core.CFNotNull;
import com.acmedcare.tiffany.framework.remoting.android.core.exception.RemotingCommandException;
import com.acmedcare.tiffany.framework.remoting.android.core.protocol.CommandCustomHeader;
import com.acmedcare.tiffany.framework.remoting.jlib.Constants;
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

  @CFNotNull private String namespace = Constants.DEFAULT_NAMESPACE;
  @CFNotNull private String groupId;
  @CFNotNull private String passportId;
  @CFNotNull private String memberName;
  private String memberUserName;
  private String portrait;
  private String memberExt;

  @CFNotNull private OperateType operateType;



  @Override
  public void checkFields() throws RemotingCommandException {}
}
