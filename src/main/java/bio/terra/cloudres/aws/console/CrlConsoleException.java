package bio.terra.cloudres.aws.console;

/**
 * This exception is thrown in the case where URL creation encounters an error. This is generally
 * not expected to happen, as we use parameterized builders for URLs.
 */
public class CrlConsoleException extends RuntimeException {
  public CrlConsoleException(String message, Throwable cause) {
    super(message, cause);
  }
}
