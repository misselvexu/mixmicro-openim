package com.acmedcare.framework.newim;

import java.io.Serializable;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Group
 *
 * @author Elve.Xu [iskp.me<at>gmail.com]
 * @version v1.0 - 09/08/2018.
 */
@Getter
@Setter
@NoArgsConstructor
public class Group implements Serializable {

  private static final long serialVersionUID = -7613061811845762121L;

  private String name;
  private String code;

  @Builder
  public Group(String name, String code) {
    this.name = name;
    this.code = code;
  }
}
