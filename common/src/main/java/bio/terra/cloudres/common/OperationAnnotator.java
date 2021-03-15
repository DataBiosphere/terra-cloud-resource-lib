package bio.terra.cloudres.common;

import bio.terra.cloudres.util.MetricsHelper;
import com.google.api.client.http.HttpResponseException;
import com.google.api.gax.rpc.ApiException;
import com.google.cloud.http.BaseHttpServiceException;
import com.google.common.annotations.VisibleForTesting;
import com.google.common.base.Stopwatch;
import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonNull;
import com.google.gson.JsonObject;
import io.opencensus.trace.*;
import java.time.Duration;
import java.util.Optional;
import java.util.OptionalInt;
import org.slf4j.Logger;

/** Annotates executing cloud operations with logs, traces, and metrics to record what happens. */
public class OperationAnnotator {
  private static final Tracer tracer = Tracing.getTracer();

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
   * @param cloudOperation: the {@link CloudOperation} to operate.
   * @param cowExecute: how to execute this cloud operation
   * @param cowSerialize: how to serialize request
   * @return the result of executing the {@code cowOperation}
   */
  public <R> R executeCowOperation(
      CloudOperation cloudOperation, CowExecute<R> cowExecute, CowSerialize cowSerialize) {
    try {
      return executeCheckedCowOperation(
          cloudOperation,
          // Wrap cowExecute in a CowExecuteCheckedException so we can use the same code even though
          // it will never
          // throw a checked exception.
          (CowCheckedExecute<R, BogusException>) cowExecute::execute,
          cowSerialize);
    } catch (BogusException e) {
      throw new AssertionError("Our BogusException should never be thrown by cowExecute.", e);
    }
  }

  /**
   * Executes the CowOperation and allows for checked exceptions..
   *
   * @param cloudOperation: the {@link CloudOperation} to operate.
   * @param cowExecute: how to execute this cloud operation
   * @param cowSerialize: how to serialize request
   * @return the result of executing the {@code cowOperation}
   */
  public <R, E extends Exception> R executeCheckedCowOperation(
      CloudOperation cloudOperation, CowCheckedExecute<R, E> cowExecute, CowSerialize cowSerialize)
      throws E {
    Optional<Exception> executionException = Optional.empty();
    Span span = tracer.spanBuilder(cloudOperation.name()).startSpan();

    // Record the Cloud API usage.
    recordApiCount(cloudOperation);

    Stopwatch stopwatch = Stopwatch.createStarted();
    try {
      R response = cowExecute.execute();
      recordLatency(stopwatch.stop().elapsed(), cloudOperation);
      return response;
    } catch (Exception e) {
      // TODO(yonghao): Add success/error tag for latency for us to track differentiate latency in
      // different scenarios.
      recordLatency(stopwatch.stop().elapsed(), cloudOperation);
      OptionalInt httpErrorCode = getHttpErrorCode(e);
      tracer
          .getCurrentSpan()
          .putAttribute(
              "/terra/crl/httpErrorCode",
              AttributeValue.longAttributeValue(httpErrorCode.orElse(-1)));
      recordErrors(getHttpErrorCode(e), cloudOperation);
      executionException = Optional.of(e);
      throw e;
    } finally {
      logEvent(
          cloudOperation,
          cowSerialize.serializeRequest(),
          stopwatch.elapsed(),
          executionException);
      // We manually manage the span so that the expected span is still present in catch and
      // finally.
      // See warning on SpanBuilder#startScopedSpan.
      span.end();
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
   * Logs an info message indicating the completion of a CRL event, or an error message indicating
   * an exception occurred.
   *
   * <p>A structured JsonObject is included in the logging arguments; this payload will not affect
   * human-readable logging output, but will be included in JSON-formatted output for services which
   * are using the terra-common-lib logging library with JSON format enabled.
   *
   * <p>If an exception is present, it will also be included as a logging argument. SLF4J should
   * recognize this argument and include a stacktrace in the resulting error log message.
   *
   * @param operation the cloud operation to log.
   * @param requestData data included in the cloud operation request
   * @param duration the duration of the CRL event
   * @param executionException Optional, only included if an error occurred.
   */
  @VisibleForTesting
  void logEvent(
      CloudOperation operation,
      JsonObject requestData,
      Duration duration,
      Optional<Exception> executionException) {
    JsonObject logData = new JsonObject();
    logData.addProperty("clientName", clientConfig.getClientName());
    logData.addProperty("durationMs", duration.toMillis());
    executionException.ifPresent(e -> logData.add("exception", createExceptionEntry(e)));
    logData.addProperty("operation", operation.name());
    logData.add("requestData", requestData);

    if (executionException.isPresent()) {
      OptionalInt httpErrorCode = getHttpErrorCode(executionException.get());
      logger.error(
          String.format(
              "CRL exception in %s (HTTP code %s, %s)",
              operation.name(), httpErrorCode.orElse(-1), prettyPrintDuration(duration)),
          // Include logData for terra-common-lib logging to pick up and include in JSON output.
          logData,
          // Include the exception, which slf4j will append to the formatted log message.
          executionException.get());
    } else {
      logger.debug(
          String.format("CRL completed %s (%s)", operation.name(), prettyPrintDuration(duration)),
          // Include logData for terra-common-lib logging to pick up and include in JSON output.
          logData);
    }
  }

  private String prettyPrintDuration(Duration duration) {
    // Truncate to 0.1 second precision.
    duration = Duration.ofMillis((duration.toMillis() / 100l) * 100l);
    return duration.toString().substring(2).replaceAll("(\\d[HMS])(?!$)", "$1 ").toLowerCase();
  }

  private OptionalInt getHttpErrorCode(Exception e) {
    // com.google.cloud library standard HTTP exception.
    if (e instanceof BaseHttpServiceException) {
      return OptionalInt.of(((BaseHttpServiceException) e).getCode());
    }
    // com.google.cloud library standard gRPC exception. Not technically an http error code, but
    // equivalent.
    if (e instanceof ApiException) {
      return OptionalInt.of(((ApiException) e).getStatusCode().getCode().getHttpStatusCode());
    }
    // com.google.api library standard HTTP exception.
    if (e instanceof HttpResponseException) {
      return OptionalInt.of(((HttpResponseException) e).getStatusCode());
    }
    return OptionalInt.empty();
  }

  private JsonObject createExceptionEntry(Exception executionException) {
    Gson gson = new Gson();
    JsonObject exceptionEntry = new JsonObject();
    exceptionEntry.addProperty("message", executionException.getMessage());
    getHttpErrorCode(executionException)
        .ifPresent(i -> exceptionEntry.addProperty("errorCode", String.valueOf(i)));

    return exceptionEntry;
  }

  /** How to execute this operation */
  @FunctionalInterface
  public interface CowExecute<R> {
    R execute();
  }

  /** How to execute this operation. Like {@link CowExecute}, but allows for checked exceptions. */
  @FunctionalInterface
  public interface CowCheckedExecute<R, E extends Exception> {
    R execute() throws E;
  }

  /** How to serialize Request */
  @FunctionalInterface
  public interface CowSerialize {
    JsonObject serializeRequest();
  }

  /** A bogus exception type used to make a {@link CowExecute} into a {@link CowCheckedExecute}. */
  private static class BogusException extends Exception {}
}
