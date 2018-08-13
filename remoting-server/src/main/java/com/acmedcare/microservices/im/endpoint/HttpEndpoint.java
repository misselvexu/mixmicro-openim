package com.acmedcare.microservices.im.endpoint;

import com.acmedcare.microservices.im.RemotingApplication.Datas;
import com.acmedcare.microservices.im.RemotingApplication.Ids;
import com.acmedcare.microservices.im.biz.bean.Message;
import com.acmedcare.microservices.im.biz.bean.Message.GroupMessage;
import com.acmedcare.microservices.im.biz.bean.Message.InnerType;
import com.acmedcare.microservices.im.biz.bean.Message.MessageType;
import com.acmedcare.microservices.im.biz.bean.Message.SingleMessage;
import com.acmedcare.microservices.im.core.ServerFacade;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import java.nio.charset.Charset;
import java.util.Date;
import java.util.List;
import java.util.Set;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

/**
 * Main Biz Http Endpoint Api
 *
 * @author Elve.Xu [iskp.me<at>gmail.com]
 * @version v1.0 - 09/08/2018.
 */
@RestController
@Api(value = "Remote IM Server Http Apis", tags = "IM通讯服务器HTTP接口")
public class HttpEndpoint {

  private static final Logger LOGGER = LoggerFactory.getLogger(HttpEndpoint.class);

  /**
   * Create Room Request
   *
   * @return response
   */
  @RequestMapping(value = "/group_open_http_svc/create_group", method = RequestMethod.POST)
  @ApiOperation(value = "新建群组接口", httpMethod = "POST", response = TencentResult.class)
  public TencentResult newRoom(@RequestBody String body) {
    try {
      LOGGER.debug("Client Request Body : {}", body);

      JSONObject object = JSONObject.parseObject(body);

      Set<String> members = Sets.newHashSet();

      String groupId = object.getString("GroupId");
      // 解析群成员
      if (object.containsKey("MemberList")) {
        JSONArray array = object.getJSONArray("MemberList");
        for (Object o : array) {
          JSONObject temp = (JSONObject) o;
          if (temp.containsKey("Member_Account")) {
            members.add(temp.getString("Member_Account"));
          }
        }
      }

      String owner = null;
      if (object.containsKey("Owner_Account")) {
        owner = object.getString("Owner_Account");
        if (StringUtils.isNoneBlank(owner)) {
          if (!members.contains(owner)) {
            members.add(owner);
          }
        }
      }

      // 解析群名称
      String groupName = object.getString("Name");

      // do
      Datas.persistenceExecutor.newGroup(groupId, groupName, owner, members);

      return TencentResult.SUCCESS;

    } catch (Exception e) {
      return TencentResult.builder()
          .errorCode(-1)
          .actionStatus("FAIL")
          .errorInfo(e.getMessage())
          .build();
    }
  }

  /**
   * 新增群成员
   *
   * @param body tencent json
   * @return result
   */
  @RequestMapping(value = "/group_open_http_svc/add_group_member", method = RequestMethod.POST)
  @ApiOperation(value = "新增群成员", httpMethod = "POST", response = TencentResult.class)
  public TencentResult addGroupMember(@RequestBody String body) {

    try {

      LOGGER.debug("Client Request Body : {}", body);

      JSONObject object = JSONObject.parseObject(body);

      // 新增群成员
      Set<String> members = Sets.newHashSet();

      // 解析 group ID
      String groupId = object.getString("GroupId");

      JSONArray array = object.getJSONArray("MemberList");

      for (Object o : array) {
        JSONObject temp = (JSONObject) o;
        if (temp.containsKey("Member_Account")) {
          members.add(temp.getString("Member_Account"));
        }
      }

      Datas.persistenceExecutor.addGroupMember(groupId, members);

      return TencentResult.SUCCESS;
    } catch (Exception e) {
      return TencentResult.builder()
          .errorCode(-1)
          .actionStatus("FAIL")
          .errorInfo(e.getMessage())
          .build();
    }
  }

