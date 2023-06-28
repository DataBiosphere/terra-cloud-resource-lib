package bio.terra.cloudres.google.dataproc;

import bio.terra.cloudres.common.CloudOperation;

/** {@link CloudOperation} for using Google Dataproc Clusters API. */
public enum DataprocOperation implements CloudOperation {
  GOOGLE_CREATE_CLUSTER,
  GOOGLE_DELETE_CLUSTER,
  GOOGLE_GET_CLUSTER,
  GOOGLE_GET_IAM_POLICY_CLUSTER,
  GOOGLE_LIST_CLUSTER,
  GOOGLE_SET_IAM_POLICY_CLUSTER,
  GOOGLE_SET_METADATA_CLUSTER,
  GOOGLE_START_CLUSTER,
  GOOGLE_STOP_CLUSTER,
  GOOGLE_TEST_IAM_PERMISSIONS_CLUSTER,
  GOOGLE_DATAPROC_OPERATION_GET,
}
