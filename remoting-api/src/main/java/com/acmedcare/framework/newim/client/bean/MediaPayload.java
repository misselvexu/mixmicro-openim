package com.acmedcare.framework.newim.client.bean;

import java.io.Serializable;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Media Payload
 *
 * @author <a href="mailto:iskp.me@gmail.com">Elve.Xu</a>
 * @version v1.0 - 09/08/2018.
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class MediaPayload implements Serializable {

  private static final long serialVersionUID = -1496285586690313202L;

  private String mediaPayloadKey;
  /** 媒体文件访问连接 */
  private String mediaPayloadAccessUrl;

  private String mediaFileName;

  private String mediaFileSuffix;
}
