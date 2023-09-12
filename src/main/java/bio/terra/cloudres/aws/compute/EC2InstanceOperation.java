package bio.terra.cloudres.aws.compute;

import bio.terra.cloudres.common.CloudOperation;

/** {@link CloudOperation} for using AWS EC2 Instance API. */
public enum EC2InstanceOperation implements CloudOperation {
  AWS_GET_EC2_INSTANCE,
  AWS_RUN_EC2_INSTANCES,
  AWS_START_EC2_INSTANCE,
  AWS_STOP_EC2_INSTANCE,
  AWS_TERMINATE_EC2_INSTANCE,
}
