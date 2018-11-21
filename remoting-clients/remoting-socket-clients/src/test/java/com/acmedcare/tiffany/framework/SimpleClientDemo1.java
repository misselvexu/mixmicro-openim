package com.acmedcare.tiffany.framework;

import com.acmedcare.tiffany.framework.remoting.android.core.xlnio.XLMRRemotingClient;
import com.acmedcare.tiffany.framework.remoting.jlib.AcmedcareLogger;
import com.acmedcare.tiffany.framework.remoting.jlib.AcmedcareRemoting;
import com.acmedcare.tiffany.framework.remoting.jlib.AcmedcareRemoting.RemotingConnectListener;
import com.acmedcare.tiffany.framework.remoting.jlib.RemotingParameters;
import com.acmedcare.tiffany.framework.remoting.jlib.ServerAddressHandler;
import com.acmedcare.tiffany.framework.remoting.jlib.biz.bean.Session;
import com.acmedcare.tiffany.framework.remoting.jlib.biz.request.AuthRequest.AuthCallback;
import com.acmedcare.tiffany.framework.remoting.jlib.biz.request.PullSessionListRequest;
import com.acmedcare.tiffany.framework.remoting.jlib.biz.request.PullSessionListRequest.Callback;
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
public class SimpleClientDemo1 {

  public static void main(String[] args) {

    System.setProperty(AcmedcareLogger.NON_ANDROID_FLAG, "true");

    RemotingParameters temp =
        RemotingParameters.builder()
            .authCallback(
                new AuthCallback() {
                  @Override
                  public void onSuccess() {
                    System.out.println("授权成功");
                  }

                  @Override
                  public void onFailed(int code, String message) {
                    System.out.println("授权失败,Code=" + code + ", 错误:" + message);
                  }
                })
            .enableSSL(true)
            .username("demo")
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
        .registerConnectionEventListener(
            new RemotingConnectListener() {
              @Override
              public void onConnect(XLMRRemotingClient client) {}

              @Override
              public void onClose(XLMRRemotingClient client) {
                System.out.println("连接断开,等待 SDK 重连");
              }

              @Override
              public void onException(XLMRRemotingClient client) {}

              @Override
              public void onIdle(XLMRRemotingClient client) {}
            });

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
    //
    //    new Thread(
    //            new Runnable() {
    //              @Override
    //              public void run() {
    //
    //                for (; ; ) {
    //                  System.out.println(
    //                      "当前服务器地址:" +
    // AcmedcareRemoting.getInstance().getCurrentRemotingAddress());
    //
    //                  System.out.println(
    //                      "当前登录用户:" + AcmedcareRemoting.getInstance().getCurrentLoginName());
    //
    //                  try {
    //                    Thread.sleep(5000);
    //                  } catch (InterruptedException e) {
    //                    e.printStackTrace();
    //                  }
    //                }
    //              }
    //            })
    //        .start();

    new Thread(
            new Runnable() {
              @Override
              public void run() {

                try {
                  Thread.sleep(20000);

                  AcmedcareRemoting.getInstance()
                      .executor()
                      .pullOwnerSessionList(
                          PullSessionListRequest.builder().username("demo").build(),
                          new Callback() {
                            @Override
                            public void onSuccess(List<Session> list) {
                              System.out.println(list);
                            }

                            @Override
                            public void onFailed(int code, String message) {
                              System.out.println("code=" + code + " ,message = " + message);
                            }
                          });

                } catch (InterruptedException e) {
                  e.printStackTrace();
                }
              }
            })
        .start();

    System.out.println("....");
  }
}
