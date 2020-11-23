package bio.terra.cloudres.google.compute;

import bio.terra.cloudres.common.CloudOperation;

/** {@link CloudOperation} for using Google Compute API. */
public enum CloudComputeOperation implements CloudOperation {
  GOOGLE_COMPUTE_GLOBAL_OPERATION_GET,
  GOOGLE_COMPUTE_REGION_OPERATION_GET,
  GOOGLE_INSERT_FIREWALL,
  GOOGLE_INSERT_NETWORK,
  GOOGLE_INSERT_ROUTE,
  GOOGLE_INSERT_SUBNETWORK,
  GOOGLE_DELETE_FIREWALL,
  GOOGLE_DELETE_NETWORK,
  GOOGLE_GET_FIREWAL,
  GOOGLE_GET_NETWORK,
  GOOGLE_GET_ROUTE,
  GOOGLE_GET_SUBNETWORK,
}
