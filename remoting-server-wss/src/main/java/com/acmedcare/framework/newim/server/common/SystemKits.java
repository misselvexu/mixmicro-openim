/*
 * Copyright 1999-2018 Acmedcare+ Holding Ltd.
 * Licensed under the Apache License, Version 2.0 (the "License");
 */
package com.acmedcare.framework.newim.server.common;

import com.sun.management.OperatingSystemMXBean;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.lang.management.ManagementFactory;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Enumeration;
import java.util.List;

/** @author Elve.Xu */
public class SystemKits {

  private static final Logger log = LoggerFactory.getLogger(SystemKits.class);

  public static final String NEWIM_WSS_HOME_KEY = "newim.wss.home";

  public static final String NEWIM_WSS_LOCAL_IP_KEY = "NEWIM_WSS_LOCAL_IP";

  public static final String LOCAL_IP = getHostAddress();

  public static final String NEWIM_WSS_HOME = getWssHome();

  private static OperatingSystemMXBean operatingSystemMXBean =
      (OperatingSystemMXBean) ManagementFactory.getOperatingSystemMXBean();

  public static List<String> getIPsBySystemEnv(String key) {
    String env = getSystemEnv(key);
    List<String> ips = new ArrayList<String>();
    if (StringUtils.isNotEmpty(env)) {
      ips = Arrays.asList(env.split(","));
    }
    return ips;
  }

  public static String getSystemEnv(String key) {
    return System.getenv(key);
  }

  public static float getLoad() {
    return (float) operatingSystemMXBean.getSystemLoadAverage();
  }

  public static float getCPU() {
    return (float) operatingSystemMXBean.getSystemCpuLoad();
  }

  public static float getMem() {
    return (float)
        (1
            - (double) operatingSystemMXBean.getFreePhysicalMemorySize()
                / (double) operatingSystemMXBean.getTotalPhysicalMemorySize());
  }

  private static String getHostAddress() {

    String address = "127.0.0.1";

    try {
      Enumeration<NetworkInterface> networkInterfaces = NetworkInterface.getNetworkInterfaces();
      while (networkInterfaces.hasMoreElements()) {
        NetworkInterface networkInterface = networkInterfaces.nextElement();
        Enumeration<InetAddress> inetAddresses = networkInterface.getInetAddresses();
        while (inetAddresses.hasMoreElements()) {
          InetAddress ip = inetAddresses.nextElement();
          // 兼容不规范网段
          if (!ip.isLoopbackAddress() && !ip.getHostAddress().contains(":")) {
            return ip.getHostAddress();
          }
        }
      }
    } catch (Exception e) {
      log.warn("get local host address error", e);
    }

    return address;
  }

  private static String getWssHome() {
    String home = System.getProperty(NEWIM_WSS_HOME_KEY);
    if (StringUtils.isBlank(home)) {
      home = System.getProperty("user.home") + File.separator + "newim.wss";
    }
    return home;
  }
}
