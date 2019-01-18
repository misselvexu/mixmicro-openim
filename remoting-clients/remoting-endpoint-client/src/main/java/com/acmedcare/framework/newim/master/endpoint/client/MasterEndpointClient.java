package com.acmedcare.framework.newim.master.endpoint.client;

import com.acmedcare.framework.kits.Assert;
import com.acmedcare.framework.kits.http.client.AbstractHttpClient.METHOD;
import com.acmedcare.framework.kits.http.client.HttpClient;
import com.acmedcare.framework.kits.http.client.HttpParams;
import com.acmedcare.framework.kits.http.client.HttpParams.ENTITY;
import com.acmedcare.framework.kits.http.client.HttpResponse;
import com.acmedcare.framework.kits.http.client.HttpStatus;
import com.acmedcare.framework.newim.client.*;
import com.acmedcare.framework.newim.client.EndpointConstants.GroupRequest;
import com.acmedcare.framework.newim.client.EndpointConstants.MessageRequest;
import com.acmedcare.framework.newim.client.bean.Group;
import com.acmedcare.framework.newim.client.bean.MediaPayload;
import com.acmedcare.framework.newim.client.bean.Member;
import com.acmedcare.framework.newim.client.bean.request.*;
import com.acmedcare.framework.newim.client.bean.response.GroupResponse;
import com.acmedcare.framework.newim.client.exception.EndpointException;
import com.acmedcare.nas.api.NasClientConstants.ResponseCode;
import com.acmedcare.nas.api.ProgressCallback;
import com.acmedcare.nas.api.entity.UploadEntity;
import com.acmedcare.nas.api.exception.NasException;
import com.acmedcare.nas.client.NasClient;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.TypeReference;
import com.google.common.base.Strings;
import com.google.common.collect.Lists;
import org.apache.commons.lang3.ObjectUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.http.entity.ContentType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.util.List;
import java.util.Random;

/**
 * Master Endpoint Client
 *
 * @author <a href="mailto:iskp.me@gmail.com">Elve.Xu</a>
 * @version ${project.version} - 21/11/2018.
 */
public class MasterEndpointClient extends NasEndpointClient implements MasterEndpointService {

  private static final Logger LOG = LoggerFactory.getLogger(MasterEndpointClient.class);

  private final List<String> addresses;
  private final boolean https;

  public MasterEndpointClient(List<String> addresses) {
    this(addresses, false);
  }

  public MasterEndpointClient(List<String> addresses, boolean https) {
    this(addresses, false, null);
  }

  public MasterEndpointClient(List<String> addresses, boolean https, NasClient nasClient) {
    super(nasClient);
    this.addresses = addresses;
    this.https = https;
    Assert.isTrue(
        this.addresses != null && !this.addresses.isEmpty(),
        "remote master addresses must not be empty or null");
  }

  private String buildUrl(String requestPath) {
    if (Strings.isNullOrEmpty(requestPath)) {
      throw new EndpointException("invalid request context path");
    }

    Random random = new Random();
    int index = random.nextInt(this.addresses.size());
    String host = this.addresses.get(index);

    return (this.https ? "https://" : "http://")
        + host
        + (requestPath.startsWith("/") ? (requestPath) : ("/" + requestPath));
  }

  private void validate(String content) {
    try {
      JSON.parse(content);
    } catch (Exception e) {
      throw new EndpointException("content is not json.");
    }
  }

