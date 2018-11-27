package com.acmedcare.tiffany.framework;

import com.acmedcare.tiffany.framework.remoting.android.core.xlnio.XLMRRemotingClient;
import com.acmedcare.tiffany.framework.remoting.jlib.AcmedcareRemoting;
import com.acmedcare.tiffany.framework.remoting.jlib.AcmedcareRemoting.RemotingConnectListener;
import com.acmedcare.tiffany.framework.remoting.jlib.RemotingParameters;
import com.acmedcare.tiffany.framework.remoting.jlib.ServerAddressHandler;
import com.acmedcare.tiffany.framework.remoting.jlib.biz.bean.Message.SingleMessage;
import com.acmedcare.tiffany.framework.remoting.jlib.biz.request.AuthRequest.AuthCallback;
import com.acmedcare.tiffany.framework.remoting.jlib.events.AcmedcareEvent;
import com.acmedcare.tiffany.framework.remoting.jlib.events.AcmedcareEvent.Event;
import com.acmedcare.tiffany.framework.remoting.jlib.events.BasicListenerHandler;
import com.acmedcare.tiffany.framework.remoting.jlib.exception.NoServerAddressException;
import com.google.common.collect.Lists;
import java.io.UnsupportedEncodingException;
import java.util.List;

/**
 * com.acmedcare.tiffany.framework
 *
 * @author <a href="mailto:iskp.me@gmail.com">Elve.Xu</a>
 * @version v1.0 - 11/08/2018.
 */
public class ChangeAccountTester {

  static void login(String username) {

    RemotingParameters temp =
        RemotingParameters.builder()
            .authCallback(
                new AuthCallback() {
                  @Override
                  public void onSuccess() {}

                  @Override
                  public void onFailed(int code, String message) {}
                })
            .username(username)
            .heartbeatPeriod(10)
            .serverAddressHandler(
                new ServerAddressHandler() {
                  @Override
                  public List<RemotingAddress> remotingAddressList() {
                    return Lists.newArrayList(
                        new RemotingAddress(false, "192.168.1.39", 8887, false));
                  }
                })
            .build();

    AcmedcareRemoting.getInstance().init(null, temp);

    AcmedcareRemoting.getInstance()
        .registerConnectionEventListener(
            new RemotingConnectListener() {
              @Override
              public void onConnect(XLMRRemotingClient client) {
                System.out.println("onConnect");
              }

              @Override
              public void onClose(XLMRRemotingClient client) {
                System.out.println("onClose");
                AcmedcareRemoting.getInstance().shutdownNow();
              }

              @Override
              public void onException(XLMRRemotingClient client) {
                System.out.println("onException");
              }

              @Override
              public void onIdle(XLMRRemotingClient client) {
                System.out.println("onIdle");
              }
            });
    AcmedcareRemoting.getInstance()
        .onMessageEventListener(
            new BasicListenerHandler() {
              @Override
              public void execute(AcmedcareEvent acmedcareEvent) {
                Event event = acmedcareEvent.eventType();
                Object data = acmedcareEvent.data();

                System.out.println(event);
                System.out.println(data);
                if (data instanceof SingleMessage) {
                  SingleMessage singleMessage = (SingleMessage) data;
                  try {
                    System.out.println(new String(singleMessage.getBody(), "UTF-8"));
                  } catch (UnsupportedEncodingException e) {
                    e.printStackTrace();
                  }
                }
              }
            });

    try {
      AcmedcareRemoting.getInstance().run(1000);

    } catch (NoServerAddressException e) {
      e.printStackTrace();
    }
  }

  public static void main(String[] args) {

    login("demo");

    new Thread(
            new Runnable() {
              @Override
              public void run() {

                try {
                  Thread.sleep(20 * 1000);
                } catch (InterruptedException e) {
                  e.printStackTrace();
                }

                AcmedcareRemoting.getInstance().shutdownNow();
                System.out.println("准备切换用户");

                try {
                  Thread.sleep(5 * 1000);
                } catch (InterruptedException e) {
                  e.printStackTrace();
                }

                login("demo1");

                //                for (; ; ) {
                //                  System.out.println(
                //                      "当前服务器地址:" +
                // AcmedcareRemoting.getInstance().getCurrentRemotingAddress());
                //
                //                  System.out.println(
                //                      "当前登录用户:" +
                // AcmedcareRemoting.getInstance().getCurrentLoginName());
                //
                //                  try {
                //                    Thread.sleep(5000);
                //                  } catch (InterruptedException e) {
                //                    e.printStackTrace();
                //                  }
                //                }
              }
            })
        .start();

    System.out.println("....");
  }
}
