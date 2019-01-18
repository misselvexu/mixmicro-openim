package com.acmedcare.tiffany.framework.remoting.jlib.biz.bean;

import java.io.Serializable;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Member Detail
 *
 * @author <a href="mailto:iskp.me@gmail.com">Elve.Xu</a>
 * @version ${project.version} - 2018-12-06.
 */
@Getter
@Setter
@NoArgsConstructor
public class Member implements Serializable {

  private static final long serialVersionUID = 936762440477690671L;

  private Long memberId;

  private String memberName;

  private String memberUserName;

  private String portrait;

  private String memberExt;

  @Builder
  public Member(Long memberId, String memberName, String memberUserName, String memberExt, String portrait) {
    this.memberId = memberId;
    this.memberName = memberName;
    this.memberUserName = memberUserName;
    this.memberExt = memberExt;
    this.portrait = portrait;
  }
}
