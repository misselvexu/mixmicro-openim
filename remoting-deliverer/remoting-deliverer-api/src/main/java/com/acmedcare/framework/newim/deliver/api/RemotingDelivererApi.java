/*
 * Copyright 1999-2018 Acmedcare+ Holding Ltd.
 * Licensed under the Apache License, Version 2.0 (the "License");
 */

package com.acmedcare.framework.newim.deliver.api;

import com.acmedcare.framework.newim.deliver.api.exception.RemotingDelivererException;
import com.acmedcare.framework.newim.deliver.api.header.RegistryHeader;
import com.acmedcare.framework.newim.deliver.api.request.RegistryRequestBean;
import com.acmedcare.framework.newim.deliver.api.response.RegistryResponseBean;
import com.acmedcare.framework.newim.spi.Extensible;

/**
 * {@link RemotingDelivererApi}
 *
 * @author <a href="mailto:iskp.me@gmail.com">Elve.Xu</a>
 * @version ${project.version} - 2019-07-29.
 */
@Extensible
public interface RemotingDelivererApi {

  /**
   * Registry With Params
   *
   * @param header request header
   * @param request request params bean instance
   * @return response instance of {@link RegistryResponseBean}
   * @throws RemotingDelivererException maybe thrown exception of {@link RemotingDelivererException}
   */
  RegistryResponseBean register(RegistryHeader header, RegistryRequestBean request)
      throws RemotingDelivererException;



}
