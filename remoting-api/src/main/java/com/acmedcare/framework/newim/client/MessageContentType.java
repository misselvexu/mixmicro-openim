package com.acmedcare.framework.newim.client;

/**
 * MESSAGE Content Type
 *
 * @author <a href="mailto:iskp.me@gmail.com">Elve.Xu</a>
 * @version ${project.version} - 29/11/2018.
 */
public enum MessageContentType {

  /** JSON */
  APPLICATION_JSON("application/json;charset=utf-8"),

  /** Normal */
  TEXT_PLAIN("text/plain");

  String contentType;

  MessageContentType(String contentType) {
    this.contentType = contentType;
  }

  public String contentType() {
    return contentType;
  }
}
