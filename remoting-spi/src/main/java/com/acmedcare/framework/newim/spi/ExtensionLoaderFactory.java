package com.acmedcare.framework.newim.spi;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

/**
 * Nas exts factory
 *
 * @author <a href="mailto:iskp.me@gmail.com">Elve.Xu</a>
 * @version ${project.version} - 2018-12-03.
 */
public class ExtensionLoaderFactory {

  /** All extension loader {Class : ExtensionLoader} */
  private static final ConcurrentMap<Class, ExtensionLoader> LOADER_MAP = new ConcurrentHashMap<>();

  private ExtensionLoaderFactory() {}

  /**
   * Get extension loader by extensible class with listener
   *
   * @param clazz Extensible class
   * @param listener Listener of ExtensionLoader
   * @param <T> Class
   * @return ExtensionLoader of this class
   */
  public static <T> ExtensionLoader<T> getExtensionLoader(
      Class<T> clazz, ExtensionLoaderListener<T> listener) {
    ExtensionLoader<T> loader = LOADER_MAP.get(clazz);
    if (loader == null) {
      synchronized (ExtensionLoaderFactory.class) {
        loader = LOADER_MAP.get(clazz);
        if (loader == null) {
          loader = new ExtensionLoader<T>(clazz, listener);
          LOADER_MAP.put(clazz, loader);
        }
      }
    }
    return loader;
  }

  /**
   * Get extension loader by extensible class without listener
   *
   * @param clazz Extensible class
   * @param <T> Class
   * @return ExtensionLoader of this class
   */
  public static <T> ExtensionLoader<T> getExtensionLoader(Class<T> clazz) {
    return getExtensionLoader(clazz, null);
  }
}
