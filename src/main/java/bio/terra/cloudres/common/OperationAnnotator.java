package bio.terra.cloudres.common;

import bio.terra.cloudres.util.JsonConverter;
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
  private final Logger logger;

  public OperationAnnotator(ClientConfig clientConfig, Logger logger) {
    this.clientConfig = clientConfig;
    this.logger = logger;
  }

  /**
   * Executes the Google call.
   *
   * @param cowOperation: the {@link CowOperation} whichs contains all necessary information to
   *     execute this call.
   * @return the response from cloud
   */
  public <R> R executeGoogleCall(CowOperation<R> cowOperation) {
    CloudOperation cloudOperation = cowOperation.getCloudOperation();
    Optional<Exception> exception = Optional.empty();

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
        exception = Optional.of(e);
        throw e;
      } finally {
        logEvent(
            /*traceId=*/ tracer.getCurrentSpan().getContext().getTraceId(),
            /* operation=*/ cloudOperation,
            /* request=*/ cowOperation.serializeRequest(),
            /* exception=*/ exception);
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

  /**
   * Logs cloud calls. This should be in debug level.
   *
   * @param traceId the traceId where log happens
   * @param operation the operation to log.
   * @param request the request of the log
   * @param exception the exception to log. Optional, only presents when exception happens.
   */
  @VisibleForTesting
  void logEvent(
      TraceId traceId, CloudOperation operation, String request, Optional<Exception> exception) {
    if (logger.isDebugEnabled()) {
      Map<String, String> jsonMap = new LinkedHashMap<>();
      jsonMap.put("traceId:", traceId.toString());
      jsonMap.put("operation:", operation.name());
      jsonMap.put("clientName:", clientConfig.getClientName());

      String jsonString = JsonConverter.convert(jsonMap);

      // Now append the already formatted request & exception.
      Gson gson = new Gson();
      JsonObject jsonObject = gson.fromJson(jsonString, JsonObject.class);

      // If exception presents
      if (exception.isPresent()) {
        // Nested Exception
        Map<String, String> exceptionMap = new LinkedHashMap<>();
        exceptionMap.put("message", exception.get().getMessage());
        exceptionMap.put(
            "errorCode",
            String.valueOf(getHttpErrorCode(exception.get()).orElse(GENERIC_UNKNOWN_ERROR_CODE)));

        JsonConverter.appendFormattedString(
            jsonObject, "exception:", JsonConverter.convert(exceptionMap));
      }

      JsonConverter.appendFormattedString(jsonObject, "request:", request);
      logger.debug(jsonObject.toString());
    }
  }
}
