package com.acmedcare.framework.remoting.mq.client.biz.bean;

import java.io.Serializable;

import lombok.Builder;
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

  /** 主题标识 */
  private String topicTag;

  /** 主题类型 */
  private String topicType;

  /** 主题描述 */
  private String topicDesc;

  /** 主题扩展信息 */
  private String topicExt;

  @Builder
  public Topic(
      Long topicId,
      String topicName,
      String topicTag,
      String topicType,
      String topicDesc,
      String topicExt) {
    this.topicId = topicId;
    this.topicName = topicName;
    this.topicTag = topicTag;
    this.topicType = topicType;
    this.topicDesc = topicDesc;
    this.topicExt = topicExt;
  }
}
