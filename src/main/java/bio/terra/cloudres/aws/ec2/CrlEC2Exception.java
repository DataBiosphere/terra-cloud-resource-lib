package bio.terra.cloudres.aws.ec2;

/**
 * Exception thrown when CRL encounters an error from the underlying EC2 client. Generally, this is
 * a thin wrapper around the AWS exception which is available as this exception's cause.
 */
public class CrlEC2Exception extends RuntimeException {
  public CrlEC2Exception(String message, Throwable cause) {
    super(message, cause);
  }
}
