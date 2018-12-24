package com.acmedcare.framework.remoting.mq.client.biz.request;

import com.acmedcare.tiffany.framework.remoting.android.core.exception.RemotingCommandException;
import com.acmedcare.tiffany.framework.remoting.android.core.protocol.CommandCustomHeader;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * FixTopicMessageListHeader
 *
 * @author <a href="mailto:iskp.me@gmail.com">Elve.Xu</a>
 * @version ${project.version} - 2018-12-24.
 */
@Getter
@Setter
@NoArgsConstructor
public class FixTopicMessageListHeader implements CommandCustomHeader {

  private String passport;

  private String passportId;

  private Long topicId;

  /** last message id */
  private Long lastTopicMessageId;

  private int limit = 10;

  @Override
  public void checkFields() throws RemotingCommandException {}
}