  /**
   * Create New Group Api
   *
   * @param request create request instance of {@link NewGroupRequest}
   * @throws EndpointException throw failed exception
   */
  @Override
  public void createNewGroup(NewGroupRequest request) throws EndpointException {

    try {
      if (request != null) {
        if (StringUtils.isAnyBlank(
            request.getGroupId(), request.getGroupOwner(), request.getGroupName())) {
          throw new EndpointException(
              "Create new group params:[groupId,groupOwner,groupName] must not be null or empty");
        }

        // build request
        HttpClient httpClient = HttpClient.getInstance();
        HttpParams httpParams = new HttpParams();
        httpParams.setContentType(ContentType.APPLICATION_JSON);
        httpParams.setEntity(ENTITY.STRING);
        httpParams.setValue(JSON.toJSONString(request));

        HttpResponse httpResponse = new HttpResponse();
        httpClient.request(
            METHOD.POST, buildUrl(GroupRequest.CREATE_GROUP), httpParams, null, httpResponse);

        if (httpResponse.getStatusCode() != HttpStatus.SC_OK) {
          if (httpResponse.getStatusCode() == HttpStatus.SC_NOT_FOUND) {
            throw new EndpointException("[404] Request Url: " + GroupRequest.CREATE_GROUP);
          }
          BizResult bizResult = BizResult.fromJSON(httpResponse.getResult(), BizResult.class);
          throw new EndpointException(
              "Create new group failed ," + bizResult.getException().getMessage());
        } else {
          LOG.info("Create new group succeed.");
        }
      } else {
        throw new EndpointException("Update request is null.");
      }
    } catch (EndpointException e) {
      throw e;
    } catch (Exception e) {
      throw new EndpointException("Create new group request failed", e);
    }
  }

  /**
   * Member(s) Join Group Api
   *
   * @param request join group request instance of {@link AddGroupMembersRequest}
   * @throws EndpointException throw failed exception
   */
  @Override
  public void joinGroup(AddGroupMembersRequest request) throws EndpointException {
    try {
      if (request != null) {
        if (StringUtils.isAnyBlank(request.getGroupId())) {
          throw new EndpointException("Join group request params:[groupId] must not be null or ''");
        }

        if (request.getMembers() == null || request.getMembers().isEmpty()) {
          throw new EndpointException(
              "Join group request params:[members] must not be null or empty");
        }

        // build request
        HttpClient httpClient = HttpClient.getInstance();
        HttpParams httpParams = new HttpParams();
        httpParams.setContentType(ContentType.APPLICATION_JSON);
        httpParams.setEntity(ENTITY.STRING);
        httpParams.setValue(JSON.toJSONString(request));

        HttpResponse httpResponse = new HttpResponse();
        httpClient.request(
            METHOD.POST, buildUrl(GroupRequest.ADD_GROUP_MEMBERS), httpParams, null, httpResponse);

        if (httpResponse.getStatusCode() != HttpStatus.SC_OK) {

          if (httpResponse.getStatusCode() == HttpStatus.SC_NOT_FOUND) {
            throw new EndpointException("[404] Request Url: " + GroupRequest.ADD_GROUP_MEMBERS);
          }

          BizResult bizResult = BizResult.fromJSON(httpResponse.getResult(), BizResult.class);
          throw new EndpointException(
              "Join group failed ," + bizResult.getException().getMessage());
        } else {
          LOG.info("Join group succeed.");
        }
      } else {
        throw new EndpointException("Update request is null.");
      }
    } catch (EndpointException e) {
      throw e;
    } catch (Exception e) {
      throw new EndpointException("Join group request failed", e);
    }
  }

