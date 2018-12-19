package com.acmedcare.framework.newim.server.runner;

import io.atomix.AtomixClient;
import io.atomix.variables.DistributedLong;
import java.util.concurrent.ExecutionException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * RaftClientService
 *
 * @author <a href="mailto:iskp.me@gmail.com">Elve.Xu</a>
 * @version ${project.version} - 2018-12-19.
 */
@Component
public class RaftClientService {

  private static final String UNIFORM_MESSAGE_ID_KEY = "mq_message_id_key";
  private final AtomixClient atomixClient;

  @Autowired
  public RaftClientService(AtomixClient atomixClient) {
    this.atomixClient = atomixClient;
  }

  /**
   * 获取下一组消息递增编号
   *
   * @return 编号
   * @throws ExecutionException exception
   * @throws InterruptedException exception
   */
  public long nextUniformMessageId() throws ExecutionException, InterruptedException {
    DistributedLong distributedLong = atomixClient.getLong(UNIFORM_MESSAGE_ID_KEY).get();
    distributedLong.set(1L).join();
    return distributedLong.incrementAndGet().get();
  }
}
