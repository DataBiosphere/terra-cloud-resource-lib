package bio.terra.cloudres.google.common;

import bio.terra.cloudres.util.CloudOperation;
import bio.terra.cloudres.util.MetricsHelper;
import com.google.cloud.http.BaseHttpServiceException;
import io.opencensus.common.Scope;
import io.opencensus.trace.Tracer;
import io.opencensus.trace.Tracing;
import java.time.Duration;
import java.util.concurrent.Callable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/** Helper class to use Google Resource clients. */
public class GoogleResourceClientHelper {
  private final Logger logger = LoggerFactory.getLogger(GoogleResourceClientHelper.class);

  private static final Tracer tracer = Tracing.getTracer();

  private final GoogleClientConfig options;

  public GoogleResourceClientHelper(GoogleClientConfig options) {
    this.options = options;
  }

  public <R> R executeGoogleCloudCall(Callable<R> googleCall, CloudOperation cloudApiMethod)
      throws Exception {
    long startTimeNs = System.nanoTime();
    logger.debug("Executing Google Calls" + cloudApiMethod);
    try (Scope ss = tracer.spanBuilder(cloudApiMethod.name()).startScopedSpan()) {
      // Record the Cloud API usage.
      recordApiCount(cloudApiMethod);
      try {
        return googleCall.call();
      } catch (BaseHttpServiceException e) {
        logger.warn("Failed to execute Google Call for : " + cloudApiMethod);
        recordErrors(String.valueOf(e.getCode()), cloudApiMethod);
        throw e;
      } finally {
        recordLatency(startTimeNs, cloudApiMethod);
      }
    }
  }

  private void recordApiCount(CloudOperation cloudApiName) {
    MetricsHelper.recordApiCount(options.getClient(), cloudApiName);
  }

  private void recordErrors(String errorCode, CloudOperation cloudApiName) {
    MetricsHelper.recordError(options.getClient(), cloudApiName, errorCode);
  }

  private void recordLatency(long startNs, CloudOperation cloudApiName) {
    MetricsHelper.recordLatency(
        options.getClient(), cloudApiName, Duration.ofNanos(System.nanoTime() - startNs));
  }
}