  /**
   * Remove Group Member(s)
   *
   * @param request remove group member request instance of {@link RemoveGroupMembersRequest}
   * @throws EndpointException throw failed exception
   */
  @Override
  public void removeGroupMembers(RemoveGroupMembersRequest request) throws EndpointException {
    try {
      if (request != null) {
        if (StringUtils.isAnyBlank(request.getGroupId())) {
          throw new EndpointException(
              "Remove group member(s) request params:[groupId] must not be null or ''");
        }

        if (request.getMemberIds() == null || request.getMemberIds().isEmpty()) {
          throw new EndpointException(
              "Remove group member(s) request params:[members] must not be null or empty");
        }

        // build request
        HttpClient httpClient = HttpClient.getInstance();
        HttpParams httpParams = new HttpParams();
        httpParams.setContentType(ContentType.APPLICATION_JSON);
        httpParams.setEntity(ENTITY.STRING);
        httpParams.setValue(JSON.toJSONString(request));

        HttpResponse httpResponse = new HttpResponse();
        httpClient.request(
            METHOD.POST,
            buildUrl(GroupRequest.REMOVE_GROUP_MEMBERS),
            httpParams,
            null,
            httpResponse);

        if (httpResponse.getStatusCode() != HttpStatus.SC_OK) {

          if (httpResponse.getStatusCode() == HttpStatus.SC_NOT_FOUND) {
            throw new EndpointException("[404] Request Url: " + GroupRequest.REMOVE_GROUP_MEMBERS);
          }

          BizResult bizResult = BizResult.fromJSON(httpResponse.getResult(), BizResult.class);
          throw new EndpointException(
              "Remove group member(s) failed ," + bizResult.getException().getMessage());
        } else {
          LOG.info("Remove group member(s) succeed.");
        }
      } else {
        throw new EndpointException("Update request is null.");
      }
    } catch (EndpointException e) {
      throw e;
    } catch (Exception e) {
      throw new EndpointException("Remove group member(s) request failed", e);
    }
  }

  /**
   * Update Group Api
   *
   * @param request update group request instance of {@link UpdateGroupRequest}
   * @return return old {@link com.acmedcare.framework.newim.client.bean.Group} instance
   * @throws EndpointException throw failed exception
   */
  @Override
  public GroupResponse updateGroup(UpdateGroupRequest request) throws EndpointException {
    try {
      if (request != null) {
        if (StringUtils.isAnyBlank(
            request.getGroupId(), request.getGroupOwner(), request.getGroupName())) {
          throw new EndpointException(
              "Update group params:[groupId,groupOwner,groupName] must not be null or empty");
        }

        // build request
        HttpClient httpClient = HttpClient.getInstance();
        HttpParams httpParams = new HttpParams();
        httpParams.setContentType(ContentType.APPLICATION_JSON);
        httpParams.setEntity(ENTITY.STRING);
        httpParams.setValue(JSON.toJSONString(request));

        HttpResponse httpResponse = new HttpResponse();
        httpClient.request(
            METHOD.POST, buildUrl(GroupRequest.UPDATE_GROUP), httpParams, null, httpResponse);

        if (httpResponse.getStatusCode() != HttpStatus.SC_OK) {

          if (httpResponse.getStatusCode() == HttpStatus.SC_NOT_FOUND) {
            throw new EndpointException("[404] Request Url: " + GroupRequest.UPDATE_GROUP);
          }

          BizResult bizResult = BizResult.fromJSON(httpResponse.getResult(), BizResult.class);
          throw new EndpointException(
              "Update group failed ," + bizResult.getException().getMessage());
        } else {
          LOG.info("Update group succeed.");
          return BizResult.fromJSON(httpResponse.getResult(), GroupResponse.class);
        }
      } else {
        throw new EndpointException("Update request is null.");
      }
    } catch (EndpointException e) {
      throw e;
    } catch (Exception e) {
      throw new EndpointException("Create new group request failed", e);
    }
  }

  /**
   * Remove Group Api
   *
   * @param groupId group id
   * @return removed {@link Group} instance
   * @throws EndpointException throw failed exception
   */
  @Override
  public GroupResponse removeGroup(String groupId) throws EndpointException {
    return removeGroup(groupId, MessageConstants.DEFAULT_NAMESPACE);
  }

