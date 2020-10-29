package bio.terra.cloudres.google.serviceusage;

import bio.terra.cloudres.common.CloudOperation;

/** {@link CloudOperation} for using Google ServiceUsage API. */
public enum ServiceUsageOperation implements CloudOperation {
  GOOGLE_BATCH_ENABLE_SERVICES,
  GOOGLE_LIST_SERVICES,
  GOOGLE_SERVICE_USAGE_OPERATION_GET;

  @Override
  public String getName() {
    return this.name();
  }
}
