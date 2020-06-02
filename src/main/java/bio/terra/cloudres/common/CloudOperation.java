package bio.terra.cloudres.common;

/** The supported Cloud API methods in Enum */
public enum CloudOperation {
  // Resource Manager Operations
  GOOGLE_CREATE_PROJECT,
  GOOGLE_DELETE_PROJECT,

  // Storage Operations
  GOOGLE_COPY_BLOB,
  GOOGLE_CREATE_BLOB,
  GOOGLE_CREATE_BLOB_AND_WRITER,
  GOOGLE_CREATE_BUCKET,
  GOOGLE_GET_BLOB,
  GOOGLE_GET_BUCKET,
  GOOGLE_DELETE_BLOB,
  GOOGLE_DELETE_BUCKET,
  GOOGLE_READ_BLOB;
}
