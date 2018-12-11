package com.acmedcare.framework.newim.client.bean.request;

import com.acmedcare.framework.newim.client.MessageConstants;
import java.io.Serializable;
import java.util.List;
import lombok.Getter;
import lombok.Setter;

/**
 * Push Notice Request
 *
 * @author <a href="mailto:iskp.me@gmail.com">Elve.Xu</a>
 * @version ${project.version} - 26/11/2018.
 */
@Getter
@Setter
public class PushNoticeRequest implements Serializable {

  private static final long serialVersionUID = -4880453865084170588L;
  private String namespace = MessageConstants.DEFAULT_NAMESPACE;
  /** 是否定时发送 */
  private boolean useTimer;

  /** 定时表达式 */
  private String timerExpression;

  /** 应用名称 */
  private String appName;

  /** 标题 */
  private String title;

  /** 内容 */
  private String content;

  /** 动作 */
  private String action;

  /** 扩展 */
  private String ext;

  /** 接收通知的设备列表 */
  private List<String> deviceIds;
}
