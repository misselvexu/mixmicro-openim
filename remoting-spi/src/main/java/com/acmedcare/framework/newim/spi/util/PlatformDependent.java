package com.acmedcare.framework.newim.spi.util;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Platform Dependent
 *
 * @author <a href="mailto:iskp.me@gmail.com">Elve.Xu</a>
 * @version ${project.version} - 2018-12-05.
 */
public final class PlatformDependent {

  private static final Logger LOGGER = LoggerFactory.getLogger(SystemPropertyUtil.class);

  /**
   * is android os system
   *
   * @return true/false
   */
  public static boolean isAndroid() {
    String vmName = SystemPropertyUtil.get("java.vm.name");
    boolean isAndroid = "Dalvik".equals(vmName);
    if (isAndroid) {
      LOGGER.debug("Platform: Android");
    }

    return isAndroid;
  }
}
