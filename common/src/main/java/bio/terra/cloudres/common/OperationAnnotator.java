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
              // Wrap cowExecute in a CowExecuteCheckedException so we can use the same code even though it will never
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
      CloudOperation cloudOperation,
      CowCheckedExecute<R, E> cowExecute,
      CowSerialize cowSerialize)
      throws E {
    Optional<Exception> executionException = Optional.empty();

    try (Scope ss = tracer.spanBuilder(cloudOperation.name()).startScopedSpan()) {
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
        recordErrors(getHttpErrorCode(e), cloudOperation);
        executionException = Optional.of(e);
        throw e;
      } finally {
        logEvent(
            tracer.getCurrentSpan().getContext().getTraceId(),
            cloudOperation,
            cowSerialize.serializeRequest(),
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
      JsonObject request,
      Optional<Exception> executionException) {
    if (!logger.isDebugEnabled()) {
      return;
    }

    JsonObject logEntry = new JsonObject();
    logEntry.addProperty("traceId", traceId.toString());
    logEntry.addProperty("operation", operation.name());
    logEntry.addProperty("clientName", clientConfig.getClientName());

    executionException.ifPresent(e -> logEntry.add("exception", createExceptionEntry(e)));

    logEntry.add("request", request);

    logger.debug(logEntry.toString());
  }

  private OptionalInt getHttpErrorCode(Exception e) {
    return e instanceof BaseHttpServiceException
        ? OptionalInt.of(((BaseHttpServiceException) e).getCode())
        : OptionalInt.empty();
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

  /**
   * A bogus exception type used to make a {@link CowExecute} into a {@link
   * CowCheckedExecute}.
   */
  private static class BogusException extends Exception {}
}
