package com.acmedcare.framework.remoting.mq.client;

import com.google.common.annotations.GwtCompatible;
import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Alias For Field
 *
 * @author <a href="mailto:iskp.me@gmail.com">Elve.Xu</a>
 * @version ${project.version} - 26/11/2018.
 */
@Retention(RetentionPolicy.CLASS)
@Target({ElementType.FIELD})
@Documented
@GwtCompatible
public @interface Alias {

  /**
   * Real Field Name
   *
   * @return Real Field Name
   */
  String value();
}
