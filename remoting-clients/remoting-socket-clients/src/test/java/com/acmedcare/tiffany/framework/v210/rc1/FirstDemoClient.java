package com.acmedcare.tiffany.framework.v210.rc1;

import com.acmedcare.tiffany.framework.remoting.android.core.xlnio.XLMRRemotingClient;
import com.acmedcare.tiffany.framework.remoting.jlib.AcmedcareLogger;
import com.acmedcare.tiffany.framework.remoting.jlib.AcmedcareRemoting;
import com.acmedcare.tiffany.framework.remoting.jlib.AcmedcareRemoting.RemotingConnectListener;
import com.acmedcare.tiffany.framework.remoting.jlib.RemotingParameters;
import com.acmedcare.tiffany.framework.remoting.jlib.ServerAddressHandler.RemotingAddress;
import com.acmedcare.tiffany.framework.remoting.jlib.biz.bean.Group;
import com.acmedcare.tiffany.framework.remoting.jlib.biz.bean.Message;
import com.acmedcare.tiffany.framework.remoting.jlib.biz.request.AuthRequest.AuthCallback;
import com.acmedcare.tiffany.framework.remoting.jlib.biz.request.PullMessageRequest;
import com.acmedcare.tiffany.framework.remoting.jlib.biz.request.PullMessageRequest.Callback;
import com.acmedcare.tiffany.framework.remoting.jlib.biz.request.PullOwnerGroupListRequest;
import com.acmedcare.tiffany.framework.remoting.jlib.events.AcmedcareEvent;
import com.acmedcare.tiffany.framework.remoting.jlib.events.AcmedcareEvent.Event;
import com.acmedcare.tiffany.framework.remoting.jlib.events.BasicListenerHandler;
import com.acmedcare.tiffany.framework.remoting.jlib.exception.NoServerAddressException;
import com.alibaba.fastjson.JSON;
import com.google.common.base.Charsets;
import com.google.common.collect.Lists;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.List;

/**
 * com.acmedcare.tiffany.framework
 *
 * @author <a href="mailto:iskp.me@gmail.com">Elve.Xu</a>
 * @version v1.0 - 11/08/2018.
 */
public class FirstDemoClient {

  public static void main(String[] args) throws IOException {

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
            .username(KnownParams.passport)
            .accessToken(KnownParams.accessToken)
            .areaNo(KnownParams.areaNo)
            .orgId(KnownParams.orgId)
            .passportId(KnownParams.passportId)
            .deviceId(KnownParams.deviceId)
            .heartbeatPeriod(10)
            .serverAddressHandler(
                () -> Lists.newArrayList(new RemotingAddress(false, "127.0.0.1", 13110, false)))
            .build();

    AcmedcareRemoting.getInstance().init(null, temp);

    AcmedcareRemoting.getInstance()
        .registerConnectionEventListener(
            new RemotingConnectListener() {
              @Override
              public void onConnect(XLMRRemotingClient client) {
                System.out.println("连接已打开");
              }

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

    while (true) {
      System.out.print("> ");
      String input =
          new BufferedReader(new InputStreamReader(System.in, Charsets.UTF_8)).readLine();
      if (input == null || input.length() == 0) {
        continue;
      }

      input = input.trim();

      // functions
      String[] inputArgs = input.split("\\s+");

      // 拉取消息请求参数:  pullMessageList test -1
      if (inputArgs[0].equals("pullMessageList")) {
        pullMessageList(inputArgs[0], Long.parseLong(inputArgs[1]));
        continue;
      }

      // 拉取消息请求参数:  pullOwnGroupList
      if (inputArgs[0].equals("pullOwnGroupList")) {
        pullOwnGroupList();
        continue;
      }

      if (input.equalsIgnoreCase("quit")) {
        System.exit(0);
      }
    }
  }

  /** 拉取消息列表(个人) */
  private static void pullMessageList(String sender, long leastMessageId) {
    PullMessageRequest pullMessageRequest = new PullMessageRequest();
    pullMessageRequest.setLeastMessageId(leastMessageId);
    pullMessageRequest.setLimit(10);
    pullMessageRequest.setPassportId(KnownParams.passportId.toString());
    pullMessageRequest.setSender(sender);
    pullMessageRequest.setType(0);
    pullMessageRequest.setUsername(KnownParams.passport);

    AcmedcareRemoting.getInstance()
        .executor()
        .pullMessage(
            pullMessageRequest,
            new Callback() {
              @Override
              public void onSuccess(List<Message> messages) {
                System.out.println("拉取消息返回值: " + JSON.toJSONString(messages));
              }

              @Override
              public void onFailed(int code, String message) {
                System.out.println("拉取消息列表失败,Code = " + code + ", Message = " + message);
              }
            });
  }

  /** 拉取群组列表 */
  private static void pullOwnGroupList() {
    PullOwnerGroupListRequest pullOwnerGroupListRequest = new PullOwnerGroupListRequest();
    pullOwnerGroupListRequest.setPassport(KnownParams.passport);
    pullOwnerGroupListRequest.setPassportId(KnownParams.passportId.toString());
    AcmedcareRemoting.getInstance()
        .executor()
        .pullOwnerGroupList(
            pullOwnerGroupListRequest,
            new PullOwnerGroupListRequest.Callback() {
              @Override
              public void onSuccess(List<Group> groups) {
                System.out.println("拉取群组返回值: " + JSON.toJSONString(groups));
              }

              @Override
              public void onFailed(int code, String message) {
                System.out.println("拉取群组列表失败,Code = " + code + ", Message = " + message);
              }
            });
  }
}
