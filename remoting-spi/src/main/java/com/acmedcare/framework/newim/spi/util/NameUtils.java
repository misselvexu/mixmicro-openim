package com.acmedcare.framework.newim.spi.util;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Name Utils
 *
 * @author <a href="mailto:iskp.me@gmail.com">Elve.Xu</a>
 * @version ${project.version} - 2018-12-05.
 */
public final class NameUtils {

  private static Pattern humpPattern = Pattern.compile("[A-Z]");

  /**
   * convert field name from hump to line '-'
   *
   * @param str str
   * @return result
   */
  public static String humpToLine(String str) {
    Matcher matcher = humpPattern.matcher(str);
    StringBuffer sb = new StringBuffer();
    while (matcher.find()) {
      matcher.appendReplacement(sb, "-" + matcher.group(0));
    }
    matcher.appendTail(sb);
    return sb.toString().toLowerCase();
  }
}
