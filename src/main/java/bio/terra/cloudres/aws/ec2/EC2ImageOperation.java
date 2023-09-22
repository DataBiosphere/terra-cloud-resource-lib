package bio.terra.cloudres.aws.ec2;

import bio.terra.cloudres.common.CloudOperation;

/** {@link CloudOperation} for using AWS EC2 Image API. */
public enum EC2ImageOperation implements CloudOperation {
  AWS_DESCRIBE_EC2_IMAGES,
  AWS_DESCRIBE_EC2_INSTANCE_TYPES,
}
