package com.acmedcare.framework.newim;

import com.alibaba.fastjson.JSON;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.io.Serializable;
import java.nio.charset.StandardCharsets;

/**
 * Biz BizResult
 *
 * @param <T> Biz BizResult
 * @author Elve.Xu
 */
@NoArgsConstructor
@Getter
@Setter
public class BizResult<T> implements Serializable {

  private static final long serialVersionUID = 4746265037967570417L;
  public static BizResult EMPTY = BizResult.builder().build();
  public static BizResult SUCCESS = BizResult.builder().code(0).build();
  public static BizResult FAILED = BizResult.builder().code(-1).build();
  /** exception */
  private ExceptionWrapper exception;
  /** data */
  private T data;
  /** InnerType */
  private Class<?> type;
  /** Response Code */
  private int code = -1;

  @Builder
  public BizResult(ExceptionWrapper exception, T data, Class<?> type, int code) {
    this.exception = exception;
    this.data = data;
    this.type = type;
    this.code = code;
  }

  /**
   * Build to json String
   *
   * @return object instance
   */
  public String json() {
    return JSON.toJSONString(this);
  }

  /**
   * Build to json bytes (UTF-8)
   *
   * @return bytes array
   */
  public byte[] bytes() {
    return json().getBytes(StandardCharsets.UTF_8);
  }

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
   * Parse object from json string
   *
   * @param json json string
   * @param clazz target class innerType
   * @param <O> Class
   * @return Object
   */
  public static <O> O fromJSON0(String json, Class<O> clazz) {
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
   * Parse object from json string bytes
   *
   * @param bytes json string bytes
   * @param clazz target class innerType
   * @param <O> Class
   * @return Object
   */
  public static <O> O fromBytes0(byte[] bytes, Class<O> clazz) {
    if (bytes == null || bytes.length <= 0 || clazz == null) {
      return null;
    }
    return JSON.parseObject(bytes, clazz);
  }

  /**
   * Exception Wrapper
   *
   * @param <E> exception
   */
  @NoArgsConstructor
  @Getter
  @Setter
  public static class ExceptionWrapper<E> {

    private Class<?> type;
    private String message;

    @Builder
    public ExceptionWrapper(Class<?> type, String message) {
      this.type = type;
      this.message = message;
    }
  }
}
