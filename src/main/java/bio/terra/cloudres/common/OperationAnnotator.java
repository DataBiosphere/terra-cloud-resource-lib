package bio.terra.cloudres.common;

import bio.terra.cloudres.util.MetricsHelper;
import com.google.cloud.http.BaseHttpServiceException;
import io.opencensus.common.Scope;
import io.opencensus.trace.Tracer;
import io.opencensus.trace.Tracing;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.Duration;
import java.util.concurrent.Callable;

/** Helper class to use Resource clients. */
public class OperationAnnotator {
  private static final Tracer tracer = Tracing.getTracer();
  private final Logger logger = LoggerFactory.getLogger(OperationAnnotator.class);
  private final ClientConfig options;

  public OperationAnnotator(ClientConfig options) {
    this.options = options;
  }

  public <R> R executeGoogleCall(Callable<R> googleCall, CloudOperation operation)
      throws Exception {
    long startTimeNs = System.nanoTime();
    logger.debug("Executing Google Calls" + operation);
    try (Scope ss = tracer.spanBuilder(operation.name()).startScopedSpan()) {
      // Record the Cloud API usage.
      recordApiCount(operation);
      try {
        return googleCall.call();
      } catch (BaseHttpServiceException e) {
        logger.warn("Failed to execute Google Call for : " + operation);
        recordErrors(String.valueOf(e.getCode()), operation);
        throw e;
      } finally {
        recordLatency(startTimeNs, operation);
      }
    }
  }

  private void recordApiCount(CloudOperation operation) {
    MetricsHelper.recordApiCount(options.getClient(), operation);
  }

  private void recordErrors(String errorCode, CloudOperation operation) {
    MetricsHelper.recordError(options.getClient(), operation, errorCode);
  }

  private void recordLatency(long startNs, CloudOperation operation) {
    MetricsHelper.recordLatency(
        options.getClient(), operation, Duration.ofNanos(System.nanoTime() - startNs));
  }
}
