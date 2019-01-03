package com.acmedcare.framework.newim;

import com.acmedcare.framework.newim.storage.IMStorageCollections;
import java.io.Serializable;
import java.util.Objects;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.data.mongodb.core.index.CompoundIndex;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

/**
 * Topic
 *
 * @author <a href="mailto:iskp.me@gmail.com">Elve.Xu</a>
 * @version ${project.version} - 2018-12-18.
 */
@Getter
@Setter
@NoArgsConstructor
@Document(value = IMStorageCollections.TOPIC)
public class Topic implements Serializable {

  private static final long serialVersionUID = -3411517234330957094L;

  @Indexed(unique = true)
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

  /** 命名空间 */
  private String namespace = "MQ-DEFAULT";

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (!(o instanceof Topic)) {
      return false;
    }
    Topic topic = (Topic) o;
    return getTopicId().equals(topic.getTopicId()) && getTopicTag().equals(topic.getTopicTag());
  }

  @Override
  public int hashCode() {
    return Objects.hash(getTopicId(), getTopicTag());
  }

  @Override
  public String toString() {
    final StringBuffer sb = new StringBuffer("Topic{");
    sb.append("topicId=").append(topicId);
    sb.append(", topicName='").append(topicName).append('\'');
    sb.append(", topicTag='").append(topicTag).append('\'');
    sb.append(", topicType='").append(topicType).append('\'');
    sb.append(", topicDesc='").append(topicDesc).append('\'');
    sb.append(", topicExt='").append(topicExt).append('\'');
    sb.append('}');
    return sb.toString();
  }

  @Getter
  @Setter
  @NoArgsConstructor
  @Document(value = IMStorageCollections.TOPIC_SUBSCRIBE)
  @CompoundIndex(
      unique = true,
      name = "topicId_passport_id_and_namespace_index",
      def = "{'topicId': 1, 'passportId': -1, 'namespace': 1}")
  public static class TopicSubscribe implements Serializable {
    private String namespace;
    private Long topicId;
    private Long passportId;

    @Builder
    public TopicSubscribe(String namespace, Long topicId, Long passportId) {
      this.namespace = namespace;
      this.topicId = topicId;
      this.passportId = passportId;
    }
  }
}
