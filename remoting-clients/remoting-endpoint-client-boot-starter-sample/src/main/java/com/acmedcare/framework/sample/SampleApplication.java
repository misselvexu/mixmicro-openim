package com.acmedcare.framework.sample;

import com.acmedcare.framework.newim.client.MessageBizType;
import com.acmedcare.framework.newim.client.MessageContentType;
import com.acmedcare.framework.newim.master.endpoint.client.MasterEndpointClient;
import com.google.common.collect.Lists;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * com.acmedcare.framework.newim.client.sample
 *
 * @author <a href="mailto:iskp.me@gmail.com">Elve.Xu</a>
 * @version ${project.version} - 29/11/2018.
 */
@SpringBootApplication
public class SampleApplication {

  public static void main(String[] args) {
    SpringApplication.run(SampleApplication.class, args);
  }

  @RestController
  public static class DemoController {

    @Autowired private MasterEndpointClient masterEndpointClient;


    @GetMapping("/")
    String test() {

      this.masterEndpointClient.batchSendSingleMessages(
          MessageBizType.NORMAL,
          "JUNIT-TEST",
          Lists.newArrayList("3837142362366976", "3837142362366977"),
          "{\"name\":\"misselvexu\"}",
          MessageContentType.APPLICATION_JSON,
          null);

      return "test";
    }
  }
}
