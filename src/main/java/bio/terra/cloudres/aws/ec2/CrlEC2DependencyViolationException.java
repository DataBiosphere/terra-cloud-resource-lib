package bio.terra.cloudres.aws.ec2;

import bio.terra.cloudres.common.CloudOperation;

/**
 * Exception thrown when an operation would violate a dependency.
 *
 * <p>For example, performing {@link EC2SecurityGroupOperation#AWS_DELETE_EC2_SECURITY_GROUP} on an
 * EC2 Security Group that is in use by non-terminated EC2 Instances would throw an {@link
 * CrlEC2DependencyViolationException}.
 */
public class CrlEC2DependencyViolationException extends CrlEC2Exception {
  public CrlEC2DependencyViolationException(
      CloudOperation operation, String resourceId, Throwable cause) {
    super(
        String.format(
            "Cannot perform operation %s on resource with ID '%s' as it is referenced by other resources.",
            operation, resourceId),
        cause);
  }
}
