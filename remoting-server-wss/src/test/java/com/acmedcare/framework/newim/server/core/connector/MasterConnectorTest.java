package com.acmedcare.framework.newim.server.core.connector;

import com.acmedcare.framework.newim.server.Application;
import com.acmedcare.framework.newim.server.config.IMProperties;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

/**
 * Master Connector Test
 *
 * @author <a href="mailto:iskp.me@gmail.com">Elve.Xu</a>
 * @version ${project.version} - 15/11/2018.
 */
@RunWith(SpringRunner.class)
@SpringBootTest(classes = Application.class)
public class MasterConnectorTest {

  @Autowired private IMProperties properties;

  @Test
  public void testMasterConnector() {

    MasterConnector masterConnector = new MasterConnector(properties, null);
    masterConnector.start();

    try {
      Thread.sleep(Integer.MAX_VALUE);
    } catch (InterruptedException e) {
      e.printStackTrace();
    }
  }
}
