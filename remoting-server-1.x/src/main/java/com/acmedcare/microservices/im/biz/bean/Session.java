package com.acmedcare.microservices.im.biz.bean;

import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Session
 *
 * @author <a href="mailto:iskp.me@gmail.com">Elve.Xu</a>
 * @version v1.0 - 09/08/2018.
 */
@Getter
@Setter
@NoArgsConstructor
public class Session {

  private String name;

  private String groupName;

  // 0-单聊  1-群组
  private int type;

  private int unreadSize;

  private Message leastMessage;

  @Builder
  public Session(String name, int unreadSize, Message leastMessage, int type, String groupName) {
    this.name = name;
    this.unreadSize = unreadSize;
    this.leastMessage = leastMessage;
    this.type = type;
    this.groupName = groupName;
  }
}
