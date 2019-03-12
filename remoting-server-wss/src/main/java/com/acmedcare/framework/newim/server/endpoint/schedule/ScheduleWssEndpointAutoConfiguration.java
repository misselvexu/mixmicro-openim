package com.acmedcare.framework.newim.server.endpoint.schedule;

import com.acmedcare.framework.newim.server.ClusterServerAutoBootstrap;
import com.acmedcare.framework.newim.server.core.IMSession;
import com.acmedcare.framework.newim.storage.api.GroupRepository;
import org.springframework.beans.BeansException;
import org.springframework.boot.autoconfigure.AutoConfigureAfter;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Schedule WssEndpoint AutoConfiguration
 *
 * @author <a href="mailto:iskp.me@gmail.com">Elve.Xu</a>
 * @version ${project.version} - 19/11/2018.
 */
@Configuration
@AutoConfigureAfter(ClusterServerAutoBootstrap.class)
public class ScheduleWssEndpointAutoConfiguration implements ApplicationContextAware {

  private ApplicationContext applicationContext;

  @Bean
  public ScheduleSysContext scheduleSysContext(
      IMSession imSession, GroupRepository groupRepository) {
    return new ScheduleSysContext(imSession, groupRepository);
  }

  @Override
  public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
    this.applicationContext = applicationContext;
  }
}
