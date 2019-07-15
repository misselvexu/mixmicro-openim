/*
 * Copyright 2014-2019 Acmedcare+ Holding Ltd.
 * Licensed under the Apache License, Version 2.0 (the "License");
 */

package com.acmedcare.framework.newim.server.core.connector;

import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * {@link DateConvertor}
 *
 * @author <a href="mailto:iskp.me@gmail.com">Elve.Xu</a>
 * @version ${project.version} - 2019-04-29.
 */
public class DateConvertor {
  public static void main(String[] args) {
    Date date = new Date(619703101013L);
    System.out.println(new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(date));

    date = new Date(2268223527231L);
    System.out.println(new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(date));

    System.out.println(System.nanoTime());
  }
}
