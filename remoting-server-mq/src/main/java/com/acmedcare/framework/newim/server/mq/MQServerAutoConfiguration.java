package com.acmedcare.framework.newim.server.mq;

import com.acmedcare.framework.newim.server.mq.service.MQService;
import com.acmedcare.framework.newim.storage.api.TopicRepository;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;

/**
 * MQ Server Properties Auto Configuration
 *
 * @author <a href="mailto:iskp.me@gmail.com">Elve.Xu</a>
 * @version ${project.version} - 2018-12-13.
 */
@EnableConfigurationProperties(MQServerProperties.class)
public class MQServerAutoConfiguration {

  @Bean
  @Primary
  private MQService mqService(TopicRepository topicRepository) {
    return new MQService(topicRepository);
  }
}
