package bio.terra.cloudres.common;

/**
 * Exception thrown when Janitor call to record resource for cleanup fails.
 *
 * <p>There is nothing for the CRL client to do about this. This should hopefully happen very
 * rarely, or we should revisit.
 */
public class JanitorException extends RuntimeException {
  public JanitorException(String msg, Throwable cause) {
    super(msg, cause);
  }
}
