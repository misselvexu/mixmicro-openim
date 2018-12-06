package com.acmedcare.tiffany.framework.remoting.jlib.biz.response;

import com.acmedcare.tiffany.framework.remoting.jlib.biz.bean.Member;
import com.google.common.collect.Lists;
import java.io.Serializable;
import java.util.List;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Group Message Read Status Response Body
 *
 * @author <a href="mailto:iskp.me@gmail.com">Elve.Xu</a>
 * @version ${project.version} - 2018-12-06.
 */
@Getter
@Setter
@NoArgsConstructor
public class GroupMessageReadStatusResponse implements Serializable {

  private static final long serialVersionUID = 188320582863384731L;

  private List<Member> readers = Lists.newArrayList(); // 已读人数

  private List<Member> unReaders = Lists.newArrayList(); // 未读人数
}
