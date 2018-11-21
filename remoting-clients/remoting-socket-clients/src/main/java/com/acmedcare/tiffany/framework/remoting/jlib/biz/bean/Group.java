package com.acmedcare.tiffany.framework.remoting.jlib.biz.bean;

import java.io.Serializable;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Group
 *
 * @author <a href="mailto:iskp.me@gmail.com">Elve.Xu</a>
 * @version v1.0 - 09/08/2018.
 */
@Getter
@Setter
@NoArgsConstructor
public class Group implements Serializable {

  private static final long serialVersionUID = -7613061811845762121L;

  private String name;
  private String code;

  public Group(String name, String code) {
    this.name = name;
    this.code = code;
  }

  /**
   * Group Ext Properties
   *
   * @version v1.0
   */
  @Getter
  @Setter
  @NoArgsConstructor
  public static class GroupExt extends Group {
    private static final long serialVersionUID = -4292596471820796119L;

    private int unreadSize;
    private Message leastMessage;

    @Builder
    public GroupExt(String name, String code, int unreadSize, Message leastMessage) {
      super(name, code);
      this.unreadSize = unreadSize;
      this.leastMessage = leastMessage;
    }
  }
}