  @Override
  public GroupResponse removeGroup(String groupId, String namespace) throws EndpointException {
    try {
      if (StringUtils.isAnyBlank(groupId)) {
        throw new EndpointException("Remove group request params:[groupId] must not be null or ''");
      }

      // build request
      HttpClient httpClient = HttpClient.getInstance();
      HttpParams httpParams = new HttpParams();
      httpParams.setEntity(ENTITY.FORM);
      httpParams.put("groupId", groupId);
      httpParams.put("namespace", namespace);

      HttpResponse httpResponse = new HttpResponse();
      httpClient.request(
          METHOD.POST, buildUrl(GroupRequest.REMOVE_GROUP), httpParams, null, httpResponse);

      if (httpResponse.getStatusCode() != HttpStatus.SC_OK) {

        if (httpResponse.getStatusCode() == HttpStatus.SC_NOT_FOUND) {
          throw new EndpointException("[404] Request Url: " + GroupRequest.REMOVE_GROUP);
        }

        BizResult bizResult = BizResult.fromJSON(httpResponse.getResult(), BizResult.class);
        throw new EndpointException(
            "Remove group failed ," + bizResult.getException().getMessage());
      } else {
        LOG.info("Remove group succeed.");
        return BizResult.fromJSON(httpResponse.getResult(), GroupResponse.class);
      }
    } catch (EndpointException e) {
      throw e;
    } catch (Exception e) {
      throw new EndpointException("Remove group request failed", e);
    }
  }


  @Override
  public List<Member> queryGroupMemberList(String groupId, String namespace) throws EndpointException {
    try {
      if (StringUtils.isAnyBlank(groupId)) {
        throw new EndpointException("Query group member list request params:[groupId] must not be null or ''");
      }

      // build request
      HttpClient httpClient = HttpClient.getInstance();
      HttpParams httpParams = new HttpParams();
      httpParams.setEntity(ENTITY.FORM);
      httpParams.put("groupId", groupId);
      httpParams.put("namespace", namespace);

      HttpResponse httpResponse = new HttpResponse();
      httpClient.request(
          METHOD.GET, buildUrl(GroupRequest.GROUP_MEMBER_LIST), httpParams, null, httpResponse);

      if (httpResponse.getStatusCode() != HttpStatus.SC_OK) {

        if (httpResponse.getStatusCode() == HttpStatus.SC_NOT_FOUND) {
          throw new EndpointException("[404] Request Url: " + GroupRequest.REMOVE_GROUP);
        }

        BizResult bizResult = BizResult.fromJSON(httpResponse.getResult(), BizResult.class);
        throw new EndpointException(
            "Query group member list failed ," + bizResult.getException().getMessage());
      } else {
        LOG.info("Query group member list  succeed.");
        return JSON.parseObject(httpResponse.getResult(),new TypeReference<List<Member>>(){});
      }
    } catch (EndpointException e) {
      throw e;
    } catch (Exception e) {
      throw new EndpointException("Remove group request failed", e);
    }
  }


  @Override
  public List<Group> queryGroupList(String groupBizType, String namespace) throws EndpointException {
    try {
      if (StringUtils.isAnyBlank(groupBizType)) {
        throw new EndpointException("Query group list request params:[groupBizType] must not be null or ''");
      }

      // build request
      HttpClient httpClient = HttpClient.getInstance();
      HttpParams httpParams = new HttpParams();
      httpParams.setEntity(ENTITY.FORM);
      httpParams.put("groupBizType", groupBizType);
      httpParams.put("namespace", namespace);

      HttpResponse httpResponse = new HttpResponse();
      httpClient.request(
          METHOD.GET, buildUrl(GroupRequest.GROUP_LIST), httpParams, null, httpResponse);

      if (httpResponse.getStatusCode() != HttpStatus.SC_OK) {

        if (httpResponse.getStatusCode() == HttpStatus.SC_NOT_FOUND) {
          throw new EndpointException("[404] Request Url: " + GroupRequest.REMOVE_GROUP);
        }

        if (httpResponse.getStatusCode() == HttpStatus.SC_NO_CONTENT) {
          LOG.warn("[204] Query group list no content");
          return Lists.newArrayList();
        }

        BizResult bizResult = BizResult.fromJSON(httpResponse.getResult(), BizResult.class);
        throw new EndpointException(
            "Query group list failed ," + bizResult.getException().getMessage());
      } else {
        LOG.info("Query group list  succeed.");
        return JSON.parseObject(httpResponse.getResult(), new TypeReference<List<Group>>() {
        });
      }
    } catch (EndpointException e) {
      throw e;
    } catch (Exception e) {
      throw new EndpointException("Query group list request failed", e);
    }
  }


