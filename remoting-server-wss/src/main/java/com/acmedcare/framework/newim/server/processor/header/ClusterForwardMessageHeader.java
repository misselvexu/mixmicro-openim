package com.acmedcare.framework.newim.server.processor.header;

import com.acmedcare.framework.newim.Message;
import com.acmedcare.tiffany.framework.remoting.CommandCustomHeader;
import com.acmedcare.tiffany.framework.remoting.exception.RemotingCommandException;
import lombok.Getter;
import lombok.Setter;
import org.apache.commons.lang3.StringUtils;

/**
 * Forward Message Header
 *
 * @author <a href="mailto:iskp.me@gmail.com">Elve.Xu</a>
 * @version ${project.version} - 13/11/2018.
 */
@Getter
@Setter
public class ClusterForwardMessageHeader implements CommandCustomHeader {

  private String messageType;

  @Override
  public void checkFields() throws RemotingCommandException {
    if (StringUtils.isBlank(messageType)) {
      throw new RemotingCommandException("转发消息类型不能为空");
    }
  }

  /**
   * Decode Message Type
   *
   * @return type enum
   */
  public Message.MessageType decodeType() {
    return Message.MessageType.valueOf(messageType);
  }
}
