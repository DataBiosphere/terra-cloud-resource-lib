package bio.terra.cloudres.aws.compute;

import bio.terra.cloudres.common.CloudOperation;

/** {@link CloudOperation} for using AWS EC2 Security Group API. */
public enum EC2SecurityGroupOperation implements CloudOperation {
  AWS_AUTHORIZE_EGRESS_EC2_SECURITY_GROUP,
  AWS_AUTHORIZE_INGRESS_EC2_SECURITY_GROUP,
  AWS_CREATE_EC2_SECURITY_GROUP,
  AWS_DELETE_EC2_SECURITY_GROUP,
  AWS_GET_EC2_SECURITY_GROUP,
  AWS_GET_BY_TAG_EC2_SECURITY_GROUP,
}
