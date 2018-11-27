package com.acmedcare.tiffany.framework.v210.rc1;

import com.acmedcare.tiffany.framework.remoting.android.nio.filter.ssl.KeyStoreFactory;
import com.acmedcare.tiffany.framework.remoting.android.nio.filter.ssl.SslContextFactory;
import java.io.File;
import java.security.KeyStore;
import javax.net.ssl.SSLContext;

/**
 * com.acmedcare.tiffany.framework.v210.rc1
 *
 * @author <a href="mailto:iskp.me@gmail.com">Elve.Xu</a>
 * @version ${project.version} - 28/11/2018.
 */
public class SSLTest {
  public static void main(String[] args) throws Exception {

    KeyStoreFactory keyStoreFactory = new KeyStoreFactory();
    keyStoreFactory.setPassword("1qaz2wsx");
    keyStoreFactory.setDataFile(new File("/Users/ive/git-acmedcare/Acmedcare-NewIM/remoting-certs/client/keystore.jks"));

    KeyStore keyStore = keyStoreFactory.newInstance();
    KeyStoreFactory truststoreFactory = new KeyStoreFactory();
    truststoreFactory.setPassword("1qaz2wsx");
    truststoreFactory.setDataFile(new File("/Users/ive/git-acmedcare/Acmedcare-NewIM/remoting-certs/client/keystore.jks"));
    KeyStore trustStore = truststoreFactory.newInstance();

    SslContextFactory sslContextFactory = new SslContextFactory();
    sslContextFactory.setProtocol("TLS");
    sslContextFactory.setKeyManagerFactoryAlgorithm("SunX509");
    sslContextFactory.setKeyManagerFactoryKeyStore(keyStore);
    sslContextFactory.setKeyManagerFactoryKeyStorePassword("1qaz2wsx");
    sslContextFactory.setTrustManagerFactoryAlgorithm("SunX509");
    sslContextFactory.setTrustManagerFactoryKeyStore(trustStore);

    SSLContext sslContext = sslContextFactory.newInstance();

    System.out.println(sslContext);
  }
}
