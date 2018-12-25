package com.acmedcare.framework.remoting.mq.client.biz.request;

import com.acmedcare.tiffany.framework.remoting.android.core.CFNotNull;
import com.acmedcare.tiffany.framework.remoting.android.core.exception.RemotingCommandException;
import com.acmedcare.tiffany.framework.remoting.android.core.protocol.CommandCustomHeader;
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

  @CFNotNull private String namespace = "MQ-DEFAULT";

  private String passport;

  private String passportId;

  /** 定于的主题标识 */
  private String[] topicIds;

  private String operateType;

  @Override
  public void checkFields() throws RemotingCommandException {}

  public enum OperateType {
    /** 订阅 */
    SUBSCRIBE,

    /** 取消订阅 */
    UB_SUBSCRIBE
  }
}
