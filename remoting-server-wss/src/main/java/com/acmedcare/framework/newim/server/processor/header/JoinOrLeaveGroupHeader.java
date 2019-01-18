package com.acmedcare.framework.newim.server.processor.header;

import com.acmedcare.framework.newim.client.MessageConstants;
import com.acmedcare.tiffany.framework.remoting.CommandCustomHeader;
import com.acmedcare.tiffany.framework.remoting.annotation.CFNotNull;
import com.acmedcare.tiffany.framework.remoting.exception.RemotingCommandException;
import lombok.Getter;
import lombok.Setter;
import org.apache.commons.lang3.StringUtils;

/**
 * Join Group Header
 *
 * @author <a href="mailto:iskp.me@gmail.com">Elve.Xu</a>
 * @version ${project.version} - 2018-12-03.
 */
@Getter
@Setter
public class JoinOrLeaveGroupHeader implements CommandCustomHeader {
  private String namespace = MessageConstants.DEFAULT_NAMESPACE;
  @CFNotNull private String groupId;
  @CFNotNull private String passportId;
  @CFNotNull private String memberName;
  @CFNotNull private String operateType;
  private String memberUserName;
  private String portrait;
  private String memberExt;

  public OperateType decodeOperateType() {
    return OperateType.valueOf(this.operateType);
  }

  @Override
  public void checkFields() throws RemotingCommandException {
    if (StringUtils.isAnyBlank(groupId, passportId)) {
      throw new RemotingCommandException("请求参数[groupId,passportId]不能为空");
    }
    if (operateType == null) {
      throw new RemotingCommandException("请求参数[operateType]不能为空");
    } else {
      if (OperateType.JOIN.name().equals(operateType)) {
        if (StringUtils.isAnyBlank(memberName)) {
          throw new RemotingCommandException("请求参数[memberName]不能为空");
        }
      }
    }
  }
}
