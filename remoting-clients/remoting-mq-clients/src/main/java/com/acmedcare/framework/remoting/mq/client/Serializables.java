package com.acmedcare.framework.remoting.mq.client;

import com.alibaba.fastjson.JSON;

/**
 * Serializable's Kits
 *
 * @author <a href="mailto:iskp.me@gmail.com">Elve.Xu</a>
 * @version alpha - 10/08/2018.
 */
public final class Serializables {

  /**
   * Parse object from json string
   *
   * @param json json string
   * @param clazz target class innerType
   * @param <O> Class
   * @return Object
   */
  public static <O> O fromJSON(String json, Class<O> clazz) {
    if (json == null || json.trim().length() <= 0 || clazz == null) {
      return null;
    }
    return JSON.parseObject(json, clazz);
  }

  /**
   * Parse object from json string bytes
   *
   * @param bytes json string bytes
   * @param clazz target class innerType
   * @param <O> Class
   * @return Object
   */
  public static <O> O fromBytes(byte[] bytes, Class<O> clazz) {
    if (bytes == null || bytes.length <= 0 || clazz == null) {
      return null;
    }
    return JSON.parseObject(bytes, clazz);
  }

  /**
   * Checks if any of the CharSequences are empty ("") or null or whitespace only.
   *
   * <p>Whitespace is defined by {@link Character#isWhitespace(char)}.
   *
   * <pre>
   * StringUtils.isAnyBlank(null)             = true
   * StringUtils.isAnyBlank(null, "foo")      = true
   * StringUtils.isAnyBlank(null, null)       = true
   * StringUtils.isAnyBlank("", "bar")        = true
   * StringUtils.isAnyBlank("bob", "")        = true
   * StringUtils.isAnyBlank("  bob  ", null)  = true
   * StringUtils.isAnyBlank(" ", "bar")       = true
   * StringUtils.isAnyBlank(new String[] {})  = false
   * StringUtils.isAnyBlank(new String[]{""}) = true
   * StringUtils.isAnyBlank("foo", "bar")     = false
   * </pre>
   *
   * @param css the CharSequences to check, may be null or empty
   * @return {@code true} if any of the CharSequences are empty or null or whitespace only
   * @since 3.2
   */
  public static boolean isAnyBlank(final CharSequence... css) {
    if (css == null || css.length == 0) {
      return false;
    }
    for (final CharSequence cs : css) {
      if (isBlank(cs)) {
        return true;
      }
    }
    return false;
  }

  /**
   * Checks if a CharSequence is empty (""), null or whitespace only.
   *
   * <p>Whitespace is defined by {@link Character#isWhitespace(char)}.
   *
   * <pre>
   * StringUtils.isBlank(null)      = true
   * StringUtils.isBlank("")        = true
   * StringUtils.isBlank(" ")       = true
   * StringUtils.isBlank("bob")     = false
   * StringUtils.isBlank("  bob  ") = false
   * </pre>
   *
   * @param cs the CharSequence to check, may be null
   * @return {@code true} if the CharSequence is null, empty or whitespace only
   * @since 2.0
   * @since 3.0 Changed signature from isBlank(String) to isBlank(CharSequence)
   */
  public static boolean isBlank(final CharSequence cs) {
    int strLen;
    if (cs == null || (strLen = cs.length()) == 0) {
      return true;
    }
    for (int i = 0; i < strLen; i++) {
      if (!Character.isWhitespace(cs.charAt(i))) {
        return false;
      }
    }
    return true;
  }
}
