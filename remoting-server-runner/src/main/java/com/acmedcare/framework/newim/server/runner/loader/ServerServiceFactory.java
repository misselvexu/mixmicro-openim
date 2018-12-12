package com.acmedcare.framework.newim.server.runner.loader;

import com.acmedcare.framework.newim.server.Server;
import com.acmedcare.framework.newim.spi.ExtensionClass;
import com.acmedcare.framework.newim.spi.ExtensionLoader;
import com.acmedcare.framework.newim.spi.ExtensionLoaderFactory;
import com.acmedcare.framework.newim.spi.ExtensionLoaderListener;
import java.util.Collection;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Server Service Factory
 *
 * @author <a href="mailto:iskp.me@gmail.com">Elve.Xu</a>
 * @version ${project.version} - 2018-12-12.
 */
public class ServerServiceFactory {

  private static final Map<String, Server> SERVER_MAP = new ConcurrentHashMap<>();

  private static final ExtensionLoader<Server> SERVER_EXTENSION_LOADER = buildServerLoader();

  private static ExtensionLoader<Server> buildServerLoader() {
    return ExtensionLoaderFactory.getExtensionLoader(
        Server.class,
        new ExtensionLoaderListener<Server>() {
          @Override
          public void onLoad(ExtensionClass<Server> extensionClass) {
            SERVER_MAP.put(extensionClass.getAlias(), extensionClass.getExtInstance());
          }
        });
  }

  public static Collection<String> servers() {
    return SERVER_MAP.keySet();
  }

  public static Collection<Server> instances() {
    return SERVER_MAP.values();
  }
}
