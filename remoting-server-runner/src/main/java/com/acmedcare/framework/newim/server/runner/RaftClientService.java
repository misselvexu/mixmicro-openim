package com.acmedcare.framework.newim.server.runner;

import com.acmedcare.framework.boot.snowflake.Snowflake;
import io.atomix.AtomixClient;
import io.atomix.variables.DistributedLong;
import io.atomix.variables.DistributedValue;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.atomic.AtomicBoolean;
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
  private static final String ID_INITED_KEY = "mq_message_id_inited_key";
  private static volatile AtomicBoolean inited = new AtomicBoolean(false);
  private final AtomixClient atomixClient;
  private final Snowflake snowflake;

  @Autowired
  public RaftClientService(AtomixClient atomixClient, Snowflake snowflake) {
    this.atomixClient = atomixClient;
    this.snowflake = snowflake;
  }

  private boolean inited() {
    try {
      DistributedValue<Object> value = atomixClient.getValue(ID_INITED_KEY).get();
      if (value == null) {
        return false;
      } else {
        return Boolean.parseBoolean(value.toString());
      }
    } catch (Exception e) {
      return false;
    }
  }

  /**
   * 获取下一组消息递增编号
   *
   * @return 编号
   * @throws ExecutionException exception
   * @throws InterruptedException exception
   */
  public long nextUniformMessageId() throws ExecutionException, InterruptedException {

    if (inited.compareAndSet(false, true)) {
      if (!inited()) {
        atomixClient.getLong(UNIFORM_MESSAGE_ID_KEY).get().set(snowflake.nextId()).join();
        atomixClient.getValue(ID_INITED_KEY).get().set(true);
      }
    }

    DistributedLong distributedLong = atomixClient.getLong(UNIFORM_MESSAGE_ID_KEY).get();
    return atomixClient.getLong(UNIFORM_MESSAGE_ID_KEY).get().incrementAndGet().get();
  }
}
