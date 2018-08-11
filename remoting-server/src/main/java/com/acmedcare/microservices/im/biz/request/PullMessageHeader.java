package com.acmedcare.microservices.im.biz.request;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Pull Message Header
 *
 * @author Elve.Xu [iskp.me<at>gmail.com]
 * @version v1.0 - 09/08/2018.
 */
@Getter
@Setter
@NoArgsConstructor
public class PullMessageHeader extends BaseHeader {

  private String sender;

  /**
   * InnerType 0默认单聊 ,1-群组
   *
   * <pre>
   *
   * </pre>
   */
  private int type;

  /**
   * 最新的消息 ID
   *
   * <pre></pre>
   */
  private long leastMessageId;

  private long limit;
}
