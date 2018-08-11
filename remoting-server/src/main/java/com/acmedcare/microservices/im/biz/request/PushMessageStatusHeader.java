package com.acmedcare.microservices.im.biz.request;

import lombok.Getter;
import lombok.Setter;
import org.apache.commons.lang3.StringUtils;

/**
 * Push Message Status
 *
 * @author Elve.Xu [iskp.me<at>gmail.com]
 * @version v1.0 - 09/08/2018.
 */
@Getter
@Setter
public class PushMessageStatusHeader extends BaseHeader {

  /**
   * 最新消息 ID
   *
   * <pre>根据客户端群组最后一条消息的编号,反推,界限不是显示,而是全部</pre>
   */
  private long leastMessageId;

  /** 此处 ID 标识单聊(发送人) / 群组的标识 */
  private String sender;

  private String pmt;

  /**
   * 根据PMT的类型处理对应的消息已读
   *
   * @return PMT
   */
  public PMT decodePMTValue() {
    if (StringUtils.isNoneBlank(pmt)) {
      return PMT.valueOf(pmt);
    }
    return null;
  }

  public enum PMT {
    GROUP,
    SINGLE
  }
}
