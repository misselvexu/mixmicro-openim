package com.acmedcare.framework.remoting.mq.client.biz.request;

import com.acmedcare.tiffany.framework.remoting.android.core.CFNotNull;
import com.acmedcare.tiffany.framework.remoting.android.core.exception.RemotingCommandException;
import com.acmedcare.tiffany.framework.remoting.android.core.protocol.CommandCustomHeader;
import lombok.Getter;
import lombok.Setter;

/**
 * RemoveTopicHeader
 *
 * @author <a href="mailto:iskp.me@gmail.com">Elve.Xu</a>
 * @version ${project.version} - 2018-12-24.
 */
@Getter
@Setter
public class RemoveTopicHeader implements CommandCustomHeader {

  @CFNotNull private String namespace = "MQ-DEFAULT";

  private String passport;

  private String passportId;

  /** 主题名称 */
  @CFNotNull private Long topicId;

  @Override
  public void checkFields() throws RemotingCommandException {}
}
