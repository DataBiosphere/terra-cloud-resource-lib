package bio.terra.cloudres.google.api.services.common;

import java.io.IOException;
import java.time.Duration;
import java.time.Instant;
import javax.annotation.CheckReturnValue;

/** Utility class for working with Operations. */
public class OperationUtils {
  private OperationUtils() {}

  /**
   * Poll an operation until it completes, the operation get errors, or the timeout is reached.
   *
   * <p>Note that this does not throw an error if the operation executes "successfully" but returns
   * an error.
   */
  @CheckReturnValue
  public static <T> OperationCow<T> pollUntilComplete(
      OperationCow<T> operation, Duration pollingInterval, Duration timeout)
      throws IOException, InterruptedException {
    Instant deadline = Instant.now().plus(timeout);

    while (!isDone(operation.getOperationAdapter())
        && operation.getOperationAdapter().getError() == null) {
      if (Instant.now().plus(pollingInterval).isAfter(deadline)) {
        throw new InterruptedException(
            "Timeout during pollUntilComplete for operation "
                + operation.getOperationAdapter().getName());
      }
      Thread.sleep(pollingInterval.toMillis());
      operation = operation.executeGet();
    }
    return operation;
  }

  /** Convenience method for checking if an operation is done. */
  public static boolean isDone(OperationCow.OperationAdapter<?> operationAdapter) {
    // Google lets getDone on operations return null, which is also not done.
    return operationAdapter.getDone() != null && operationAdapter.getDone();
  }
}
