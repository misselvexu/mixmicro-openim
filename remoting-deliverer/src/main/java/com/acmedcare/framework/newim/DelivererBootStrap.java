/*
 * Copyright 1999-2018 Acmedcare+ Holding Ltd.
 * Licensed under the Apache License, Version 2.0 (the "License");
 */

package com.acmedcare.framework.newim;

import org.springframework.boot.WebApplicationType;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.builder.SpringApplicationBuilder;

/**
 * {@link DelivererBootStrap}
 *
 * @author <a href="mailto:iskp.me@gmail.com">Elve.Xu</a>
 * @version ${project.version} - 2019-07-13.
 */
@SpringBootApplication
public class DelivererBootStrap {

  public static void main(String[] args) {
    new SpringApplicationBuilder()
        .sources(DelivererBootStrap.class)
        .properties("--spring.profiles.active=production")
        .web(WebApplicationType.NONE)
        .run(args);
  }
}
