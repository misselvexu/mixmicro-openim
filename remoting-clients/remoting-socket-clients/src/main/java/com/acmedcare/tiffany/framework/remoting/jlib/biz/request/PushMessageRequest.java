package com.acmedcare.tiffany.framework.remoting.jlib.biz.request;

import com.acmedcare.nas.api.ProgressCallback;
import com.acmedcare.tiffany.framework.remoting.jlib.biz.bean.Message;
import java.io.File;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Push Message Request
 *
 * @author <a href="mailto:iskp.me@gmail.com">Elve.Xu</a>
 * @version v1.0 - 10/08/2018.
 */
@Getter
@Setter
@NoArgsConstructor
public class PushMessageRequest extends BaseRequest {

  private String messageType;

  private Message message;

  /** send file instance */
  private File file;

  private ProgressCallback progressCallback;

  public interface Callback {

    void onSuccess(long messageId);

    void onFailed(int code, String message);
  }

  public abstract class MediaMessageCallback implements Callback {

    public abstract void onMediaPayloadUploadSuccess();

    @Override
    public void onSuccess(long messageId) {}

    @Override
    public void onFailed(int code, String message) {}
  }
}
