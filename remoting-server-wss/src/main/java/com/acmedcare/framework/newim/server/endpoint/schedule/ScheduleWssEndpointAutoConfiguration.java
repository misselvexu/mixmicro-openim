package com.acmedcare.framework.newim.server.endpoint.schedule;

import static com.acmedcare.framework.newim.protocol.Command.WebSocketClusterCommand.WS_HEARTBEAT;
import static com.acmedcare.framework.newim.protocol.Command.WebSocketClusterCommand.WS_REGISTER;
import static com.acmedcare.framework.newim.protocol.Command.WebSocketClusterCommand.WS_SHUTDOWN;
import static com.acmedcare.framework.newim.server.ClusterLogger.wssServerLog;

import com.acmedcare.framework.newim.server.core.IMSession;
import com.acmedcare.framework.newim.server.endpoint.WssEndpointAutoConfiguration;
import com.acmedcare.framework.newim.server.endpoint.schedule.processor.HeartbeatProcessor;
import com.acmedcare.framework.newim.server.endpoint.schedule.processor.PullOnlineSubOrgsRequestProcessor;
import com.acmedcare.framework.newim.server.endpoint.schedule.processor.PushOrderProcessor;
import com.acmedcare.framework.newim.server.endpoint.schedule.processor.RegisterProcessor;
import com.acmedcare.framework.newim.server.endpoint.schedule.processor.ShutdownProcessor;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.beans.factory.BeanFactoryAware;
import org.springframework.beans.factory.BeanInitializationException;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;

/**
 * Schedule WssEndpoint AutoConfiguration
 *
 * @author <a href="mailto:iskp.me@gmail.com">Elve.Xu</a>
 * @version ${project.version} - 19/11/2018.
 */
@Configuration
@Order(Ordered.HIGHEST_PRECEDENCE)
public class ScheduleWssEndpointAutoConfiguration
    implements WssEndpointAutoConfiguration, BeanFactoryAware {

  private BeanFactory beanFactory;

  @Bean
  @Primary
  public ScheduleSysContext scheduleSysContext(IMSession imSession) {
    return new ScheduleSysContext(imSession);
  }

  /**
   * Invoked by a BeanFactory on destruction of a singleton.
   *
   * @throws Exception in case of shutdown errors. Exceptions will get logged but not rethrown to
   *     allow other beans to release their resources too.
   */
  @Override
  public void destroy() throws Exception {}

  /**
   * Invoked by a BeanFactory after it has set all bean properties supplied (and satisfied
   * BeanFactoryAware and ApplicationContextAware).
   *
   * <p>This method allows the bean instance to perform initialization only possible when all bean
   * properties have been set and to throw an exception in the event of misconfiguration.
   *
   * @throws Exception in the event of misconfiguration (such as failure to set an essential
   *     property) or if initialization fails.
   */
  @Override
  public void afterPropertiesSet() throws Exception {
    ScheduleSysContext context = this.beanFactory.getBean(ScheduleSysContext.class);
    ScheduleSysWssEndpoint endpoint = this.beanFactory.getBean(ScheduleSysWssEndpoint.class);
    // 注册处理器
    endpoint.registerProcessor(WS_REGISTER, new RegisterProcessor(context), null);
    // 注销处理器
    endpoint.registerProcessor(WS_SHUTDOWN, new ShutdownProcessor(context), null);
    // 心跳处理器
    endpoint.registerProcessor(WS_HEARTBEAT, new HeartbeatProcessor(context), null);
    // 拉取在线子机构列表
    endpoint.registerProcessor(
        ScheduleCommand.PULL_ONLINE_SUB_ORGS.getBizCode(),
        new PullOnlineSubOrgsRequestProcessor(context),
        null);

    // 推送订单
    endpoint.registerProcessor(
        ScheduleCommand.PUSH_ORDER.getBizCode(), new PushOrderProcessor(context), null);

    wssServerLog.info("[WSS] wss message processors register-ed.");
  }

  /**
   * Callback that supplies the owning factory to a bean instance.
   *
   * <p>Invoked after the population of normal bean properties but before an initialization callback
   * such as {@link InitializingBean#afterPropertiesSet()} or a custom init-method.
   *
   * @param beanFactory owning BeanFactory (never {@code null}). The bean can immediately call
   *     methods on the factory.
   * @throws BeansException in case of initialization errors
   * @see BeanInitializationException
   */
  @Override
  public void setBeanFactory(BeanFactory beanFactory) throws BeansException {
    this.beanFactory = beanFactory;
  }
}
