package com.acmedcare.framework.newim.server.endpoint;

import static com.acmedcare.framework.newim.server.ClusterLogger.wssServerLog;

import com.acmedcare.framework.constants.defined.AuthConstants.AuthHeaders;
import com.acmedcare.framework.kits.thread.DefaultThreadFactory;
import com.acmedcare.framework.kits.thread.ThreadKit;
import com.acmedcare.framework.newim.server.core.IMSession;
import com.acmedcare.framework.newim.server.exception.UnauthorizedException;
import com.acmedcare.framework.newim.server.service.RemotingAuthService;
import com.acmedcare.tiffany.framework.remoting.common.Pair;
import com.google.common.collect.Maps;
import io.netty.handler.codec.http.HttpHeaders;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.ThreadPoolExecutor.CallerRunsPolicy;
import java.util.concurrent.TimeUnit;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * WebSocket & Socket Message Adapter
 *
 * @author <a href="mailto:iskp.me@gmail.com">Elve.Xu</a>
 * @version ${project.version} - 08/11/2018.
 */
public abstract class WssAdapter implements InitializingBean, DisposableBean {

  /**
   * Wss Client Type
   *
   * <p>//TODO 多客户端类型分布处理
   *
   * <p>
   */
  protected static final String WSS_TYPE = "wssClientType";

  private static Map<Integer, Pair<WssMessageRequestProcessor, ExecutorService>> processors =
      Maps.newConcurrentMap();

  private static Pair<WssMessageRequestProcessor, ExecutorService> defaultProcessor;
  @Autowired protected WssSessionContext wssSessionContext;
  @Autowired protected RemotingAuthService remotingAuthService;
  @Autowired protected IMSession imSession;
  private ExecutorService publicExecutorService =
      new ThreadPoolExecutor(
          8,
          32,
          5000,
          TimeUnit.MICROSECONDS,
          new LinkedBlockingQueue<>(64),
          new DefaultThreadFactory("wss-schedule-public-executor"),
          new CallerRunsPolicy());

  @Override
  public void afterPropertiesSet() throws Exception {}

  public void registerDefaultProcessor(
      WssMessageRequestProcessor processor, ExecutorService executorService) {
    defaultProcessor = new Pair<>(processor, executorService);
  }

  /**
   * Register Processor
   *
   * @param bizCode biz code
   * @param processor processor
   * @param executorService executor
   */
  public void registerProcessor(
      int bizCode, WssMessageRequestProcessor processor, ExecutorService executorService) {
    processors.put(
        bizCode,
        new Pair<>(processor, executorService == null ? publicExecutorService : executorService));
  }

  public Pair<WssMessageRequestProcessor, ExecutorService> getProcessor(int bizCode) {
    if (processors.containsKey(bizCode)) {
      Pair<WssMessageRequestProcessor, ExecutorService> pair = processors.get(bizCode);
      if (pair.getObject2() == null) {
        pair.setObject2(publicExecutorService);
      }
      return pair;
    } else {
      return defaultProcessor;
    }
  }

  /**
   * Valid Auth
   *
   * @param headers request header instance of {@link HttpHeaders}
   * @return true/false
   */
  protected boolean validateAuth(HttpHeaders headers) {
    try {
      String token = parseWssHeaderToken(headers);
      wssServerLog.info("[WSS] Wss Client With Token: {}", token);
      return remotingAuthService.auth(token);
    } catch (Exception e) {
      throw new UnauthorizedException("WebSocket请求授权校验失败");
    }
  }

  protected boolean validateAuth(String token) {
    try {
      wssServerLog.info("[WSS] Wss Client With Token: {}", token);
      return remotingAuthService.auth(token);
    } catch (Exception e) {
      throw new UnauthorizedException("WebSocket请求授权校验失败");
    }
  }

  /**
   * Parse Web Socket Header Token
   *
   * @param headers request header instance of {@link HttpHeaders}
   * @return token value
   */
  @Deprecated
  protected String parseWssHeaderToken(HttpHeaders headers) {
    if (headers != null) {
      if (headers.contains(AuthHeaders.AUTHORIZATION_TOKEN)) {
        return headers.get(AuthHeaders.AUTHORIZATION_TOKEN);
      }
      if (headers.contains(AuthHeaders.ACCESS_TOKEN)) {
        return headers.get(AuthHeaders.ACCESS_TOKEN);
      }
    }
    throw new UnauthorizedException("WebSocket请求链接为包含授权头信息参数,[AccessToken,Authorization]");
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