  /**
   * 删除群成员
   *
   * @param body tencent json
   * @return result
   */
  @RequestMapping(value = "/group_open_http_svc/delete_group_member", method = RequestMethod.POST)
  @ApiOperation(value = "新增群成员", httpMethod = "POST", response = TencentResult.class)
  public TencentResult delGroupMember(@RequestBody String body) {

    try {

      LOGGER.debug("Client Request Body : {}", body);

      JSONObject object = JSONObject.parseObject(body);

      // 新增群成员
      List<String> members = Lists.newArrayList();

      // 解析 group ID
      String groupId = object.getString("GroupId");

      JSONArray array = object.getJSONArray("MemberToDel_Account");

      for (Object o : array) {
        members.add((String) o);
      }

      Datas.persistenceExecutor.deleteGroupMember(groupId, members);

      return TencentResult.SUCCESS;

    } catch (Exception e) {
      return TencentResult.builder()
          .errorCode(-1)
          .actionStatus("FAIL")
          .errorInfo(e.getMessage())
          .build();
    }
  }

  /**
   * 发送单消息
   *
   * @param body 消息内容
   * @return result
   */
  @RequestMapping(value = "/openim/sendmsg", method = RequestMethod.POST)
  @ApiOperation(value = "发送单聊消息", httpMethod = "POST", response = TencentResult.class)
  public TencentResult sendSingleMessage(@RequestBody String body) {

    try {
      LOGGER.debug("Client Request Body : {}", body);

      JSONObject object = JSONObject.parseObject(body);

      List<String> members = Lists.newArrayList();

      List<Message> messages = Lists.newLinkedList();

      if (object.containsKey("To_Account")) {
        members.add(object.getString("To_Account"));
      }

      String fromAccount = object.getString("From_Account");

      // 解析消息类型
      if (object.containsKey("MsgBody")) {

        JSONArray msgArray = object.getJSONArray("MsgBody");

        for (Object o : msgArray) {

          JSONObject msg = (JSONObject) o;

          String messageType = msg.getString("MsgType");

          if (msg.containsKey("MsgContent")) {

            InnerType innerType = null;
            JSONObject contentJson = msg.getJSONObject("MsgContent");
            String content = "";

            if ("TIMTextElem".equals(messageType)) {
              innerType = InnerType.NORMAL;
              content = contentJson.getString("Text");
            }

            if ("TIMCustomElem".equals(messageType)) {
              innerType = InnerType.COMMAND;
              content = contentJson.getString("Data");
            }

            Message message =
                SingleMessage.builder()
                    .mid(Ids.idHelper.nextId())
                    .sender(fromAccount)
                    .sendTimestamp(new Date())
                    .body(content.getBytes(Charset.defaultCharset()))
                    .messageType(MessageType.SINGLE)
                    .innerType(innerType)
                    .receiver(members.get(0))
                    .build();

            // add
            messages.add(message);
          }
        }
      }

      // save and push
      //      Datas.persistenceExecutor.saveMessage(messages.toArray(new Message[] {}));

      // send
      ServerFacade.Executor.sendMessageAsync(messages);

      return TencentResult.SUCCESS;

    } catch (Exception e) {
      return TencentResult.builder()
          .errorCode(-1)
          .actionStatus("FAIL")
          .errorInfo(e.getMessage())
          .build();
    }
  }

