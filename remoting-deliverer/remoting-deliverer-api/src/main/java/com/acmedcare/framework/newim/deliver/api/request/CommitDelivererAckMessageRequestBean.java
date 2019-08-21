/*
 * Copyright 1999-2018 Acmedcare+ Holding Ltd.
 * Licensed under the Apache License, Version 2.0 (the "License");
 */

package com.acmedcare.framework.newim.deliver.api.request;

import lombok.*;

import java.io.Serializable;

/**
 * {@link CommitDelivererAckMessageRequestBean}
 *
 * @author <a href="mailto:iskp.me@gmail.com">Elve.Xu</a>
 * @version ${project.version} - 2019-08-20.
 */
@Getter
@Setter
@Builder
@ToString
@NoArgsConstructor
@AllArgsConstructor
public class CommitDelivererAckMessageRequestBean implements Serializable {

  private static final long serialVersionUID = 4389906507956357156L;

  private String namespace;

  private String passportId;

  private String messageId;

}
