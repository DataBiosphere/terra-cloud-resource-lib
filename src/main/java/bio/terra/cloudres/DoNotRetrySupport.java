package bio.terra.cloudres;

import com.google.cloud.BaseServiceException;
import com.uber.cadence.common.RetryOptions;

/**
 * Support class for signaling to cadence which exception should NOT be retried. This is required
 * because {@link RetryOptions#getDoNotRetry()} supports only exception classes, not any details
 * about those exceptions. Generally use one of the wrapDoNotRetry functions with a lambda: {@code
 * DoNotRetrySupport.wrapDoNotRetry(() -> this.googleCloudStorage.deleteBucketRaw(bucketName))}
 */
public class DoNotRetrySupport {
  /** Include this exception in {@link RetryOptions.Builder#setDoNotRetry(Class[])} */
  public static class DoNotRetryException extends RuntimeException {
    public DoNotRetryException(Throwable cause) {
      super(cause);
    }
  }

  public static <T> T wrapDoNotRetry(MaybeRetryableFunction<T> f) {
    try {
      return f.execute();
    } catch (BaseServiceException se) {
      throw maybeDoNotRetry(se);
    }
  }

  public static void wrapDoNotRetry(MaybeRetryableProcedure f) {
    try {
      f.execute();
    } catch (BaseServiceException se) {
      throw maybeDoNotRetry(se);
    }
  }

  /**
   * Lambda interface
   *
   * @param <T> nothing
   */
  public interface MaybeRetryableFunction<T> {
    T execute();
  }

  /** Lambda interface */
  public interface MaybeRetryableProcedure {
    void execute();
  }

  private static RuntimeException maybeDoNotRetry(BaseServiceException se) {
    if (!se.isRetryable()) {
      return new DoNotRetryException(se);
    } else {
      return se;
    }
  }
}
