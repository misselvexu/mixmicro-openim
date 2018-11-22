package com.acmedcare.framework.newim.storage.api;

import com.acmedcare.framework.newim.Message;

/**
 * Message Repository Api
 *
 * @author <a href="mailto:iskp.me@gmail.com">Elve.Xu</a>
 * @version ${project.version} - 13/11/2018.
 */
public interface MessageRepository {

  /**
   * 保存信息
   *
   * @param message 信息
   * @return mid
   */
  long saveMessage(Message message);

  /**
   * 批量保存消息
   *
   * @param messages 消息列表
   * @return 保存成功的消息
   */
  Long[] batchSaveMessage(Message... messages);
}
