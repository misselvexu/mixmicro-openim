package com.acmedcare.framework.newim.wss;

import com.alibaba.fastjson.JSON;
import lombok.Builder;
import lombok.Builder.Default;
import lombok.Getter;
import lombok.Setter;

/**
 * WebSocket Message
 *
 * @author <a href="mailto:iskp.me@gmail.com">Elve.Xu</a>
 * @version ${project.version} - 19/11/2018.
 */
@Getter
@Setter
public class WssPayload {

  public String json() {
    return JSON.toJSONString(this);
  }

  public byte[] bytes() {
    return JSON.toJSONBytes(this);
  }

  @Getter
  @Setter
  public static class WssRequest extends WssPayload {
    /**
     * Biz Code Request
     *
     * @see com.acmedcare.framework.newim.protocol.Command.WebSocketClusterCommand
     */
    private int bizCode;
  }

  @Getter
  @Setter
  @Builder
  public static class WssResponse<T> extends WssPayload {

    /**
     * Biz Code Request
     *
     * @see com.acmedcare.framework.newim.protocol.Command.WebSocketClusterCommand
     */
    private int bizCode;

    /**
     * Processor Code
     *
     * <p>>=0 success
     *
     * <p><0 failed
     */
    @Default private int code = -1;

    /**
     * response data
     *
     * @see T
     */
    private T data;

    public static WssResponse failResponse(int bizCode, String message) {
      return WssResponse.<String>builder().bizCode(bizCode).code(-1).data(message).build();
    }

    public static WssResponse successResponse(int bizCode, java.lang.Object object) {
      return WssResponse.<java.lang.Object>builder().bizCode(bizCode).code(0).data(object).build();
    }

    public static WssResponse successResponse(int bizCode) {
      return WssResponse.<java.lang.Object>builder().bizCode(bizCode).code(0).build();
    }
  }
}
