package bio.terra.cloudres.google.compute;

import bio.terra.cloudres.common.CloudOperation;

/** {@link CloudOperation} for using Google Compute API. */
public enum CloudComputeOperation implements CloudOperation {
  GOOGLE_INSERT_INSTANCE,
  GOOGLE_DELETE_INSTANCE,
  GOOGLE_GET_INSTANCE,
  GOOGLE_GET_IAM_POLICY_INSTANCE,
  GOOGLE_LIST_INSTANCE,
  GOOGLE_SET_IAM_POLICY_INSTANCE,
  GOOGLE_SET_METADATA_INSTANCE,
  GOOGLE_START_INSTANCE,
  GOOGLE_STOP_INSTANCE,
  GOOGLE_TEST_IAM_PERMISSIONS_INSTANCE,
  GOOGLE_AGGREGATED_LIST_SUBNETWORK,
  GOOGLE_COMPUTE_GLOBAL_OPERATION_GET,
  GOOGLE_COMPUTE_REGION_OPERATION_GET,
  GOOGLE_COMPUTE_ZONE_OPERATION_GET,
  GOOGLE_INSERT_FIREWALL,
  GOOGLE_INSERT_NETWORK,
  GOOGLE_INSERT_ROUTE,
  GOOGLE_INSERT_ROUTER,
  GOOGLE_INSERT_SUBNETWORK,
  GOOGLE_DELETE_FIREWALL,
  GOOGLE_DELETE_NETWORK,
  GOOGLE_DELETE_ROUTER,
  GOOGLE_GET_FIREWAL,
  GOOGLE_GET_NETWORK,
  GOOGLE_GET_ROUTE,
  GOOGLE_GET_ROUTER,
  GOOGLE_GET_SUBNETWORK,
  GOOGLE_GET_ZONE,
  GOOGLE_LIST_SUBNETWORK,
  GOOGLE_LIST_ZONE,
}
