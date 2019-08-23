/*
 * Copyright 1999-2018 Acmedcare+ Holding Ltd.
 * Licensed under the Apache License, Version 2.0 (the "License");
 */

package com.acmedcare.framework.newim.master.services;

import com.acmedcare.framework.newim.storage.api.AccountRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

/**
 * {@link AccountServices}
 *
 * @author <a href="mailto:iskp.me@gmail.com">Elve.Xu</a>
 * @version ${project.version} - 2019-08-16.
 */
@Service
public class AccountServices {

  private static final Logger log = LoggerFactory.getLogger(AccountServices.class);

  private final AccountRepository accountRepository;


  public AccountServices(AccountRepository accountRepository) {
    this.accountRepository = accountRepository;
  }



}
