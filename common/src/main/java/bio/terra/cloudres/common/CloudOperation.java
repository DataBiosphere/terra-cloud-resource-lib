package bio.terra.cloudres.common;

/** The supported Cloud API methods in Enum */
public enum CloudOperation {
  // Google Resource Manager Operations
  GOOGLE_CREATE_PROJECT,
  GOOGLE_DELETE_PROJECT,
  GOOGLE_RESOURCE_MANAGER_OPERATION_GET,

  // Google Storage Operations
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
  GOOGLE_UPDATE_BUCKET,

  // Google BigQuery Operations
  GOOGLE_CREATE_DATASET,
  GOOGLE_CREATE_BIGQUERY_TABLE,
  GOOGLE_INSERT_BIGQUERY_TABLE,
  GOOGLE_UPDATE_DATASET,
  GOOGLE_UPDATE_BIGQUERY_TABLE,
  GOOGLE_DELETE_DATASET,
  GOOGLE_DELETE_BIGQUERY_TABLE,
  GOOGLE_GET_DATASET,
  GOOGLE_GET_BIGQUERY_TABLE,
  GOOGLE_LIST_BIGQUERY_TABLE,
  GOOGLE_QUERY_BIGQUERY_TABLE,
  GOOGLE_RELOAD_DATASET,
  GOOGLE_RELOAD_BIGQUERY_TABLE,
}
