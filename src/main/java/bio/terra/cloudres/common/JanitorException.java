package bio.terra.cloudres.common;

/**
 * Exception thrown during Janitor publish message to record resource for cleanup fails.
 *
 * <p>There is nothing for the CRL client to do about this. This should hopefully happen very
 * rarely, or we should revisit.
 *
 */
public class JanitorException extends RuntimeException {
  public JanitorException(String msg, Throwable cause) {
    super(msg, cause);
  }
}
