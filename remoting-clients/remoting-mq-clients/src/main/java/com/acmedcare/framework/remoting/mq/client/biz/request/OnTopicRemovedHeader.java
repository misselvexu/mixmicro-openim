package com.acmedcare.framework.remoting.mq.client.biz.request;

import com.acmedcare.tiffany.framework.remoting.android.core.exception.RemotingCommandException;
import com.acmedcare.tiffany.framework.remoting.android.core.protocol.CommandCustomHeader;
import lombok.Getter;
import lombok.Setter;

/**
 * OnTopicRemovedHeader
 *
 * @author <a href="mailto:iskp.me@gmail.com">Elve.Xu</a>
 * @version ${project.version} - 2018-12-29.
 */
@Getter
@Setter
public class OnTopicRemovedHeader implements CommandCustomHeader {

  private Long topicId;

  @Override
  public void checkFields() throws RemotingCommandException {}
}
