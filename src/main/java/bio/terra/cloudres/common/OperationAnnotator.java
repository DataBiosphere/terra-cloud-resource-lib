package bio.terra.cloudres.common;

import bio.terra.cloudres.util.MetricsHelper;
import com.google.cloud.http.BaseHttpServiceException;
import com.google.common.annotations.VisibleForTesting;
import com.google.common.base.Stopwatch;
import com.google.gson.Gson;
import com.google.gson.JsonObject;
import io.opencensus.common.Scope;
import io.opencensus.trace.TraceId;
import io.opencensus.trace.Tracer;
import io.opencensus.trace.Tracing;
import java.time.Duration;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Optional;
import java.util.OptionalInt;
import org.slf4j.Logger;

/** Annotates executing cloud operations with logs, traces, and metrics to record what happens. */
public class OperationAnnotator {
  private static final Tracer tracer = Tracing.getTracer();
  /**
   * Fake HTTP status code value for errors that are not HTTP status errors. Useful for including
   * non-HTTP status errors in a single metric.
   */
  public static final int GENERIC_UNKNOWN_ERROR_CODE = 1;

  private final ClientConfig clientConfig;

  /** We inject a Logger to allow how logs are made to be controlled by the COWs. */
  private final Logger logger;

  public OperationAnnotator(ClientConfig clientConfig, Logger logger) {
    this.clientConfig = clientConfig;
    this.logger = logger;
  }

  /**
   * Executes the CowOperation.
   *
   * @param cowOperation: the {@link CowOperation} whichs contains all necessary information to
   *     execute this call.
   * @return the result of executing the {@code cowOperation}
   */
  public <R> R executeCowOperation(CowOperation<R> cowOperation) {
    CloudOperation cloudOperation = cowOperation.getCloudOperation();
    Optional<Exception> executionException = Optional.empty();

    try (Scope ss = tracer.spanBuilder(cowOperation.getCloudOperation().name()).startScopedSpan()) {
      // Record the Cloud API usage.
      recordApiCount(cloudOperation);

      Stopwatch stopwatch = Stopwatch.createStarted();
      try {
        R response = cowOperation.execute();
        recordLatency(stopwatch.stop().elapsed(), cloudOperation);
        return response;
      } catch (Exception e) {
        recordLatency(stopwatch.stop().elapsed(), cloudOperation);
        recordErrors(getHttpErrorCode(e), cloudOperation);
        executionException = Optional.of(e);
        throw e;
      } finally {
        logEvent(
            tracer.getCurrentSpan().getContext().getTraceId(),
            cloudOperation,
            cowOperation.serializeRequest(),
            executionException);
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

  /**
   * Logs cloud calls.
   *
   * <p>This log is for debug purpose when using CRL, so should be in debug level.
   *
   * @param traceId the traceId where log happens
   * @param operation the operation to log.
   * @param request the request of the log
   * @param executionException the exception to log. Optional, only presents when exception happens.
   */
  @VisibleForTesting
  void logEvent(
      TraceId traceId,
      CloudOperation operation,
      String request,
      Optional<Exception> executionException) {
    if (!logger.isDebugEnabled()) {
      return;
    }
    Gson gson = new Gson();

    Map<String, String> jsonMap = new LinkedHashMap<>();
    jsonMap.put("traceId:", traceId.toString());
    jsonMap.put("operation:", operation.name());
    jsonMap.put("clientName:", clientConfig.getClientName());

    String jsonString = gson.toJson(jsonMap);

    // Now append the already formatted request & exception.
    JsonObject jsonObject = gson.fromJson(jsonString, JsonObject.class);

    // If exception presents
    if (executionException.isPresent()) {
      // Nested Exception
      Map<String, String> exceptionMap = getExceptionMap(executionException.get());
      jsonObject.add("exception:", gson.fromJson(gson.toJson(exceptionMap), JsonObject.class));
    }

    jsonObject.add("request:", gson.fromJson(request, JsonObject.class));

    logger.debug(jsonObject.toString());
  }

  private OptionalInt getHttpErrorCode(Exception e) {
    return e instanceof BaseHttpServiceException
        ? OptionalInt.of(((BaseHttpServiceException) e).getCode())
        : OptionalInt.empty();
  }

  private Map<String, String> getExceptionMap(Exception executionException) {
    // Nested Exception
    Map<String, String> exceptionMap = new LinkedHashMap<>();
    exceptionMap.put("message", executionException.getMessage());
    exceptionMap.put(
        "errorCode",
        String.valueOf(getHttpErrorCode(executionException).orElse(GENERIC_UNKNOWN_ERROR_CODE)));
    return exceptionMap;
  }
}
