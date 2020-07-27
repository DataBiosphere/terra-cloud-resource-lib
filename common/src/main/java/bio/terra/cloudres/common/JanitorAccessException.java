package bio.terra.cloudres.common;

/** Exception thrown when obtain/refresh the Janitor client access token. */
public class JanitorAccessException extends RuntimeException {
  public JanitorAccessException(String msg, Throwable cause) {
    super(msg, cause);
  }
}
