/*
 * Copyright 1999-2018 Acmedcare+ Holding Ltd.
 * Licensed under the Apache License, Version 2.0 (the "License");
 */

package com.acmedcare.framework.newim.deliver.api;

import com.acmedcare.framework.newim.deliver.api.exception.RemotingDelivererException;
import com.acmedcare.framework.newim.deliver.api.header.RegistryHeader;
import com.acmedcare.framework.newim.deliver.api.request.RegistryRequestBean;
import com.acmedcare.framework.newim.deliver.api.response.RegistryResponseBean;
import com.acmedcare.framework.newim.spi.Extension;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * {@link DefaultDelivererApi}
 *
 * @author <a href="mailto:iskp.me@gmail.com">Elve.Xu</a>
 * @version ${project.version} - 2019-07-29.
 */
@Extension("default")
public class DefaultDelivererApi implements RemotingDelivererApi {

  private static final Logger log = LoggerFactory.getLogger(DefaultDelivererApi.class);

  /**
   * Registry With Params
   *
   * @param header  request header
   * @param request request params bean instance
   * @return response instance of {@link RegistryResponseBean}
   * @throws RemotingDelivererException maybe thrown exception of {@link RemotingDelivererException}
   */
  @Override
  public RegistryResponseBean register(RegistryHeader header, RegistryRequestBean request) throws RemotingDelivererException {

    // TODO

    return null;
  }
}
