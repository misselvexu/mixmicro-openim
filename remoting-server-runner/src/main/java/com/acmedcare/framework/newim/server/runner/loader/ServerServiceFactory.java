package com.acmedcare.framework.newim.server.runner.loader;

import com.acmedcare.framework.newim.server.Server;
import com.acmedcare.framework.newim.spi.ExtensionClass;
import com.acmedcare.framework.newim.spi.ExtensionLoader;
import com.acmedcare.framework.newim.spi.ExtensionLoaderFactory;
import com.acmedcare.framework.newim.spi.ExtensionLoaderListener;
import java.util.Collection;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import lombok.Getter;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.beans.factory.annotation.AutowiredAnnotationBeanPostProcessor;

/**
 * Server Service Factory
 *
 * @author <a href="mailto:iskp.me@gmail.com">Elve.Xu</a>
 * @version ${project.version} - 2018-12-12.
 */
public class ServerServiceFactory {

  private static final Map<String, Server> SERVER_MAP = new ConcurrentHashMap<>();

  @Getter private static ExtensionLoader<Server> serverExtensionLoader;

  private static BeanFactory beanFactory;

  public static void refresh(BeanFactory beanFactory) {
    ServerServiceFactory.beanFactory = beanFactory;
    serverExtensionLoader = buildServerLoader();
  }

  private static ExtensionLoader<Server> buildServerLoader() {
    return ExtensionLoaderFactory.getExtensionLoader(
        Server.class,
        new ExtensionLoaderListener<Server>() {
          @Override
          public void onLoad(ExtensionClass<Server> extensionClass) {
            Server server = extensionClass.getExtInstance();
            AutowiredAnnotationBeanPostProcessor processor =
                beanFactory.getBean(AutowiredAnnotationBeanPostProcessor.class);
            processor.postProcessProperties(null, server, null);
            SERVER_MAP.put(extensionClass.getAlias(), server);
          }
        });
  }

  public static Collection<String> servers() {
    return SERVER_MAP.keySet();
  }

  static Collection<Server> instances() {
    return SERVER_MAP.values();
  }
}