  /**
   * Send Single Message
   *
   * @param bizType bizType of {@link MessageBizType}
   * @param sender message sender ,passport id
   * @param receiver message receiver , dest passport id
   * @param content message content
   * @param contentType content type of {@link MessageContentType}, nullable ; Default type is
   *     {@link MessageContentType#APPLICATION_JSON}
   * @throws EndpointException throw failed exception
   * @see MessageBizType
   * @see MessageContentType
   */
  @Override
  public void sendSingleMessage(
      MessageBizType bizType,
      String sender,
      String receiver,
      String content,
      MessageContentType contentType,
      MessageAttribute messageAttribute)
      throws EndpointException {

    try {
      if (!ObjectUtils.allNotNull(bizType)) {
        throw new EndpointException(
            "Send single message request param:[bizType] can't be null, @see class 'com.acmedcare.framework.newim.client.MessageBizType'");
      }

      if (StringUtils.isAnyBlank(sender, receiver, content)) {
        throw new EndpointException(
            "Send single message request param:[sender,receiver,content] can't be null");
      }

      if (contentType == null) {
        contentType = MessageContentType.APPLICATION_JSON;
      }

      if (contentType.equals(MessageContentType.APPLICATION_JSON)) {
        validate(content);
      }

      if (messageAttribute == null) {
        messageAttribute = MessageAttribute.builder().build();
      }

      SendMessageRequest request = new SendMessageRequest();
      request.setContent(content);
      request.setReceiver(receiver);
      request.setSender(sender);
      request.setType(bizType.name());
      request.setMaxRetryTimes(messageAttribute.getMaxRetryTimes());
      request.setPersistent(messageAttribute.isPersistent());
      request.setQos(messageAttribute.isQos());
      request.setRetryPeriod(messageAttribute.getRetryPeriod());

      // build request
      HttpClient httpClient = HttpClient.getInstance();
      HttpParams httpParams = new HttpParams();
      httpParams.setContentType(ContentType.APPLICATION_JSON);
      httpParams.setEntity(ENTITY.STRING);
      httpParams.setValue(JSON.toJSONString(request));

      HttpResponse httpResponse = new HttpResponse();
      httpClient.request(
          METHOD.POST, buildUrl(MessageRequest.SEND_MESSAGE), httpParams, null, httpResponse);

      if (httpResponse.getStatusCode() != HttpStatus.SC_OK) {

        if (httpResponse.getStatusCode() == HttpStatus.SC_NOT_FOUND) {
          throw new EndpointException("[404] Request Url: " + MessageRequest.SEND_MESSAGE);
        }

        BizResult bizResult = BizResult.fromJSON(httpResponse.getResult(), BizResult.class);
        throw new EndpointException(
            "Send single message failed ," + bizResult.getException().getMessage());
      } else {
        LOG.info("Send single message succeed.");
      }

    } catch (EndpointException e) {
      throw e;
    } catch (Exception e) {
      throw new EndpointException("Send single message request failed", e);
    }
  }

