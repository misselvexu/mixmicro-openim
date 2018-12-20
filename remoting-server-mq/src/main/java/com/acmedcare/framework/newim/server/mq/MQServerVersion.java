package com.acmedcare.framework.newim.server.mq;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

/**
 * MQServerVersion
 *
 * @author <a href="mailto:iskp.me@gmail.com">Elve.Xu</a>
 * @version ${project.version} - 2018-12-20.
 */
public class MQServerVersion {

  /**
   * The version format is X.Y.Z (Major.Minor.Patch), a pre-release version may be denoted by
   * appending a hyphen and a series of dot-separated identifiers immediately following the patch
   * version, like X.Y.Z-alpha.
   *
   * <p>Server version follows semver scheme partially.
   *
   * @see <a href="http://semver.org">http://semver.org</a>
   */
  public static String version = "UnKnown";

  static {
    InputStream stream =
        MQServerVersion.class.getClassLoader().getResourceAsStream("mq-server-version.properties");
    try {
      if (stream != null) {
        Properties properties = new Properties();
        properties.load(stream);
        version = String.valueOf(properties.get("version"));
      }
    } catch (IOException ignore) {
    }
  }
}
