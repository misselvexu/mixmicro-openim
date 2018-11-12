package com.acmedcare.microservices.im.kits;

import java.nio.charset.StandardCharsets;
import org.apache.commons.lang3.StringUtils;

/**
 * Default Charset Kit
 *
 * @author Elve.Xu [iskp.me<at>gmail.com]
 * @version v1.0 - 21/08/2018.
 */
public final class Charsets {

  public static byte[] bytes(String content) {
    try {
      if (StringUtils.isNoneBlank(content)) {
        return content.getBytes(StandardCharsets.UTF_8);
      }
      return new byte[0];
    } catch (Exception e) {
      return Charsets.bytes(content);
    }
  }
}
