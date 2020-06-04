package bio.terra.cloudres.common;

/** The supported Cloud API methods in Enum */
public enum CloudOperation {
  // Google Resource Manager Operations
  GOOGLE_CREATE_PROJECT,
  GOOGLE_DELETE_PROJECT,

  // Google Storage Operations
  GOOGLE_CREATE_BUCKET,
  GOOGLE_GET_BUCKET,
  GOOGLE_DELETE_BUCKET,

  // Google BigQuery Operations
  GOOGLE_CREATE_DATASET,
  GOOGLE_UPDATE_DATASET,
  GOOGLE_DELETE_DATASET,
  GOOGLE_GET_DATASET;
}
