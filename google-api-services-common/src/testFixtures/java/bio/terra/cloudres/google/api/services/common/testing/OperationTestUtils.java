package bio.terra.cloudres.google.api.services.common.testing;

import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import bio.terra.cloudres.google.api.services.common.OperationCow;
import bio.terra.cloudres.google.api.services.common.OperationUtils;
import java.io.IOException;
import java.time.Duration;

/** Test utilities for working with operations. */
public class OperationTestUtils {
  private OperationTestUtils() {}

  /**
   * Poll the operation until it completes and assert that it completed successfully. Returns the
   * final operation.
   */
  public static <T> OperationCow<T> pollAndAssertSuccess(
      OperationCow<T> operation, Duration pollingInterval, Duration timeout)
      throws IOException, InterruptedException {
    OperationCow<T> completed =
        OperationUtils.pollUntilComplete(operation, pollingInterval, timeout);
    assertNull(completed.getOperationAdapter().getError());
    assertTrue(completed.getOperationAdapter().getDone());
    return completed;
  }
}
