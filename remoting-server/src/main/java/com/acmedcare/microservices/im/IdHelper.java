package com.acmedcare.microservices.im;

import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnResource;
import org.springframework.context.annotation.Configuration;

@ConditionalOnResource(resources = {"ext.snowflake.datacenterId", "ext.snowflake.machineId"})
@Configuration
public class IdHelper implements InitializingBean {

  private static final long START_STMP = 1480166465630L;
  private static final long SEQUENCE_BIT = 10;

  private static final long MACHINE_BIT = 3;
  private static final long DATACENTER_BIT = 3;

  private static final long MAX_DATACENTER_NUM = -1L ^ (-1L << DATACENTER_BIT);

  private static final long MAX_MACHINE_NUM = -1L ^ (-1L << MACHINE_BIT);
  private static final long MAX_SEQUENCE = -1L ^ (-1L << SEQUENCE_BIT);

  private static final long MACHINE_LEFT = SEQUENCE_BIT;

  private static final long DATACENTER_LEFT = SEQUENCE_BIT + MACHINE_BIT;
  private static final long TIMESTMP_LEFT = DATACENTER_LEFT + DATACENTER_BIT;

  @Value("${ext.snowflake.datacenterId}")
  private Long datacenterId; // 数据中心

  @Value("${ext.snowflake.machineId}")
  private Long machineId; // 机器标识

  private long sequence = 0L; // 序列号
  private long lastStmp = -1L; // 上一次时间戳

  /**
   * 产生下一个ID
   *
   * @return value
   */
  public synchronized long nextId() {
    long currStmp = getNewstmp();
    if (currStmp < lastStmp) {
      throw new RuntimeException("Clock moved backwards.  Refusing to generate sender");
    }

    if (currStmp == lastStmp) {
      // 相同毫秒内，序列号自增
      sequence = (sequence + 1) & MAX_SEQUENCE;
      // 同一毫秒的序列数已经达到最大
      if (sequence == 0L) {
        currStmp = getNextMill();
      }
    } else {
      // 不同毫秒内，序列号置为0
      sequence = 0L;
    }

    lastStmp = currStmp;

    long temp =
        (currStmp - START_STMP) << TIMESTMP_LEFT // 时间戳部分
            | datacenterId << DATACENTER_LEFT // 数据中心部分
            | machineId << MACHINE_LEFT // 机器标识部分
            | sequence; // 序列号部分

    return temp;
  }

  private long getNextMill() {
    long mill = getNewstmp();
    while (mill <= lastStmp) {
      mill = getNewstmp();
    }
    return mill;
  }

  private long getNewstmp() {
    return System.currentTimeMillis();
  }

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
    if (datacenterId > MAX_DATACENTER_NUM || datacenterId < 0) {
      throw new IllegalArgumentException(
          "datacenterId can't be greater than MAX_DATACENTER_NUM or less than 0");
    }
    if (machineId > MAX_MACHINE_NUM || machineId < 0) {
      throw new IllegalArgumentException(
          "machineId can't be greater than MAX_MACHINE_NUM or less than 0");
    }
  }
}
