package bio.terra.cloudres.google.iam;

import bio.terra.cloudres.common.CloudOperation;

/** {@link CloudOperation} for using Google IAM API. */
public enum IamOperation implements CloudOperation {
  GOOGLE_CREATE_SERVICE_ACCOUNT,
  GOOGLE_DELETE_SERVICE_ACCOUNT,
  GOOGLE_LIST_SERVICE_ACCOUNT,
  GOOGLE_CREATE_ROLE,
  GOOGLE_DELETE_ROLE,
  GOOGLE_GET_ROLE,
  GOOGLE_LIST_ROLE,
}
