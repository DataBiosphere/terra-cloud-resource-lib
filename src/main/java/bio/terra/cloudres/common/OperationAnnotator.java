package bio.terra.cloudres.common;

import static io.opentelemetry.semconv.SemanticAttributes.HTTP_RESPONSE_STATUS_CODE;

import bio.terra.cloudres.util.MetricsHelper;
import com.google.api.client.http.HttpResponseException;
import com.google.api.client.http.HttpStatusCodes;
import com.google.api.gax.rpc.ApiException;
import com.google.cloud.http.BaseHttpServiceException;
import com.google.common.annotations.VisibleForTesting;
import com.google.common.base.Stopwatch;
import com.google.gson.Gson;
import com.google.gson.JsonObject;
import java.time.Duration;
import java.util.Optional;
import java.util.OptionalInt;
import org.slf4j.Logger;

import io.opentelemetry.api.trace.Span;
import io.opentelemetry.api.trace.Tracer;
import io.opentelemetry.context.Scope;
import software.amazon.awssdk.core.exception.SdkServiceException;

/** Annotates executing cloud operations with logs, traces, and metrics to record what happens. */
public class OperationAnnotator {
  private final Tracer tracer;

  private final ClientConfig clientConfig;

  /** We inject a Logger to allow how logs are made to be controlled by the COWs. */
  private final Logger logger;

  public OperationAnnotator(ClientConfig clientConfig, Logger logger) {
    this.clientConfig = clientConfig;
    this.logger = logger;
    this.tracer = clientConfig.getOpenTelemetry().getTracer(OperationAnnotator.class.getName());
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
   * Executes the CowOperation and allows for checked exceptions.
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
    OptionalInt httpStatusCode = OptionalInt.of(HttpStatusCodes.STATUS_CODE_OK);
    Span span = tracer.spanBuilder(cloudOperation.name()).startSpan();

    Stopwatch stopwatch = Stopwatch.createStarted();
    try(Scope ignored = span.makeCurrent()) {
      R response = cowExecute.execute();
      return response;
    } catch (Exception e) {
      // TODO(yonghao): Add success/error tag for latency for us to track differentiate latency in
      // different scenarios.
      httpStatusCode = getHttpErrorCode(e);
      executionException = Optional.of(e);
      throw e;
    } finally {
      recordOperation(
          OperationData.builder()
              .setCloudOperation(cloudOperation)
              .setDuration(stopwatch.elapsed())
              .setExecutionException(executionException)
              .setHttpStatusCode(httpStatusCode)
              .setRequestData(cowSerialize.serializeRequest())
              .build());

      // We manually manage the span so that the expected span is still present in catch and
      // finally.
      // See warning on SpanBuilder#startScopedSpan.
      // Trace the http status code
      httpStatusCode.ifPresent(s -> span.setAttribute(HTTP_RESPONSE_STATUS_CODE, s));
      span.end();
    }
  }

  /**
   * Records the information captured in {@link OperationData} via logs, tracing and metrics.
   *
   * <p>This method should be used to capture the result of an already-executed cloud operation.
   * {@link #executeCowOperation(CloudOperation, CowExecute, CowSerialize)} or {@link
   * #executeCheckedCowOperation(CloudOperation, CowCheckedExecute, CowSerialize)} should be used to
   * actually execute the operation and record resulting data.
   *
   * @param operationData the {@link OperationData} to record.
   */
  public void recordOperation(OperationData operationData) {
    // Record the Cloud API usage as a metric
    recordApiCount(operationData.cloudOperation());

    // Record the latency as a metric
    recordLatency(operationData.duration(), operationData.cloudOperation());

    // Record errors as a metric
    recordErrors(operationData.httpStatusCode(), operationData.cloudOperation());

    // Log the event
    logEvent(operationData);
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
   * Logs the information captured in {@link OperationData} indicating the completion of a CRL event
   * or that an exception occurred.
   *
   * <p>A structured JsonObject is included in the logging arguments; this payload will not affect
   * human-readable logging output, but will be included in JSON-formatted output for services which
   * are using the terra-common-lib logging library with JSON format enabled.
   *
   * <p>If an exception is present, it will also be included as a logging argument. SLF4J should
   * recognize this argument and include a stacktrace in the resulting error log message.
   *
   * @param operationData the {@link OperationData} associated with a cloud operation.
   */
  @VisibleForTesting
  void logEvent(OperationData operationData) {
    JsonObject logData = new JsonObject();
    logData.addProperty("clientName", clientConfig.getClientName());
    logData.addProperty("durationMs", operationData.duration().toMillis());
    operationData.httpStatusCode().ifPresent(s -> logData.addProperty("httpStatusCode", s));
    operationData.tryCount().ifPresent(c -> logData.addProperty("tryCount", c));
    operationData
        .executionException()
        .ifPresent(e -> logData.add("exception", createExceptionEntry(e)));
    logData.addProperty("operation", operationData.cloudOperation().name());
    logData.add("requestData", operationData.requestData());

    if (operationData.executionException().isPresent()) {
      logger.debug(
          String.format(
              "CRL exception in %s (HTTP code %s, %s)",
              operationData.cloudOperation().name(),
              operationData.httpStatusCode().orElse(-1),
              prettyPrintDuration(operationData.duration())),
          // Include logData for terra-common-lib logging to pick up and include in JSON output.
          logData,
          // Include the exception, which slf4j will append to the formatted log message.
          operationData.executionException().get());
    } else {
      logger.debug(
          String.format(
              "CRL completed %s (%s)",
              operationData.cloudOperation().name(), prettyPrintDuration(operationData.duration())),
          // Include logData for terra-common-lib logging to pick up and include in JSON output.
          logData);
    }
  }

  // Turns the default Duration.toString output into a more human-readable output (e.g. "4h 2m 3s").
  // See https://stackoverflow.com/a/40487511 for inspiration.
  private String prettyPrintDuration(Duration duration) {
    return Duration
        // Truncate to 0.1 second precision.
        .ofMillis((duration.toMillis() / 100l) * 100l)
        // Format the duration and apply some fine-tuning to the string output.
        .toString()
        // Remove the "PT" prefix
        .substring(2)
        // Add spaces between tokens
        .replaceAll("(\\d[HMS])(?!$)", "$1 ")
        .toLowerCase();
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
    // Base class for AWS SDK service exceptions.
    if (e instanceof SdkServiceException) {
      return OptionalInt.of(((SdkServiceException) e).statusCode());
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
