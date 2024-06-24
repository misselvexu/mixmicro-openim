package com.acmedcare.framework.newim.spi;

import com.acmedcare.framework.newim.spi.exception.SpiException;
import com.acmedcare.framework.newim.spi.util.ClassLoaderUtils;
import com.acmedcare.framework.newim.spi.util.ClassTypeUtils;
import com.acmedcare.framework.newim.spi.util.ClassUtils;
import com.acmedcare.framework.newim.spi.util.ExceptionUtils;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.lang.reflect.Modifier;
import java.net.URL;
import java.util.Enumeration;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Extension Loader
 *
 * @author <a href="mailto:iskp.me@gmail.com">Elve.Xu</a>
 * @version ${project.version} - 2018-12-03.
 */
public class ExtensionLoader<T> {

  /** 扩展点加载的路径 */
  public static final String EXTENSION_LOAD_PATH = "META-INF/services/servers/";

  private static final Logger LOGGER = LoggerFactory.getLogger(ExtensionLoader.class);
  /** 当前加载的接口类名 */
  protected final Class<T> interfaceClass;

  /** 接口名字 */
  protected final String interfaceName;

  /** 加载监听器 */
  protected final ExtensionLoaderListener<T> listener;

  /** 扩展点是否单例 */
  protected final Extensible extensible;

  /** 全部的加载的实现类 {"alias":ExtensionClass} */
  protected final ConcurrentMap<String, ExtensionClass<T>> all;

  /** 如果是单例，那么factory不为空 */
  protected final ConcurrentMap<String, T> factory;

  /**
   * Default Construct
   *
   * @param interfaceClass interface class
   * @param listener listener for loader
   */
  public ExtensionLoader(Class<T> interfaceClass, ExtensionLoaderListener<T> listener) {

    // 接口为空，既不是接口，也不是抽象类
    if (interfaceClass == null
        || !(interfaceClass.isInterface() || Modifier.isAbstract(interfaceClass.getModifiers()))) {
      throw new IllegalArgumentException("Extensible class must be interface or abstract class!");
    }

    this.interfaceClass = interfaceClass;
    this.interfaceName = ClassTypeUtils.getTypeStr(interfaceClass);
    this.listener = listener;

    Extensible extensible = interfaceClass.getAnnotation(Extensible.class);
    if (extensible == null) {
      throw new IllegalArgumentException(
          "Error when load extensible interface " + interfaceName + ", must add annotation @Extensible.");
    } else {
      this.extensible = extensible;
    }

    this.factory = extensible.singleton() ? new ConcurrentHashMap<String, T>() : null;
    this.all = new ConcurrentHashMap<>();
    // load extension from local classpath.
    loadFromFile(EXTENSION_LOAD_PATH);
  }

  /**
   * 得到当前ClassLoader，先找线程池的，找不到就找中间件所在的ClassLoader
   *
   * @return ClassLoader
   */
  public static ClassLoader getCurrentClassLoader() {
    ClassLoader cl = Thread.currentThread().getContextClassLoader();
    if (cl == null) {
      cl = ExtensionLoader.class.getClassLoader();
    }
    return cl == null ? ClassLoader.getSystemClassLoader() : cl;
  }

  /** @param path path必须以/结尾 */
  protected synchronized void loadFromFile(String path) {
    if (LOGGER.isDebugEnabled()) {
      LOGGER.debug("Loading extension of extensible {} from path: {}", interfaceName, path);
    }
    // 默认如果不指定文件名字，就是接口名
    String file =
        (extensible.file() == null || extensible.file().trim().length() == 0)
            ? interfaceName
            : extensible.file().trim();
    String fullFileName = path + file;

    try {
      ClassLoader classLoader = ClassLoaderUtils.getClassLoader(getClass());

      // load extension from class loader
      loadFromClassLoader(classLoader, fullFileName);
    } catch (Throwable t) {
      if (LOGGER.isErrorEnabled()) {
        LOGGER.error("Failed to load extension of extensible " + interfaceName + " from path:" + fullFileName, t);
      }
    }
  }

