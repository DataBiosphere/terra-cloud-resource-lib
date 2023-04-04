package bio.terra.cloudres.aws.bucket;

import bio.terra.cloudres.common.CloudOperation;

public enum S3BucketOperation implements CloudOperation {
  AWS_LIST_S3_OBJECTS,
  AWS_CREATE_S3_OBJECT,
  AWS_GET_S3_OBJECT,
  AWS_DELETE_S3_OBJECT
}
