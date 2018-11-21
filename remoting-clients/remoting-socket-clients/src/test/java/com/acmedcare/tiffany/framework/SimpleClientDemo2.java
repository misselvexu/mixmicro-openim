package com.acmedcare.tiffany.framework;

import com.acmedcare.tiffany.framework.remoting.jlib.AcmedcareRemoting;
import com.acmedcare.tiffany.framework.remoting.jlib.RemotingParameters;
import com.acmedcare.tiffany.framework.remoting.jlib.ServerAddressHandler;
import com.acmedcare.tiffany.framework.remoting.jlib.biz.request.AuthRequest.AuthCallback;
import com.acmedcare.tiffany.framework.remoting.jlib.events.AcmedcareEvent;
import com.acmedcare.tiffany.framework.remoting.jlib.events.AcmedcareEvent.Event;
import com.acmedcare.tiffany.framework.remoting.jlib.events.BasicListenerHandler;
import com.acmedcare.tiffany.framework.remoting.jlib.exception.NoServerAddressException;
import com.alibaba.fastjson.JSON;
import com.google.common.collect.Lists;
import java.util.List;

/**
 * com.acmedcare.tiffany.framework
 *
 * @author <a href="mailto:iskp.me@gmail.com">Elve.Xu</a>
 * @version v1.0 - 11/08/2018.
 */
public class SimpleClientDemo2 {

  public static void main(String[] args) {

    RemotingParameters temp =
        RemotingParameters.builder()
            .authCallback(
                new AuthCallback() {
                  @Override
                  public void onSuccess() {}

                  @Override
                  public void onFailed(int code, String message) {}
                })
            .username("demo1")
            .heartbeatPeriod(10)
            .serverAddressHandler(
                new ServerAddressHandler() {
                  @Override
                  public List<RemotingAddress> remotingAddressList() {
                    return Lists.newArrayList(new RemotingAddress("127.0.0.1", 8887, false));
                  }
                })
            .build();

    AcmedcareRemoting.getInstance().init(null, temp);

    AcmedcareRemoting.getInstance()
        .onMessageEventListener(
            new BasicListenerHandler() {
              @Override
              public void execute(AcmedcareEvent acmedcareEvent) {
                Event event = acmedcareEvent.eventType();
                Object data = acmedcareEvent.data();

                System.out.println(event);
                System.out.println(JSON.toJSONString(data));
              }
            });

    try {
      AcmedcareRemoting.getInstance().run(1000);

    } catch (NoServerAddressException e) {
      e.printStackTrace();
    }

    new Thread(
            new Runnable() {
              @Override
              public void run() {

                for (; ; ) {
                  System.out.println(
                      "当前服务器地址:" + AcmedcareRemoting.getInstance().getCurrentRemotingAddress());

                  System.out.println(
                      "当前登录用户:" + AcmedcareRemoting.getInstance().getCurrentLoginName());

                  try {
                    Thread.sleep(5000);
                  } catch (InterruptedException e) {
                    e.printStackTrace();
                  }
                }
              }
            })
        .start();

    System.out.println("....");
  }
}
