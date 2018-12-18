package com.acmedcare.framework.remoting.mq.client.biz.bean;

import java.io.Serializable;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Topic
 *
 * @author <a href="mailto:iskp.me@gmail.com">Elve.Xu</a>
 * @version ${project.version} - 2018-12-18.
 */
@Getter
@Setter
@NoArgsConstructor
public class Topic implements Serializable {

  private static final long serialVersionUID = -3390344985542671632L;

  private Long topicId;

  /** 主题名称 */
  private String topicName;

  /** 主题描述 */
  private String topicDesc;

  /** 主题扩展信息 */
  private String topicExt;
}
