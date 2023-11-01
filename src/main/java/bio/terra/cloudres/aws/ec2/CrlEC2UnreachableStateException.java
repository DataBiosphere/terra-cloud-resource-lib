package bio.terra.cloudres.aws.ec2;

import software.amazon.awssdk.services.ec2.model.InstanceStateName;

/**
 * Exception thrown when attempting to wait on a state that is unreachable from the current state.
 * For example, attempting to wait on state {@link InstanceStateName#RUNNING} while the instance is
 * in {@link InstanceStateName#STOPPING} state,
 */
public class CrlEC2UnreachableStateException extends CrlEC2Exception {
  protected CrlEC2UnreachableStateException(String instanceId, Throwable cause) {
    super(
        String.format(
            "Detected unreachable state while attempting to wait for EC2 Resource %s", instanceId),
        cause);
  }
}
