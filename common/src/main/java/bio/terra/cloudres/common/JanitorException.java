package bio.terra.cloudres.common;

/** Exception thrown when Janitor call throws exception. */
public class JanitorException extends RuntimeException {
  public JanitorException(String msg, Throwable cause) {
    super(msg, cause);
  }
}
