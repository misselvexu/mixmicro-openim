package com.acmedcare.tiffany.framework.remoting.jlib;

import android.util.Log;

import javax.annotation.Nullable;

/**
 * Acmedcare SDK Logger Utils
 *
 * @author <a href="mailto:iskp.me@gmail.com">Elve.Xu</a>
 * @version alpha - 26/07/2018.
 */
public class AcmedcareLogger {

  public static final String NON_ANDROID_FLAG = "acmedcare.android.flag";
  static final String SDK_LOG_TAG = "Acmedcare.remoting.sdk.log";

  private static boolean isAndroid() {
    return System.getProperty(NON_ANDROID_FLAG) == null;
  }

  public static void i(@Nullable String tag, String message) {
    if (isAndroid()) {
      Log.i(SDK_LOG_TAG, message);
    } else {
      System.out.println("[" + SDK_LOG_TAG + "] >> " + message);
    }
  }

  public static void e(String tag, Throwable throwable, String message) {
    if (isAndroid()) {
      Log.e(SDK_LOG_TAG, message, throwable);
    } else {
      System.out.println("[" + SDK_LOG_TAG + "] >> " + message);
    }
  }

  public static void w(String tag, String message) {
    if (isAndroid()) {
      Log.w(SDK_LOG_TAG, message);
    } else {
      System.out.println("[" + SDK_LOG_TAG + "] >> " + message);
    }
  }
}
