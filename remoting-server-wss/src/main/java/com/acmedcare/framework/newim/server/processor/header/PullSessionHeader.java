package com.acmedcare.framework.newim.server.processor.header;

import com.acmedcare.tiffany.framework.remoting.CommandCustomHeader;
import com.acmedcare.tiffany.framework.remoting.annotation.CFNotNull;
import com.acmedcare.tiffany.framework.remoting.exception.RemotingCommandException;
import lombok.Getter;
import lombok.Setter;
import org.apache.commons.lang3.StringUtils;

/**
 * Pull Session Header
 *
 * @author <a href="mailto:iskp.me@gmail.com">Elve.Xu</a>
 * @version ${project.version} - 13/11/2018.
 */
@Getter
@Setter
public class PullSessionHeader implements CommandCustomHeader {

  @CFNotNull private String passport;
  @CFNotNull private String passportId;

  @Override
  public void checkFields() throws RemotingCommandException {
    if (StringUtils.isAnyBlank(passport, passportId)) {
      throw new RemotingCommandException("通行证编号不能为空");
    }
  }
}
