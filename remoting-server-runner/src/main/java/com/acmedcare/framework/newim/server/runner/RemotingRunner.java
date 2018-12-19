package com.acmedcare.framework.newim.server.runner;

import com.acmedcare.framework.boot.snowflake.EnableSnowflake;
import com.acmedcare.framework.kits.thread.ThreadKit;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.stereotype.Component;

/**
 * Remoting Server Runner
 *
 * @author <a href="mailto:iskp.me@gmail.com">Elve.Xu</a>
 * @version ${project.version} - 2018-12-12.
 */
@SpringBootApplication
@EnableSnowflake
public class RemotingRunner {

  public static void main(String[] args) {
    SpringApplication.run(RemotingRunner.class, args);
  }

  @Component
  public static class DemoThread implements Runnable, InitializingBean {

    @Autowired RaftClientService raftClientService;

    @Override
    public void run() {
      System.out.println("等待程序启动完成");
      ThreadKit.sleep(5000);
      while (true) {
        try {
          System.out.println("获取值: " + raftClientService.nextUniformMessageId());
          ThreadKit.sleep(2000);
        } catch (Exception e) {

        }
      }
    }

    @Override
    public void afterPropertiesSet() throws Exception {
      new Thread(this).start();
    }
  }
}