  protected void loadFromClassLoader(ClassLoader classLoader, String fullFileName)
      throws Throwable {
    Enumeration<URL> urls =
        classLoader != null
            ? classLoader.getResources(fullFileName)
            : ClassLoader.getSystemResources(fullFileName);
    // 可能存在多个文件。
    if (urls != null) {
      while (urls.hasMoreElements()) {
        // 读取一个文件
        URL url = urls.nextElement();
        if (LOGGER.isDebugEnabled()) {
          LOGGER.debug(
              "Loading extension of extensible {} from classloader: {} and file: {}",
              interfaceName,
              classLoader,
              url);
        }
        BufferedReader reader = null;
        try {
          reader = new BufferedReader(new InputStreamReader(url.openStream(), "UTF-8"));
          String line;
          while ((line = reader.readLine()) != null) {
            readLine(url, line);
          }
        } catch (Throwable t) {
          if (LOGGER.isWarnEnabled()) {
            LOGGER.warn(
                "Failed to load extension of extensible " + interfaceName + " from classloader: " + classLoader + " and file:" + url, t);
          }
        } finally {
          if (reader != null) {
            reader.close();
          }
        }
      }
    }
  }

  private String[] parseAliasAndClassName(String line) {
    if (line == null || line.trim().length() == 0) {
      return null;
    }
    line = line.trim();
    int i0 = line.indexOf('#');
    if (i0 == 0 || line.length() == 0) {
      return null; // 当前行是注释 或者 空
    }
    if (i0 > 0) {
      line = line.substring(0, i0).trim();
    }

    String alias = null;
    String className;
    int i = line.indexOf('=');
    if (i > 0) {
      alias = line.substring(0, i).trim(); // 以代码里的为准
      className = line.substring(i + 1).trim();
    } else {
      className = line;
    }
    if (className.length() == 0) {
      return null;
    }
    return new String[] {alias, className};
  }

  protected void readLine(URL url, String line) {
    String[] aliasAndClassName = parseAliasAndClassName(line);
    if (aliasAndClassName == null || aliasAndClassName.length != 2) {
      return;
    }
    String alias = aliasAndClassName[0];
    String className = aliasAndClassName[1];
    // 读取配置的实现类
    Class tmp;
    try {
      tmp = ClassUtils.forName(className, false);
    } catch (Throwable e) {
      if (LOGGER.isWarnEnabled()) {
        LOGGER.warn(
            "Extension {} of extensible {} is disabled, cause by: {}", className, interfaceName, ExceptionUtils.toShortString(e, 2));
      }
      if (LOGGER.isDebugEnabled()) {
        LOGGER.debug("Extension " + className + " of extensible " + interfaceName + " is disabled.", e);
      }
      return;
    }
    if (!interfaceClass.isAssignableFrom(tmp)) {
      throw new IllegalArgumentException(
          "Error when load extension of extensible "
              + interfaceName
              + " from file:"
              + url
              + ", "
              + className
              + " is not subtype of interface.");
    }
    Class<? extends T> implClass = (Class<? extends T>) tmp;

    // 检查是否有可扩展标识
    Extension extension = implClass.getAnnotation(Extension.class);
    if (extension == null) {
      throw new IllegalArgumentException(
          "Error when load extension of extensible "
              + interfaceName
              + " from file:"
              + url
              + ", "
              + className
              + " must add annotation @Extension.");
    } else {
      String aliasInCode = extension.value();
      if (aliasInCode == null || aliasInCode.trim().length() == 0) {
        // 扩展实现类未配置@Extension 标签
        throw new IllegalArgumentException(
            "Error when load extension of extensible "
                + interfaceClass
                + " from file:"
                + url
                + ", "
                + className
                + "'s alias of @Extension is blank");
      }
      if (alias == null) {
        // spi文件里没配置，用代码里的
        alias = aliasInCode;
      } else {
        // spi文件里配置的和代码里的不一致
        if (!aliasInCode.equals(alias)) {
          throw new IllegalArgumentException(
              "Error when load extension of extensible "
                  + interfaceName
                  + " from file:"
                  + url
                  + ", aliases of "
                  + className
                  + " are "
                  + "not equal between "
                  + aliasInCode
                  + "(code) and "
                  + alias
                  + "(file).");
        }
      }
    }
    // 不可以是default和*
    if ("default".equals(alias) || "*".equals(alias)) {
      throw new IllegalArgumentException(
          "Error when load extension of extensible "
              + interfaceName
              + " from file:"
              + url
              + ", alias of @Extension must not \"default\" and \"*\" at "
              + className);
    }
    // 检查是否有存在同名的
    ExtensionClass<T> extensionClass = buildClass(extension, implClass, alias);

    loadSuccess(alias, extensionClass);
  }

