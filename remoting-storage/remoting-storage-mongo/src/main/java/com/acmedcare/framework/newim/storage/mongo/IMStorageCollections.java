package com.acmedcare.framework.newim.storage.mongo;

/**
 * IM Storage Collections
 *
 * @author <a href="mailto:iskp.me@gmail.com">Elve.Xu</a>
 * @version ${project.version} - 13/11/2018.
 */
public enum IMStorageCollections {

  /** 消息 */
  IM_MESSAGE("im_message"),

  /** 群组 */
  GROUP("im_group"),

  /** 群组成员管理 */
  REF_GROUP_MEMBER("im_refs_group_member"),
  ;

  String collectionName;

  IMStorageCollections(String collectionName) {
    this.collectionName = collectionName;
  }

  public String collectionName() {
    return collectionName;
  }
}
