package bio.terra.cloudres.google.billing;

import bio.terra.cloudres.common.CloudOperation;

/** {@link CloudOperation} for using Google Cloud Billing API. */
public enum CloudBillingOperation implements CloudOperation {
  GOOGLE_GET_PROJECT_BILLING,
  GOOGLE_UPDATE_PROJECT_BILLING,
  GOOGLE_TEST_IAM_PERMISSIONS
}
