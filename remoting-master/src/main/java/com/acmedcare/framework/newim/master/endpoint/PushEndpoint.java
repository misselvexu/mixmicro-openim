package com.acmedcare.framework.newim.master.endpoint;

import static com.acmedcare.framework.newim.MasterLogger.endpointLog;

import com.acmedcare.framework.exception.defined.InvalidRequestParamException;
import com.acmedcare.framework.newim.BizResult;
import com.acmedcare.framework.newim.BizResult.ExceptionWrapper;
import com.acmedcare.framework.newim.client.bean.request.PushNoticeRequest;
import com.acmedcare.framework.newim.master.services.PushServices;
import com.acmedcare.framework.newim.storage.exception.StorageException;
import com.alibaba.fastjson.JSON;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Push Endpoint
 *
 * @author <a href="mailto:iskp.me@gmail.com">Elve.Xu</a>
 * @version ${project.version} - 09/11/2018.
 */
@RestController
@RequestMapping("/push")
public class PushEndpoint {

  private final PushServices pushServices;

  @Autowired
  public PushEndpoint(PushServices pushServices) {
    this.pushServices = pushServices;
  }

  @PostMapping(value = "/notice")
  ResponseEntity<?> pushNotice(PushNoticeRequest request) {
    try {
      endpointLog.info("推送通知请求参数: {}", JSON.toJSONString(request));

      if (StringUtils.isAnyBlank(request.getContent(), request.getTitle(), request.getAppName())) {
        throw new InvalidRequestParamException("推送通知的参数[content,title,appName]不能为空");
      }

      this.pushServices.sendNotice(
          request.isUseTimer(),
          request.getTimerExpression(),
          request.getAppName(),
          request.getContent(),
          request.getAction(),
          request.getTitle(),
          request.getExt(),
          request.getDeviceIds());

      return ResponseEntity.ok().build();
    } catch (InvalidRequestParamException | StorageException e) {
      endpointLog.error("推送通知异常", e);
      return ResponseEntity.status(HttpStatus.BAD_REQUEST)
          .body(
              BizResult.builder()
                  .code(-1)
                  .exception(
                      ExceptionWrapper.builder().type(e.getClass()).message(e.getMessage()).build())
                  .build());
    } catch (Exception e) {
      endpointLog.error("推送通知异常", e);
      return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
          .body(
              BizResult.builder()
                  .code(-1)
                  .exception(
                      ExceptionWrapper.builder().type(e.getClass()).message(e.getMessage()).build())
                  .build());
    }
  }
}
