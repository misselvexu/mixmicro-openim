package com.acmedcare.framework.newim.storage.mongo;

/**
 * IM Storage Collections
 *
 * @author <a href="mailto:iskp.me@gmail.com">Elve.Xu</a>
 * @version ${project.version} - 13/11/2018.
 */
public enum IMStorageCollections {

  /** Message Storage Collection */
  MESSAGE("im_message");

  String collectionName;

  IMStorageCollections(String collectionName) {
    this.collectionName = collectionName;
  }

  public String collectionName() {
    return collectionName;
  }
}
