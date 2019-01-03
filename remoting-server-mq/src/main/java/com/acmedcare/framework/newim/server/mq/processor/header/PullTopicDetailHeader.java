package com.acmedcare.framework.newim.server.mq.processor.header;

import com.acmedcare.tiffany.framework.remoting.CommandCustomHeader;
import com.acmedcare.tiffany.framework.remoting.annotation.CFNotNull;
import com.acmedcare.tiffany.framework.remoting.exception.RemotingCommandException;
import lombok.Getter;
import lombok.Setter;

/**
 * PullTopicListHeader
 *
 * @author <a href="mailto:iskp.me@gmail.com">Elve.Xu</a>
 * @version ${project.version} - 2018-12-24.
 */
@Getter
@Setter
public class PullTopicDetailHeader implements CommandCustomHeader {

  @CFNotNull private String namespace = "MQ-DEFAULT";

  @CFNotNull private String passport;

  @CFNotNull private String passportId;

  private Long topicId;

  @Override
  public void checkFields() throws RemotingCommandException {}
}
