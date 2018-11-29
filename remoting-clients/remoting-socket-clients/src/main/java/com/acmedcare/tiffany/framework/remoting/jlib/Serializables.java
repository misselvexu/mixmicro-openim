package com.acmedcare.tiffany.framework.remoting.jlib;

import com.alibaba.fastjson.JSON;

/**
 * Serializables Kits
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
}
