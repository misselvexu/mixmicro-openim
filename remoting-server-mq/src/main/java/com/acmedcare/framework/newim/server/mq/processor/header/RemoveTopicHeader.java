package com.acmedcare.framework.newim.server.mq.processor.header;

import com.acmedcare.tiffany.framework.remoting.CommandCustomHeader;
import com.acmedcare.tiffany.framework.remoting.annotation.CFNotNull;
import com.acmedcare.tiffany.framework.remoting.exception.RemotingCommandException;
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