  /**
   * Send Single Media Message
   *
   * @param bizType bizType of {@link MessageBizType}
   * @param sender message sender ,passport id
   * @param receiver message receiver , dest passport id
   * @param file message content
   * @param progressCallback progress callback of {@link ProgressCallback}
   * @param messageAttribute message attribute instance of {@link MessageAttribute}
   * @throws EndpointException throw failed exception
   * @see MessageBizType
   * @see MessageContentType
   * @see MessageAttribute
   */
  @Override
  public void sendSingleMessage(
      MessageBizType bizType,
      String sender,
      String receiver,
      File file,
      MessageAttribute messageAttribute,
      ProgressCallback progressCallback)
      throws EndpointException, NasException {

    try {

      if (file != null && file.exists()) {

        String fileName = file.getName();
        String fileSuffix =
            file.getAbsolutePath().substring(file.getAbsolutePath().lastIndexOf(".") + 1);

        UploadEntity uploadEntity =
            this.nasClient.upload(fileName, fileSuffix, file, progressCallback);

        if (uploadEntity.getResponseCode().equals(ResponseCode.UPLOAD_OK)) {
          MediaPayload mediaPayload = new MediaPayload();
          mediaPayload.setMediaFileName(fileName);
          mediaPayload.setMediaFileSuffix(fileSuffix);
          mediaPayload.setMediaPayloadKey(uploadEntity.getFid());
          mediaPayload.setMediaPayloadAccessUrl(uploadEntity.getPublicUrl());

          // invoke send
          this.sendSingleMessage(
              bizType,
              sender,
              receiver,
              JSON.toJSONString(mediaPayload),
              MessageContentType.APPLICATION_JSON,
              messageAttribute);

        } else {

          // failed
          throw new NasException(
              "Single file upload failed ,error code : "
                  + uploadEntity.getResponseCode()
                  + ", reason : "
                  + uploadEntity.getMessage());
        }
      }
    } catch (NasException | EndpointException e) {
      throw e;
    } catch (Exception e) {
      throw new EndpointException("Send single media message request failed", e);
    }
  }

  /**
   * Batch Send Single Message
   *
   * @param bizType bizType of {@link MessageBizType}
   * @param sender message sender ,passport id
   * @param receivers message receivers , dest passport ids list
   * @param content message content
   * @param contentType content type of {@link MessageContentType}
   * @throws EndpointException throw failed exception
   * @see MessageBizType
   * @see MessageContentType
   */
  @Override
  public void batchSendSingleMessages(
      MessageBizType bizType,
      String sender,
      List<String> receivers,
      String content,
      MessageContentType contentType,
      MessageAttribute messageAttribute)
      throws EndpointException {

    try {
      if (!ObjectUtils.allNotNull(bizType)) {
        throw new EndpointException(
            "Batch send single message request param:[bizType] can't be null, @see class 'com.acmedcare.framework.newim.client.MessageBizType'");
      }

      if (StringUtils.isAnyBlank(sender, content)) {
        throw new EndpointException(
            "Batch send single message request param:[sender,content] can't be null");
      }

      if (receivers == null || receivers.isEmpty()) {
        throw new EndpointException(
            "Batch send single message request param:[receivers] can't be null");
      }

      if (contentType == null) {
        contentType = MessageContentType.APPLICATION_JSON;
      }

      if (contentType.equals(MessageContentType.APPLICATION_JSON)) {
        validate(content);
      }

      if (messageAttribute == null) {
        messageAttribute = MessageAttribute.builder().build();
      }

      BatchSendMessageRequest request = new BatchSendMessageRequest();
      request.setContent(content);
      request.setReceivers(receivers);
      request.setSender(sender);
      request.setType(bizType.name());
      request.setMaxRetryTimes(messageAttribute.getMaxRetryTimes());
      request.setPersistent(messageAttribute.isPersistent());
      request.setQos(messageAttribute.isQos());
      request.setRetryPeriod(messageAttribute.getRetryPeriod());

      // build request
      HttpClient httpClient = HttpClient.getInstance();
      HttpParams httpParams = new HttpParams();
      httpParams.setContentType(ContentType.APPLICATION_JSON);
      httpParams.setEntity(ENTITY.STRING);
      httpParams.setValue(JSON.toJSONString(request));

      HttpResponse httpResponse = new HttpResponse();
      httpClient.request(
          METHOD.POST, buildUrl(MessageRequest.BATCH_SEND_MESSAGE), httpParams, null, httpResponse);

      if (httpResponse.getStatusCode() != HttpStatus.SC_OK) {

        if (httpResponse.getStatusCode() == HttpStatus.SC_NOT_FOUND) {
          throw new EndpointException("[404] Request Url: " + MessageRequest.BATCH_SEND_MESSAGE);
        }

        BizResult bizResult = BizResult.fromJSON(httpResponse.getResult(), BizResult.class);
        throw new EndpointException(
            "Batch send single message failed ," + bizResult.getException().getMessage());
      } else {
        LOG.info("Batch send single message succeed.");
      }

    } catch (EndpointException e) {
      throw e;
    } catch (Exception e) {
      throw new EndpointException("Batch send single message request failed", e);
    }
  }