  /**
   * 批量发送单聊消息
   *
   * @param body 消息内容
   * @return result
   */
  @RequestMapping(value = "/openim/batchsendmsg", method = RequestMethod.POST)
  @ApiOperation(value = "发送单聊消息", httpMethod = "POST", response = TencentResult.class)
  public TencentResult sendMessages(@RequestBody String body) {

    try {
      LOGGER.debug("Client Request Body : {}", body);

      JSONObject object = JSONObject.parseObject(body);

      Set<String> members = Sets.newHashSet();

      List<Message> messages = Lists.newLinkedList();

      if (object.containsKey("To_Account")) {
        JSONArray accountArray = object.getJSONArray("To_Account");
        for (Object o : accountArray) {
          members.add((String) o);
        }
      }

      // 发送者
      String fromAccount = object.getString("From_Account");

      // 解析消息类型
      if (object.containsKey("MsgBody")) {

        JSONArray msgArray = object.getJSONArray("MsgBody");

        for (Object o : msgArray) {

          JSONObject msg = (JSONObject) o;

          String messageType = msg.getString("MsgType");

          if (msg.containsKey("MsgContent")) {

            InnerType innerType = null;
            JSONObject contentJson = msg.getJSONObject("MsgContent");
            String content = "";

            if ("TIMTextElem".equals(messageType)) {
              innerType = InnerType.NORMAL;
              content = contentJson.getString("Text");
            }

            if ("TIMCustomElem".equals(messageType)) {
              innerType = InnerType.COMMAND;
              content = contentJson.getString("Data");
            }

            // 批量发送单人消息
            for (String temp : members) {
              SingleMessage message =
                  SingleMessage.builder()
                      .mid(Ids.idHelper.nextId())
                      .sender(fromAccount)
                      .sendTimestamp(new Date())
                      .body(content.getBytes(Charset.defaultCharset()))
                      .messageType(MessageType.SINGLE)
                      .innerType(innerType)
                      .receiver(temp)
                      .build();
              messages.add(message);
            }
          }
        }
      }

      // save and push
      //      Datas.persistenceExecutor.saveMessage(messages.toArray(new Message[] {}));

      // send
      ServerFacade.Executor.sendMessageAsync(messages);

      return TencentResult.SUCCESS;

    } catch (Exception e) {
      return TencentResult.builder()
          .errorCode(-1)
          .actionStatus("FAIL")
          .errorInfo(e.getMessage())
          .build();
    }
  }

  /**
   * 在群组中发送普通消息
   *
   * @param body 消息内容
   * @return result
   */
  @RequestMapping(value = "/group_open_http_svc/send_group_msg", method = RequestMethod.POST)
  @ApiOperation(value = "在群组中发送普通消息", httpMethod = "POST", response = TencentResult.class)
  public TencentResult sendGroupMessages(@RequestBody String body) {

    try {
      LOGGER.debug("Client Request Body : {}", body);

      JSONObject object = JSONObject.parseObject(body);

      List<Message> messages = Lists.newLinkedList();

      // 群组标识
      String groupId = object.getString("GroupId");

      // 发送者
      String fromAccount = object.getString("From_Account");

      // 解析消息类型
      if (object.containsKey("MsgBody")) {

        JSONArray msgArray = object.getJSONArray("MsgBody");

        for (Object o : msgArray) {

          JSONObject msg = (JSONObject) o;

          String messageType = msg.getString("MsgType");

          // 只处理文本消息和自定义消息
          if (StringUtils.equalsAny(messageType, "TIMTextElem", "TIMCustomElem")) {

            if (msg.containsKey("MsgContent")) {

              InnerType innerType = null;
              JSONObject contentJson = msg.getJSONObject("MsgContent");
              String content = "";

              if ("TIMTextElem".equals(messageType)) {
                innerType = InnerType.NORMAL;
                content = contentJson.getString("Text");
              }

              if ("TIMCustomElem".equals(messageType)) {
                innerType = InnerType.COMMAND;
                content = contentJson.getString("Data");
              }

              GroupMessage message =
                  GroupMessage.builder()
                      .mid(Ids.idHelper.nextId())
                      .sender(fromAccount)
                      .sendTimestamp(new Date())
                      .body(content.getBytes(Charset.defaultCharset()))
                      .messageType(MessageType.GROUP)
                      .innerType(innerType)
                      .group(groupId)
                      .build();

              // add
              messages.add(message);
            }
          }
        }
      }

      // save and push
      //      Datas.persistenceExecutor.saveMessage(messages.toArray(new Message[] {}));

      // send
      ServerFacade.Executor.sendMessageAsync(messages);

      return TencentResult.SUCCESS;

    } catch (Exception e) {
      return TencentResult.builder()
          .errorCode(-1)
          .actionStatus("FAIL")
          .errorInfo(e.getMessage())
          .build();
    }
  }
}
