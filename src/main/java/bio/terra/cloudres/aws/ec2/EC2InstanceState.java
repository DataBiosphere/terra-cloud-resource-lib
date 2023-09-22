package bio.terra.cloudres.aws.ec2;

/**
 * Enumeration representing EC2 Instance states as described in the <a
 * href="https://docs.aws.amazon.com/AWSEC2/latest/UserGuide/ec2-instance-lifecycle.html">EC2 User
 * Guide Instance Lifecycle</a>.
 */
public enum EC2InstanceState {
  PENDING,
  RUNNING,
  SHUTTING_DOWN,
  STOPPED,
  TERMINATED,
}
