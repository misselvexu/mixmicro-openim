package com.acmedcare.framework.newim.server.mq.processor.header;

import com.acmedcare.tiffany.framework.remoting.CommandCustomHeader;
import com.acmedcare.tiffany.framework.remoting.annotation.CFNotNull;
import com.acmedcare.tiffany.framework.remoting.exception.RemotingCommandException;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * SubscribeTopicOperateHeader
 *
 * @author <a href="mailto:iskp.me@gmail.com">Elve.Xu</a>
 * @version ${project.version} - 2018-12-24.
 */
@Getter
@Setter
@NoArgsConstructor
public class SubscribeTopicOperateHeader implements CommandCustomHeader {

  @CFNotNull private String passport;

  @CFNotNull private String passportId;

  /** 定于的主题标识 */
  @CFNotNull private String[] topicIds;

  @CFNotNull private String operateType;

  public OperateType decodeOperateType() {
    return OperateType.valueOf(operateType);
  }

  @Override
  public void checkFields() throws RemotingCommandException {}

  public enum OperateType {
    /** 订阅 */
    SUBSCRIBE,

    /** 取消订阅 */
    UB_SUBSCRIBE
  }
}
