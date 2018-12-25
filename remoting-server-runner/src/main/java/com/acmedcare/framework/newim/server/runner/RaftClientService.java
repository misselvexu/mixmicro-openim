package com.acmedcare.framework.newim.server.runner;

import com.acmedcare.framework.boot.snowflake.Snowflake;
import com.acmedcare.framework.newim.server.IdService;
import io.atomix.AtomixClient;
import io.atomix.variables.DistributedLong;
import io.atomix.variables.DistributedValue;
import java.util.concurrent.atomic.AtomicBoolean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

/**
 * RaftClientService
 *
 * @author <a href="mailto:iskp.me@gmail.com">Elve.Xu</a>
 * @version ${project.version} - 2018-12-19.
 */
@Component
@ConditionalOnClass({Snowflake.class, AtomixClient.class})
@Order(Ordered.HIGHEST_PRECEDENCE)
public class RaftClientService implements IdService {

  private static final String UNIFORM_MESSAGE_ID_KEY = "mq_message_id_key";
  private static final String ID_INITED_KEY = "mq_message_id_inited_key";
  private static final String ID_INITED_LOCK_KEY = "mq_message_id_inited_lock_key";
  private static volatile AtomicBoolean inited = new AtomicBoolean(false);
  private final AtomixClient atomixClient;
  private final Snowflake snowflake;

  @Autowired
  public RaftClientService(AtomixClient atomixClient, Snowflake snowflake) {
    this.atomixClient = atomixClient;
    this.snowflake = snowflake;
  }

  private void doInit() {

    DistributedValue<Boolean> booleanDistributedValue =
        atomixClient.<Boolean>getValue(ID_INITED_KEY).join();
    if (booleanDistributedValue.compareAndSet(false, true).join()) {
      try {
        if (inited.compareAndSet(false, true)) {
          atomixClient.getLong(UNIFORM_MESSAGE_ID_KEY).get().set(snowflake.nextId()).join();
          atomixClient.getValue(ID_INITED_KEY).get().set(true);
        }
      } catch (Exception e) {
        e.printStackTrace();
      }
    }
  }

  /**
   * 获取下一组消息递增编号
   *
   * @return 编号
   * @throws Exception exception
   */
  @Override
  public long nextId() throws Exception {
    if (!inited.get()) {
      doInit();
    }
    DistributedLong distributedLong = atomixClient.getLong(UNIFORM_MESSAGE_ID_KEY).get();
    return atomixClient.getLong(UNIFORM_MESSAGE_ID_KEY).get().incrementAndGet().get();
  }
}
