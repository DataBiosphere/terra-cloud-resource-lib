package bio.terra.cloudres.aws.notebook;

/**
 * Exception thrown when CRL encounters an error from the underlying SageMaker client. Generally,
 * this is a thin wrapper around the AWS exception which is available as this exception's cause.
 */
public class CrlSageMakerException extends RuntimeException {
  public CrlSageMakerException(String message, Throwable cause) {
    super(message, cause);
  }
}