  /**
   * Batch Send Single Media Message
   *
   * @param bizType bizType of {@link MessageBizType}
   * @param sender message sender ,passport id
   * @param receivers message receivers , dest passport ids list
   * @param file message content
   * @param messageAttribute message attribute instance of {@link MessageAttribute}
   * @param progressCallback progress callback of {@link ProgressCallback}
   * @throws EndpointException throw failed exception
   * @throws NasException nas failed exception
   * @see MessageBizType
   * @see MessageContentType
   */
  @Override
  public void batchSendSingleMessages(
      MessageBizType bizType,
      String sender,
      List<String> receivers,
      File file,
      MessageAttribute messageAttribute,
      ProgressCallback progressCallback)
      throws EndpointException, NasException {

    try {

      if (file != null && file.exists()) {

        String fileName = file.getName();
        String fileSuffix =
            file.getAbsolutePath().substring(file.getAbsolutePath().lastIndexOf(".") + 1);

        UploadEntity uploadEntity =
            this.nasClient.upload(fileName, fileSuffix, file, progressCallback);

        if (uploadEntity.getResponseCode().equals(ResponseCode.UPLOAD_OK)) {
          MediaPayload mediaPayload = new MediaPayload();
          mediaPayload.setMediaFileName(fileName);
          mediaPayload.setMediaFileSuffix(fileSuffix);
          mediaPayload.setMediaPayloadKey(uploadEntity.getFid());
          mediaPayload.setMediaPayloadAccessUrl(uploadEntity.getPublicUrl());

          // invoke send
          this.batchSendSingleMessages(
              bizType,
              sender,
              receivers,
              JSON.toJSONString(mediaPayload),
              MessageContentType.APPLICATION_JSON,
              messageAttribute);

        } else {

          // failed
          throw new NasException(
              "Single file upload failed ,error code : "
                  + uploadEntity.getResponseCode()
                  + ", reason : "
                  + uploadEntity.getMessage());
        }
      }
    } catch (NasException | EndpointException e) {
      throw e;
    } catch (Exception e) {
      throw new EndpointException("Send single media message request failed", e);
    }
  }

