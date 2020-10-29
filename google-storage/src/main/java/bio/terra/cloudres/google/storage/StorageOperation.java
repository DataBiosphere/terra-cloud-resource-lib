package bio.terra.cloudres.google.storage;

import bio.terra.cloudres.common.CloudOperation;

/** {@link CloudOperation} for using Google Storage API. */
public enum StorageOperation implements CloudOperation {
  GOOGLE_COPY_BLOB,
  GOOGLE_CREATE_ACL_BLOB,
  GOOGLE_CREATE_BLOB,
  GOOGLE_CREATE_BLOB_AND_WRITER,
  GOOGLE_CREATE_BUCKET,
  GOOGLE_GET_ACL_BLOB,
  GOOGLE_GET_BLOB,
  GOOGLE_LIST_BLOB,
  GOOGLE_GET_BUCKET,
  GOOGLE_DELETE_ACL_BLOB,
  GOOGLE_DELETE_BLOB,
  GOOGLE_DELETE_BUCKET,
  GOOGLE_READ_BLOB,
  GOOGLE_UPDATE_BUCKET;
}
