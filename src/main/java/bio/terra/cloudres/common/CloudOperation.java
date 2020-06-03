package bio.terra.cloudres.common;

/** The supported Cloud API methods in Enum */
public enum CloudOperation {
  // Resource Manager Operations
  GOOGLE_CREATE_PROJECT,
  GOOGLE_DELETE_PROJECT,

  // Storage Operations
  GOOGLE_CREATE_BUCKET,
  GOOGLE_GET_BUCKET,
  GOOGLE_DELETE_BUCKET;
}
