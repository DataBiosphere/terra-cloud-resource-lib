package bio.terra.cloudres.aws.sts;

import bio.terra.cloudres.common.CloudOperation;

public enum SecurityTokenServiceOperation implements CloudOperation {
  AWS_ASSUME_ROLE_WITH_WEB_IDENTITY,
  AWS_CREATE_GCP_CREDENTIALS_PROVIDER
}
