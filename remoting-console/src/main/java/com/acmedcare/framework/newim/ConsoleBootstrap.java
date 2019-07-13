/*
 * Copyright 1999-2018 Acmedcare+ Holding Ltd.
 * Licensed under the Apache License, Version 2.0 (the "License");
 */

package com.acmedcare.framework.newim;

import org.springframework.boot.WebApplicationType;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.builder.SpringApplicationBuilder;

/**
 * {@link ConsoleBootstrap}
 *
 * @author <a href="mailto:iskp.me@gmail.com">Elve.Xu</a>
 * @version ${project.version} - 2019-07-13.
 */
@SpringBootApplication
public class ConsoleBootstrap {

  public static void main(String[] args) {
    new SpringApplicationBuilder()
        .sources(ConsoleBootstrap.class)
        .properties("--spring.profiles.active=production")
        .web(WebApplicationType.REACTIVE)
        .run(args);
  }
}
