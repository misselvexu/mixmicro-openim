package com.acmedcare.tiffany.framework.remoting.jlib.biz;

import java.util.concurrent.TimeUnit;

/**
 * Message Constants
 *
 * @author <a href="mailto:iskp.me@gmail.com">Elve.Xu</a>
 * @version ${project.version} - 21/11/2018.
 */
public interface MessageConstants {

  /** 最大重试次数,系统默认为:3次 */
  int DEFAULT_QOS_MAX_RETRY_TIMES = 3;

  /** 重试间隔5000 ms */
  long DEFAULT_RETRY_PERIOD = 5000;

  /** 默认执行时间单位 */
  TimeUnit DEFAULT_EXECUTE_TIME_UNIT = TimeUnit.MILLISECONDS;
}
