package com.acmedcare.microservices.im.endpoint;

import com.alibaba.fastjson.annotation.JSONField;
import java.io.Serializable;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Tencent Result
 *
 * @author <a href="mailto:iskp.me@gmail.com">Elve.Xu</a>
 * @version v1.0 - 09/08/2018.
 */
@Getter
@Setter
@NoArgsConstructor
public class TencentResult implements Serializable {
  private static final long serialVersionUID = -4972660175940663895L;

  public static TencentResult SUCCESS =
      TencentResult.builder().errorCode(0).errorInfo("").actionStatus("OK").build();
  public static TencentResult FAILED =
      TencentResult.builder().errorCode(-1).actionStatus("FAIL").build();

  @JSONField(name = "ActionStatus")
  private String actionStatus;

  @JSONField(name = "ErrorInfo")
  private String errorInfo;

  @JSONField(name = "ErrorCode")
  private int errorCode;

  @Builder
  public TencentResult(String actionStatus, String errorInfo, int errorCode) {
    this.actionStatus = actionStatus;
    this.errorInfo = errorInfo;
    this.errorCode = errorCode;
  }
}
