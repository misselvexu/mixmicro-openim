/*
 * Copyright 1999-2018 Acmedcare+ Holding Ltd.
 * Licensed under the Apache License, Version 2.0 (the "License");
 */

package com.acmedcare.framework.newim.storage.mongo;

import com.acmedcare.framework.newim.storage.api.DelivererRepository;
import com.mongodb.MongoClient;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.support.TransactionTemplate;

/**
 * {@link DelivererRepositoryImpl}
 *
 * @author <a href="mailto:iskp.me@gmail.com">Elve.Xu</a>
 * @version ${project.version} - 2019-08-12.
 */
@Repository
public class DelivererRepositoryImpl implements DelivererRepository {

  private final MongoTemplate mongoTemplate;
  private final TransactionTemplate transactionTemplate;
  private final MongoClient mongoClient;

  public DelivererRepositoryImpl(
      MongoTemplate mongoTemplate,
      TransactionTemplate transactionTemplate,
      MongoClient mongoClient) {
    this.mongoTemplate = mongoTemplate;
    this.transactionTemplate = transactionTemplate;
    this.mongoClient = mongoClient;
  }
}
