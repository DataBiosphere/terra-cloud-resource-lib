package bio.terra.cloudres.common;

import bio.terra.cloudres.util.MetricsHelper;
import com.google.cloud.http.BaseHttpServiceException;
import com.google.common.base.Stopwatch;
import io.opencensus.common.Scope;
import io.opencensus.trace.Tracer;
import io.opencensus.trace.Tracing;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.Duration;
import java.util.concurrent.Callable;

/** Annotates executing cloud operations with logs, traces, and metrics to record what happens. */
public class OperationAnnotator {
  private static final Tracer tracer = Tracing.getTracer();
  private final Logger logger = LoggerFactory.getLogger(OperationAnnotator.class);
  private final ClientConfig options;
  private final int GENERIC_UNKNOWN_ERROR_CODE = 1;

  public OperationAnnotator(ClientConfig options) {
    this.options = options;
  }

  public <R> R executeGoogleCall(Callable<R> googleCall, CloudOperation operation)
      throws Exception {
    Stopwatch stopwatch = Stopwatch.createStarted();
    logger.debug("Executing Google Calls" + operation);
    try (Scope ss = tracer.spanBuilder(operation.name()).startScopedSpan()) {
      // Record the Cloud API usage.
      recordApiCount(operation);
      try {
        return googleCall.call();
      } catch (Exception e) {
        if (e instanceof BaseHttpServiceException) {
          logger.warn("Failed to execute Google Call for : " + operation);
          recordErrors(((BaseHttpServiceException) e).getCode(), operation);
        } else {
          logger.warn("An internal error happens during Google call : " + operation);
          recordErrors(GENERIC_UNKNOWN_ERROR_CODE, operation);
        }
        throw e;
      } finally {
        recordLatency(stopwatch.stop().elapsed(), operation);
      }
    }
  }

  private void recordApiCount(CloudOperation operation) {
    MetricsHelper.recordApiCount(options.getClient(), operation);
  }

  private void recordErrors(int httpStatusCode, CloudOperation operation) {
    MetricsHelper.recordError(options.getClient(), operation, httpStatusCode);
  }

  private void recordLatency(Duration duration, CloudOperation operation) {
    MetricsHelper.recordLatency(options.getClient(), operation, duration);
  }
}
