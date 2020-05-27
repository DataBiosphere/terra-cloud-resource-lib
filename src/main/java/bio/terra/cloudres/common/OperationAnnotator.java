package bio.terra.cloudres.common;

import bio.terra.cloudres.util.MetricsHelper;
import com.google.cloud.http.BaseHttpServiceException;
import com.google.common.base.Stopwatch;
import io.opencensus.common.Scope;
import io.opencensus.trace.Tracer;
import io.opencensus.trace.Tracing;
import java.time.Duration;
import java.util.OptionalInt;
import java.util.concurrent.Callable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/** Annotates executing cloud operations with logs, traces, and metrics to record what happens. */
public class OperationAnnotator {
  private static final Tracer tracer = Tracing.getTracer();
  private final Logger logger = LoggerFactory.getLogger(OperationAnnotator.class);
  private final ClientConfig clientConfig;

  public OperationAnnotator(ClientConfig clientConfig) {
    this.clientConfig = clientConfig;
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
        recordErrors(getHttpErrorCode(e), operation);
        logger.info("Failed to execute Google Call for : " + operation);
        throw e;
      } finally {
        recordLatency(stopwatch.stop().elapsed(), operation);
      }
    }
  }

  private void recordApiCount(CloudOperation operation) {
    MetricsHelper.recordApiCount(clientConfig.getClientName(), operation);
  }

  private void recordErrors(OptionalInt httpStatusCode, CloudOperation operation) {
    MetricsHelper.recordError(clientConfig.getClientName(), operation, httpStatusCode);
  }

  private void recordLatency(Duration duration, CloudOperation operation) {
    MetricsHelper.recordLatency(clientConfig.getClientName(), operation, duration);
  }

  private OptionalInt getHttpErrorCode(Exception e) {
    return e instanceof BaseHttpServiceException
        ? OptionalInt.of(((BaseHttpServiceException) e).getCode())
        : OptionalInt.empty();
  }
}
