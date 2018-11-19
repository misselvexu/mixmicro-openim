package com.acmedcare.framework.newim.server.endpoint.schedule;

import static com.acmedcare.framework.newim.server.ClusterLogger.wssServerLog;

import com.acmedcare.framework.aorp.beans.Principal;
import com.acmedcare.framework.boot.web.socket.processor.WssSession;
import com.acmedcare.framework.kits.thread.DefaultThreadFactory;
import com.acmedcare.framework.kits.thread.ThreadKit;
import com.acmedcare.framework.newim.server.endpoint.WssMessageRequestProcessor;
import com.acmedcare.framework.newim.server.endpoint.WssSessionContext;
import com.google.common.collect.Maps;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.ThreadPoolExecutor.CallerRunsPolicy;
import java.util.concurrent.TimeUnit;
import javafx.util.Pair;
import org.springframework.beans.factory.DisposableBean;

/**
 * Schedule System Session Context
 *
 * @author <a href="mailto:iskp.me@gmail.com">Elve.Xu</a>
 * @version ${project.version} - 19/11/2018.
 */
public class ScheduleSysContext extends WssSessionContext implements DisposableBean {

  /** 保存调度站点的登录情况 */
  private static Map<ScheduleWssClientInstance, ScheduleWssClientAccountInstance>
      remotingWssScheduleInstances = Maps.newConcurrentMap();

  private static Map<Integer, Pair<WssMessageRequestProcessor, ExecutorService>> processors =
      Maps.newConcurrentMap();

  private ExecutorService publicExecutorService =
      new ThreadPoolExecutor(
          8,
          32,
          5000,
          TimeUnit.MICROSECONDS,
          new LinkedBlockingQueue<>(64),
          new DefaultThreadFactory("wss-schedule-public-executor"),
          new CallerRunsPolicy());

  /**
   * Register Processor
   *
   * @param bizCode biz code
   * @param processor processor
   * @param executorService executor
   */
  void registerProcessor(
      int bizCode, WssMessageRequestProcessor processor, ExecutorService executorService) {
    processors.put(
        bizCode,
        new Pair<>(processor, executorService == null ? publicExecutorService : executorService));
  }

  /**
   * Register Login-ed Wss Client
   *
   * @param principal principal detail
   * @param session session channel
   */
  @Override
  public void registerWssClient(Principal principal, WssSession session) {
    super.registerWssClient(principal, session);

    // schedule sys register wss client

  }

  @Override
  public void revokeWssClient(WssSession session) {
    super.revokeWssClient(session);

    // schedule sys revoke wss client

  }

  /**
   * Invoked by a BeanFactory on destruction of a singleton.
   *
   * @throws Exception in case of shutdown errors. Exceptions will get logged but not rethrown to
   *     allow other beans to release their resources too.
   */
  @Override
  public void destroy() throws Exception {
    ThreadKit.gracefulShutdown(publicExecutorService, 10, 10, TimeUnit.SECONDS);
    wssServerLog.info("[WSS] shutdown schedule wss process executor.");
  }
}
