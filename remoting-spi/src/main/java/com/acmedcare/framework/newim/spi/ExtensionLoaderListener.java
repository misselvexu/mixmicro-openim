package com.acmedcare.framework.newim.spi;

/**
 * 当扩展点加载时，可以做一些事情，例如解析code，初始化等动作
 *
 * @author <a href="mailto:iskp.me@gmail.com">Elve.Xu</a>
 */
public interface ExtensionLoaderListener<T> {

  /**
   * 当扩展点加载时，触发的事件
   *
   * @param extensionClass 扩展点类对象
   */
  void onLoad(ExtensionClass<T> extensionClass);
}