  /**
   * Send Group Message
   *
   * @param bizType bizType of {@link MessageBizType}
   * @param sender message sender ,passport id
   * @param groupId group id
   * @param content message content
   * @param contentType content type of {@link MessageContentType}
   * @throws EndpointException throw failed exception
   * @see MessageBizType
   * @see MessageContentType
   */
  @Override
  public void sendGroupMessage(
      MessageBizType bizType,
      String sender,
      String groupId,
      String content,
      MessageContentType contentType,
      MessageAttribute messageAttribute)
      throws EndpointException {

    try {
      if (!ObjectUtils.allNotNull(bizType)) {
        throw new EndpointException(
            "Send group message request param:[bizType] can't be null, @see class 'com.acmedcare.framework.newim.client.MessageBizType'");
      }

      if (StringUtils.isAnyBlank(sender, groupId, content)) {
        throw new EndpointException(
            "Send group message request param:[sender,groupId,content] can't be null");
      }

      if (contentType == null) {
        contentType = MessageContentType.APPLICATION_JSON;
      }

      if (contentType.equals(MessageContentType.APPLICATION_JSON)) {
        validate(content);
      }

      if (messageAttribute == null) {
        messageAttribute = MessageAttribute.builder().build();
      }

      SendGroupMessageRequest request = new SendGroupMessageRequest();
      request.setContent(content);
      request.setGroupId(groupId);
      request.setSender(sender);
      request.setType(bizType.name());
      request.setMaxRetryTimes(messageAttribute.getMaxRetryTimes());
      request.setPersistent(messageAttribute.isPersistent());
      request.setQos(messageAttribute.isQos());
      request.setRetryPeriod(messageAttribute.getRetryPeriod());

      // build request
      HttpClient httpClient = HttpClient.getInstance();
      HttpParams httpParams = new HttpParams();
      httpParams.setContentType(ContentType.APPLICATION_JSON);
      httpParams.setEntity(ENTITY.STRING);
      httpParams.setValue(JSON.toJSONString(request));

      HttpResponse httpResponse = new HttpResponse();
      httpClient.request(
          METHOD.POST, buildUrl(MessageRequest.SEND_GROUP_MESSAGE), httpParams, null, httpResponse);

      if (httpResponse.getStatusCode() != HttpStatus.SC_OK) {

        if (httpResponse.getStatusCode() == HttpStatus.SC_NOT_FOUND) {
          throw new EndpointException("[404] Request Url: " + MessageRequest.SEND_GROUP_MESSAGE);
        }

        BizResult bizResult = BizResult.fromJSON(httpResponse.getResult(), BizResult.class);
        throw new EndpointException(
            "Send group message failed ," + bizResult.getException().getMessage());
      } else {
        LOG.info("Send group message succeed.");
      }

    } catch (EndpointException e) {
      throw e;
    } catch (Exception e) {
      throw new EndpointException("Send group message request failed", e);
    }
  }

  /**
   * Send Group Message
   *
   * @param bizType bizType of {@link MessageBizType}
   * @param sender message sender ,passport id
   * @param groupId group id
   * @param file message content
   * @param messageAttribute message attribute instance of {@link MessageAttribute}
   * @param progressCallback progress callback of {@link ProgressCallback}
   * @throws EndpointException throw failed exception
   * @throws NasException nas failed exception
   * @see MessageBizType
   * @see MessageContentType
   */
  @Override
  public void sendGroupMessage(
      MessageBizType bizType,
      String sender,
      String groupId,
      File file,
      MessageAttribute messageAttribute,
      ProgressCallback progressCallback)
      throws EndpointException, NasException {

    try {

      if (file != null && file.exists()) {

        String fileName = file.getName();
        String fileSuffix =
            file.getAbsolutePath().substring(file.getAbsolutePath().lastIndexOf(".") + 1);

        UploadEntity uploadEntity =
            this.nasClient.upload(fileName, fileSuffix, file, progressCallback);

        if (uploadEntity.getResponseCode().equals(ResponseCode.UPLOAD_OK)) {
          MediaPayload mediaPayload = new MediaPayload();
          mediaPayload.setMediaFileName(fileName);
          mediaPayload.setMediaFileSuffix(fileSuffix);
          mediaPayload.setMediaPayloadKey(uploadEntity.getFid());
          mediaPayload.setMediaPayloadAccessUrl(uploadEntity.getPublicUrl());

          // invoke send
          this.sendGroupMessage(
              bizType,
              sender,
              groupId,
              JSON.toJSONString(mediaPayload),
              MessageContentType.APPLICATION_JSON,
              messageAttribute);

        } else {

          // failed
          throw new NasException(
              "Single file upload failed ,error code : "
                  + uploadEntity.getResponseCode()
                  + ", reason : "
                  + uploadEntity.getMessage());
        }
      }
    } catch (NasException | EndpointException e) {
      throw e;
    } catch (Exception e) {
      throw new EndpointException("Send single media message request failed", e);
    }
  }
}
