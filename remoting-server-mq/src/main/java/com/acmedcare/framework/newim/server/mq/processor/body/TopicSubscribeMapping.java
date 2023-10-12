package com.acmedcare.framework.newim.server.mq.processor.body;

import com.acmedcare.framework.newim.Topic;
import com.google.common.collect.Lists;
import java.io.Serializable;
import java.util.List;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * TopicSubscribeMapping
 *
 * @author <a href="mailto:iskp.me@gmail.com">Elve.Xu</a>
 * @version ${project.version} - 2018-12-24.
 */
@Getter
@Setter
@NoArgsConstructor
public class TopicSubscribeMapping implements Serializable {

  private static final long serialVersionUID = 8358460897070268953L;

  // Defined topic mappings 
  private List<TopicMapping> mappings;

  @Getter
  @Setter
  public static class TopicMapping extends Topic {

    private static final long serialVersionUID = 6574460621468394583L;

    /** subscribe passport ids */
    private List<String> subscribeIdsList = Lists.newArrayList();
  }
}
