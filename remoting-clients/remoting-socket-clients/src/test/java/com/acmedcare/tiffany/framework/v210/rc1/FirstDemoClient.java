package com.acmedcare.tiffany.framework.v210.rc1;

import com.acmedcare.nas.client.NasProperties;
import com.acmedcare.tiffany.framework.remoting.android.core.xlnio.XLMRRemotingClient;
import com.acmedcare.tiffany.framework.remoting.jlib.AcmedcareLogger;
import com.acmedcare.tiffany.framework.remoting.jlib.AcmedcareRemoting;
import com.acmedcare.tiffany.framework.remoting.jlib.AcmedcareRemoting.RemotingConnectListener;
import com.acmedcare.tiffany.framework.remoting.jlib.RemotingParameters;
import com.acmedcare.tiffany.framework.remoting.jlib.ServerAddressHandler;
import com.acmedcare.tiffany.framework.remoting.jlib.biz.bean.Group;
import com.acmedcare.tiffany.framework.remoting.jlib.biz.bean.Message;
import com.acmedcare.tiffany.framework.remoting.jlib.biz.bean.Message.GroupMessage;
import com.acmedcare.tiffany.framework.remoting.jlib.biz.bean.Message.InnerType;
import com.acmedcare.tiffany.framework.remoting.jlib.biz.bean.Message.MessageType;
import com.acmedcare.tiffany.framework.remoting.jlib.biz.bean.Message.SingleMessage;
import com.acmedcare.tiffany.framework.remoting.jlib.biz.request.AuthRequest.AuthCallback;
import com.acmedcare.tiffany.framework.remoting.jlib.biz.request.PullMessageRequest;
import com.acmedcare.tiffany.framework.remoting.jlib.biz.request.PullMessageRequest.Callback;
import com.acmedcare.tiffany.framework.remoting.jlib.biz.request.PullOwnerGroupListRequest;
import com.acmedcare.tiffany.framework.remoting.jlib.biz.request.PushMessageRequest;
import com.acmedcare.tiffany.framework.remoting.jlib.events.AcmedcareEvent;
import com.acmedcare.tiffany.framework.remoting.jlib.events.AcmedcareEvent.Event;
import com.acmedcare.tiffany.framework.remoting.jlib.events.BasicListenerHandler;
import com.acmedcare.tiffany.framework.remoting.jlib.exception.NoServerAddressException;
import com.acmedcare.tiffany.framework.remoting.jlib.exception.SdkInitException;
import com.alibaba.fastjson.JSON;
import com.google.common.base.Charsets;
import com.google.common.collect.Lists;
import java.io.BufferedReader;
import java.io.File;
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

    NasProperties nasProperties = new NasProperties();
    nasProperties.setServerAddrs(Lists.<String>newArrayList("127.0.0.1:18848"));
    nasProperties.setHttps(false);

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
            .jksFile(
                new File(
                    "/Users/ive/git-acmedcare/Acmedcare-NewIM/remoting-certs/client/keystore.jks"))
            .jksPassword("1qaz2wsx")
            .username(KnownParams.passport)
            .nasProperties(nasProperties)
            .accessToken(KnownParams.accessToken)
            .areaNo(KnownParams.areaNo)
            .orgId(KnownParams.orgId)
            .passportId(KnownParams.passportId)
            .deviceId(KnownParams.deviceId)
            .heartbeatPeriod(10)
            .serverAddressHandler(
                new ServerAddressHandler() {
                  @Override
                  public List<RemotingAddress> remotingAddressList() {
                    return Lists.newArrayList(
                        new RemotingAddress(false, "127.0.0.1", 13110, false));
                  }
                })
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
    } catch (SdkInitException e) {
      e.printStackTrace();
    }

    while (true) {

      try {
        System.out.print("> ");
        String input =
            new BufferedReader(new InputStreamReader(System.in, Charsets.UTF_8)).readLine();
        if (input == null || input.length() == 0) {
          continue;
        }

        input = input.trim();

        // functions
        String[] inputArgs = input.split("\\s+");

        // 拉取消息请求参数:  pullMessageList 3837142362366977 -1
        if (inputArgs[0].equals("pullMessageList")) {
          pullMessageList(inputArgs[1], Long.parseLong(inputArgs[2]));
          continue;
        }

        // 拉取消息请求参数:  pullMessageList2 gid-20181122 -1
        if (inputArgs[0].equals("pullMessageList2")) {
          pullMessageList2(inputArgs[1], Long.parseLong(inputArgs[2]));
          continue;
        }

        // 拉取消息请求参数:  pullOwnGroupList
        if (inputArgs[0].equals("pullOwnGroupList")) {
          pullOwnGroupList();
          continue;
        }

        // 发送消息
        // 单聊消息: sendMessage SINGLE 3837142362366977 hi
        // 群消息: sendMessage GROUP gid-20181122  {"em_message_type":"em_zl_message","taskcode":"123"}
        if (inputArgs[0].equals("sendMessage")) {

          if ("SINGLE".equals(inputArgs[1])) {
            SingleMessage singleMessage = new SingleMessage();
            singleMessage.setReceiver(inputArgs[2]);
            singleMessage.setBody(inputArgs[3].getBytes());
            singleMessage.setInnerType(InnerType.NORMAL);
            singleMessage.setMessageType(MessageType.SINGLE);
            singleMessage.setSender(KnownParams.passportId.toString());

            sendMessage(singleMessage);
          }

          if ("GROUP".equals(inputArgs[1])) {
            GroupMessage groupMessage = new GroupMessage();
            groupMessage.setSender(KnownParams.passportId.toString());
            groupMessage.setGroup(inputArgs[2]);
            groupMessage.setBody(inputArgs[3].getBytes());
            groupMessage.setMessageType(MessageType.GROUP);

            sendMessage(groupMessage);
          }

          continue;
        }

        // 单聊消息: sendMediaMessage SINGLE 3837142362366977
        // /Users/ive/git-acmedcare/Acmedcare-NewIM/COMMAND.md
        // 群消息: sendMediaMessage GROUP gid-20181122
        // /Users/ive/git-acmedcare/Acmedcare-NewIM/COMMAND.md
        if (inputArgs[0].equals("sendMediaMessage")) {

          if ("SINGLE".equals(inputArgs[1])) {
            SingleMessage singleMessage = new SingleMessage();
            singleMessage.setReceiver(inputArgs[2]);
            singleMessage.setBody("...".getBytes());
            singleMessage.setInnerType(InnerType.MEDIA);
            singleMessage.setMessageType(MessageType.SINGLE);
            singleMessage.setSender(KnownParams.passportId.toString());
            sendMediaMessage(singleMessage, inputArgs[3]);
          }

          if ("GROUP".equals(inputArgs[1])) {
            GroupMessage groupMessage = new GroupMessage();
            groupMessage.setGroup(inputArgs[2]);
            groupMessage.setBody("...".getBytes());
            groupMessage.setInnerType(InnerType.MEDIA);
            groupMessage.setMessageType(MessageType.GROUP);
            groupMessage.setSender(KnownParams.passportId.toString());
            sendMediaMessage(groupMessage, inputArgs[3]);
          }

          continue;
        }

        if (input.equalsIgnoreCase("quit")) {
          System.exit(0);
        }
      } catch (Exception ignored) {
        ignored.printStackTrace();
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
            new Callback<SingleMessage>() {
              @Override
              public void onSuccess(List<SingleMessage> messages) {
                System.out.println("拉取消息返回值: " + JSON.toJSONString(messages));
              }

              @Override
              public void onFailed(int code, String message) {
                System.out.println("拉取消息列表失败,Code = " + code + ", Message = " + message);
              }
            });
  }

  /** 拉取消息列表(群组) */
  private static void pullMessageList2(String sender, long leastMessageId) {
    PullMessageRequest pullMessageRequest = new PullMessageRequest();
    pullMessageRequest.setLeastMessageId(leastMessageId);
    pullMessageRequest.setLimit(10);
    pullMessageRequest.setPassportId(KnownParams.passportId.toString());
    pullMessageRequest.setSender(sender);
    pullMessageRequest.setType(1);
    pullMessageRequest.setUsername(KnownParams.passport);

    AcmedcareRemoting.getInstance()
        .executor()
        .pullMessage(
            pullMessageRequest,
            new Callback<SingleMessage>() {
              @Override
              public void onSuccess(List<SingleMessage> messages) {
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

  /**
   * 发送消息
   *
   * @param message
   */
  private static void sendMessage(Message message) {

    PushMessageRequest pushMessageRequest = new PushMessageRequest();
    pushMessageRequest.setPassport(KnownParams.passport);
    pushMessageRequest.setMessage(message);
    pushMessageRequest.setMessageType(message.getMessageType().name());
    pushMessageRequest.setPassportId(KnownParams.passportId.toString());

    AcmedcareRemoting.getInstance()
        .executor()
        .pushMessage(
            pushMessageRequest,
            new PushMessageRequest.Callback() {
              @Override
              public void onSuccess(long messageId) {
                System.out.println("发送消息成功, 生成的消息编号:" + messageId);
              }

              @Override
              public void onFailed(int code, String message) {
                System.out.println("发送消息失败,Code = " + code + ", Message = " + message);
              }
            });
  }

  private static void sendMediaMessage(Message message, String fileName) {

    PushMessageRequest pushMessageRequest = new PushMessageRequest();
    pushMessageRequest.setPassport(KnownParams.passport);
    pushMessageRequest.setMessage(message);
    pushMessageRequest.setMessageType(message.getMessageType().name());
    pushMessageRequest.setPassportId(KnownParams.passportId.toString());
    pushMessageRequest.setFile(new File(fileName));

    AcmedcareRemoting.getInstance()
        .executor()
        .pushMessage(
            pushMessageRequest,
            new PushMessageRequest.Callback() {
              @Override
              public void onSuccess(long messageId) {
                System.out.println("发送消息成功, 生成的消息编号:" + messageId);
              }

              @Override
              public void onFailed(int code, String message) {
                System.out.println("发送消息失败,Code = " + code + ", Message = " + message);
              }
            });
  }

  /**
   * 已知参数
   *
   * @author <a href="mailto:iskp.me@gmail.com">Elve.Xu</a>
   * @version ${project.version} - 27/11/2018.
   */
  private interface KnownParams {

    String accessToken =
        "eyJhbGciOiJSUzI1NiJ9.eyJfaWQiOiI3ZjkyNjcyYTZlMmU0MjY0OWE3YmY0ODAzMmQ0MzVhZCIsImRhdCI6Ik4vQmtqTkJBelh0Y04rZDdKRExrVU5OOWNXU2JQWDlId29hV0RYN1B1UElzZ1BSMlNvbS9JK09kWWpWK0hJS0pwWG9ja2Vvb1o3eVZ4a0YydnZweDJtTHA1YVJrOE5FanZrZyszbU8rZXczNmpoaEFkQ1YvVFhhTWNKQ1lqZDhCd1YrMW13T1pVdjJPVzhGZ2tPOERKVmo5bWhKeDMxZ0tIMUdPdmowanA4ST0iLCJpYXQiOjE1NDM3MzYzNzQ0NDgsImV4cCI6MTU0NDM1MTA0ODQ0OCwiYXVkIjpudWxsfQ.maY697jhev5c7tYv1XpS1aifkwn-HrUhKEFYWkuJmmu_8m91xJVsPIrHq_hfHj8gokirLsBWANvvVTMcUc29tcpuW4U0j7bFx86cBv9AJmpMG0O90zl_mejPbQle-iooOhchejoRsQk2e9infekmU-Y6xV8ClCgJWQR9Mk_5vdp4qPY-KXd0iTS-yKHEgCC3LOv-2Uq72vY0rhnOYSWDy0KD3Iy4JLMoDF2rlzkO1PEwBVlhqWCbveO500YyGqeRZ_BlWhzXglUDEK4wVqn43UNIuEIv-TmTWSia5daWB47lPPuPudDMgsaSUozcqwIhUIk2Q9dGJ60UAy3Lvy0rRQ";

    String areaNo = "320500";

    String orgId = "3910249034228736";

    Long passportId = 3837142362366976L;

    String passport = "13910187669";

    String deviceId = "DEVICE-ID";
  }

  //  private interface KnownParams {
  //
  //    String accessToken =
  //
  // "eyJhbGciOiJSUzI1NiJ9.eyJfaWQiOiIyYjRmMzBhZTdkZGE0ZGY1ODY5ZGUzNWYxM2EyOTdiZiIsImRhdCI6Ik4vQmtqTkJBelh1R1lyT1JNL245UUZGclFmQkluTlFkTlhIYm1rdUF6TCtKSldsRVpySm9wQk12MFM5YzZvWTV1Z1NTckRsclNoQjE1c25uT0NIZ0hlRVhDZ3lLcG5hd2ZIRHIvZDl5Yk9UL1ErSEJRUmJGeG8zRWRBL2c1NlZRMEFVSHVVa3Jib2liSkhoYm5yV2FZV2RKdDREM0dBbXRqcEMxUnBlb0hPOD0iLCJpYXQiOjE1NDM0NzY4MjExOTEsImV4cCI6MTU0NDA4ODExNjE5MSwiYXVkIjpudWxsfQ.ncrqSwJGQm473dNxD60r78H0OncHznrGOl9c3rj00WadsU--Ezq3nG6OxxkKIJZrNwwnUDoBQdlmGcoF_HJoBTUPkn6jfMpqk67rWtdJgBbv6WySQSB7MI8Hu-UFqp4INinFVCy7l1doacGsoibIpjiFXk4bkKANc7iCgZH_piGPZc906BbjwvMHT9nvFVpOCE7DrMLPPPR61GRYRGsJ5q2KvOFrTetCTeR7aiSaoHmlZwXBuIQnfmCUX604yuLFQKnFF7Z2cuuHqkdpSFgxdECc3RUtwa8ait-h7QPLIqRfuZHSsx56sS_22gGPv_SMKmj-fz8TiKIk_QiwJQgCAg";
  //
  //    String areaNo = "451300";
  //
  //    String orgId = "999190104901888";
  //
  //    Long passportId = 1033050009520384L;
  //
  //    String passport = "johnOuthos002";
  //
  //    String deviceId = "DEVICE-ID";
  //  }
}