  private ExtensionClass<T> buildClass(
      Extension extension, Class<? extends T> implClass, String alias) {
    ExtensionClass<T> extensionClass = new ExtensionClass<>(implClass, alias);
    extensionClass.setSingleton(extensible.singleton());
    return extensionClass;
  }

  private void loadSuccess(String alias, ExtensionClass<T> extensionClass) {
    if (listener != null) {
      try {
        listener.onLoad(extensionClass); // 加载完毕，通知监听器
        all.put(alias, extensionClass);
      } catch (Exception e) {
        LOGGER.error(
            "Error when load extension of extensible " + interfaceClass + " with alias: " + alias,
            e);
      }
    } else {
      all.put(alias, extensionClass);
    }
  }

  /**
   * 返回全部扩展类
   *
   * @return 扩展类对象
   */
  public ConcurrentMap<String, ExtensionClass<T>> getAllExtensions() {
    return all;
  }

  /**
   * 根据服务别名查找扩展类
   *
   * @param alias 扩展别名
   * @return 扩展类对象
   */
  public ExtensionClass<T> getExtensionClass(String alias) {
    return all == null ? null : all.get(alias);
  }

  /**
   * 得到实例
   *
   * @param alias 别名
   * @return 扩展实例（已判断是否单例）
   */
  public T getExtension(String alias) {
    ExtensionClass<T> extensionClass = getExtensionClass(alias);
    if (extensionClass == null) {
      throw new SpiException(
          "Not found extension of " + interfaceName + " named: \"" + alias + "\"!");
    } else {
      if (extensible.singleton() && factory != null) {
        T t = factory.get(alias);
        if (t == null) {
          synchronized (this) {
            t = factory.get(alias);
            if (t == null) {
              t = extensionClass.getExtInstance();
              factory.put(alias, t);
            }
          }
        }
        return t;
      } else {
        return extensionClass.getExtInstance();
      }
    }
  }

  /**
   * 得到实例
   *
   * @param alias 别名
   * @param argTypes 扩展初始化需要的参数类型
   * @param args 扩展初始化需要的参数
   * @return 扩展实例（已判断是否单例）
   */
  public T getExtension(String alias, Class[] argTypes, Object[] args) {
    ExtensionClass<T> extensionClass = getExtensionClass(alias);
    if (extensionClass == null) {
      throw new SpiException(
          "Not found extension of " + interfaceName + " named: \"" + alias + "\"!");
    } else {
      if (extensible.singleton() && factory != null) {
        T t = factory.get(alias);
        if (t == null) {
          synchronized (this) {
            t = factory.get(alias);
            if (t == null) {
              t = extensionClass.getExtInstance(argTypes, args);
              factory.put(alias, t);
            }
          }
        }
        return t;
      } else {
        return extensionClass.getExtInstance(argTypes, args);
      }
    }
  }
}
