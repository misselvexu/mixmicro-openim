package com.acmedcare.tiffany.framework.remoting.jlib;

import java.util.concurrent.ThreadFactory;
import java.util.concurrent.atomic.AtomicLong;

class DefaultThreadFactory implements ThreadFactory {
  private final AtomicLong threadIndex = new AtomicLong(0);
  private final String threadNamePrefix;

  public DefaultThreadFactory(final String threadNamePrefix) {
    this.threadNamePrefix = threadNamePrefix;
  }

  @Override
  public Thread newThread(Runnable r) {
    return new Thread(r, threadNamePrefix + this.threadIndex.incrementAndGet());
  }
}
