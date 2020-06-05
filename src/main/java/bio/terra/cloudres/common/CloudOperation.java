package bio.terra.cloudres.common;

/** The supported Cloud API methods in Enum */
public enum CloudOperation {
  // Google Resource Manager Operations
  GOOGLE_CREATE_PROJECT,
  GOOGLE_DELETE_PROJECT,

<<<<<<< HEAD
  // Google Storage Operations
=======
  // Storage Operations
  GOOGLE_COPY_BLOB,
  GOOGLE_CREATE_BLOB,
  GOOGLE_CREATE_BLOB_AND_WRITER,
>>>>>>> 441ecf3f29fc9377a0539c84d4a648fff39d07c3
  GOOGLE_CREATE_BUCKET,
  GOOGLE_GET_BLOB,
  GOOGLE_GET_BUCKET,
<<<<<<< HEAD
  GOOGLE_DELETE_BUCKET,

  // Google BigQuery Operations
  GOOGLE_CREATE_DATASET,
  GOOGLE_UPDATE_DATASET,
  GOOGLE_DELETE_DATASET,
  GOOGLE_GET_DATASET;
=======
  GOOGLE_DELETE_BLOB,
  GOOGLE_DELETE_BUCKET,
  GOOGLE_READ_BLOB;
>>>>>>> 441ecf3f29fc9377a0539c84d4a648fff39d07c3
}
