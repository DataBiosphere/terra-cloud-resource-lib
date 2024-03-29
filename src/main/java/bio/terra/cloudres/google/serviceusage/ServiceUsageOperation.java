package bio.terra.cloudres.google.serviceusage;

import bio.terra.cloudres.common.CloudOperation;

/** {@link CloudOperation} for using Google ServiceUsage API. */
public enum ServiceUsageOperation implements CloudOperation {
  GOOGLE_BATCH_ENABLE_SERVICES,
  GOOGLE_LIST_SERVICES,
  GOOGLE_SERVICE_USAGE_OPERATION_GET,
  GOOGLE_CONSUMER_QUOTA_METRICS_LIMITS_GET,
  GOOGLE_CONSUMER_QUOTA_METRICS_LIMITS_CREATE,
  GOOGLE_CONSUMER_QUOTA_METRICS_LIMITS_LIST,
}
