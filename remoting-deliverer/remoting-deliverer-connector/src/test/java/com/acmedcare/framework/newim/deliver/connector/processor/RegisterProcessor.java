/*
 * Copyright 1999-2018 Acmedcare+ Holding Ltd.
 * Licensed under the Apache License, Version 2.0 (the "License");
 */

package com.acmedcare.framework.newim.deliver.connector.processor;

import com.acmedcare.framework.newim.deliver.connector.bean.RegistryRequestBean;
import com.acmedcare.framework.newim.deliver.connector.bean.RegistryResponseBean;
import com.acmedcare.framework.remoting.AsyncContext;
import com.acmedcare.framework.remoting.BizContext;
import com.acmedcare.framework.remoting.rpc.protocol.AsyncUserProcessor;
import com.alibaba.fastjson.JSON;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * {@link RegisterProcessor}
 *
 * @author <a href="mailto:iskp.me@gmail.com">Elve.Xu</a>
 * @version ${project.version} - 2019-07-25.
 */
public class RegisterProcessor extends AsyncUserProcessor<RegistryRequestBean> {

  private static final Logger log = LoggerFactory.getLogger(RegisterProcessor.class);

  @Override
  public void handleRequest(BizContext bizCtx, AsyncContext asyncCtx, RegistryRequestBean request) {

    log.info("[BIZ-CTX] Remote Address :{}", bizCtx.getRemoteAddress());

    log.info("[REQUEST-BEAN] Payload: {}", JSON.toJSONString(request));

    RegistryResponseBean registryResponseBean =
        RegistryResponseBean.builder().timestamp(System.currentTimeMillis()).build();

    asyncCtx.sendResponse(registryResponseBean);
  }

  @Override
  public String interest() {
    return RegistryRequestBean.class.getName();
  }
}
