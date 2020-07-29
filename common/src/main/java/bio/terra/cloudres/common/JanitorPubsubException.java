package bio.terra.cloudres.common;

/**
 * Exception thrown when Janitor publish message to record resource for cleanup fails.
 *
 * <p>There is nothing for the CRL client to do about this. This should hopefully happen very
 * rarely, or we should revisit.
 */
public class JanitorPubsubException extends RuntimeException {
  public JanitorPubsubException(String msg, Throwable cause) {
    super(msg, cause);
  }
}
