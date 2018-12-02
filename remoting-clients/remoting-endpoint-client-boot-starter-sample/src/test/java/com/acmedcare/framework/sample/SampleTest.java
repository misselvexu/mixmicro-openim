package com.acmedcare.framework.sample;

import com.acmedcare.framework.newim.master.endpoint.client.MasterEndpointClient;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

/**
 * com.acmedcare.framework.sample
 *
 * @author <a href="mailto:iskp.me@gmail.com">Elve.Xu</a>
 * @version ${project.version} - 2018-11-30.
 */
@RunWith(SpringRunner.class)
@SpringBootTest(classes = SampleApplication.class)
public class SampleTest {

  @Autowired private MasterEndpointClient masterEndpointClient;

  @Test
  public void test() {
    this.masterEndpointClient.removeGroup("ss");
  }
}
