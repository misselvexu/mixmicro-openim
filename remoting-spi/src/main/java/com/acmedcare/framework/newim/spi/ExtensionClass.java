package com.acmedcare.framework.newim.spi;

import com.acmedcare.framework.newim.spi.exception.SpiException;
import com.acmedcare.framework.newim.spi.util.ClassUtils;

/**
 * Extension Class
 *
 * @author <a href="mailto:iskp.me@gmail.com">Elve.Xu</a>
 * @version ${project.version} - 2018-12-03.
 */
public class ExtensionClass<T> {

  /** 扩展接口实现类名 */
  protected final Class<? extends T> clazz;

  /** 扩展别名,不是provider uniqueId */
  protected final String alias;

  /** 是否单例 */
  protected boolean singleton;

  /** 服务端实例对象（只在是单例的时候保留） */
  private transient volatile T instance;

  public ExtensionClass(Class<? extends T> clazz, String alias) {
    this.clazz = clazz;
    this.alias = alias;
  }

  public boolean isSingleton() {
    return singleton;
  }

  public void setSingleton(boolean singleton) {
    this.singleton = singleton;
  }

  public Class<? extends T> getClazz() {
    return clazz;
  }

  public String getAlias() {
    return alias;
  }

  /**
   * 得到服务端实例对象，如果是单例则返回单例对象，如果不是则返回新创建的实例对象
   *
   * @return 扩展点对象实例
   */
  public T getExtInstance() {
    return getExtInstance(null, null);
  }

  /**
   * 得到服务端实例对象，如果是单例则返回单例对象，如果不是则返回新创建的实例对象
   *
   * @param argTypes 构造函数参数类型
   * @param args 构造函数参数值
   * @return 扩展点对象实例 ext instance
   */
  public T getExtInstance(Class[] argTypes, Object[] args) {
    if (clazz != null) {
      try {
        if (singleton) { // 如果是单例
          if (instance == null) {
            synchronized (this) {
              if (instance == null) {
                instance = ClassUtils.newInstanceWithArgs(clazz, argTypes, args);
              }
            }
          }
          return instance; // 保留单例
        } else {
          return ClassUtils.newInstanceWithArgs(clazz, argTypes, args);
        }
      } catch (Exception e) {
        throw new SpiException("create " + clazz.getCanonicalName() + " instance error", e);
      }
    }
    throw new SpiException("Class of ExtensionClass is null");
  }

  @Override
  public String toString() {
    String sb =
        "ExtensionClass{"
            + "clazz="
            + clazz
            + ", alias='"
            + alias
            + '\''
            + ", singleton="
            + singleton
            + ", instance="
            + instance
            + '}';
    return sb;
  }
}
