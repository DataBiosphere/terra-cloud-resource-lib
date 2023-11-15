package bio.terra.cloudres.util;

import bio.terra.cloudres.common.CloudOperation;
import com.google.common.annotations.VisibleForTesting;
import io.opentelemetry.api.OpenTelemetry;
import io.opentelemetry.api.common.AttributeKey;
import io.opentelemetry.api.common.Attributes;
import io.opentelemetry.api.metrics.LongCounter;
import io.opentelemetry.api.metrics.LongHistogram;
import io.opentelemetry.sdk.metrics.Aggregation;
import io.opentelemetry.sdk.metrics.View;
import java.time.Duration;
import java.util.List;
import java.util.Map;
import java.util.OptionalInt;
import java.util.Set;

/** Util class to hold common variable and method used by OpenCensus in tracing and stats. */
public class MetricsHelper {
  public static final String CLOUD_RESOURCE_PREFIX = "terra/cloudresourcelibrary";
  public static final String ERROR_COUNT_METER_NAME = CLOUD_RESOURCE_PREFIX + "/cloud/error";
  public static final String API_COUNT_METER_NAME = CLOUD_RESOURCE_PREFIX + "/cloud/api";
  public static final String LATENCY_METER_NAME = CLOUD_RESOURCE_PREFIX + "/cloud/latency";

  private static final AttributeKey<String> KEY_CLIENT = AttributeKey.stringKey("client");
  private static final AttributeKey<String> KEY_CLOUD_API = AttributeKey.stringKey("cloud_api");
  public static final AttributeKey<String> KEY_ERROR = AttributeKey.stringKey("error_code");
  /** Unit string for count. */
  private static final String COUNT = "1";
  /** Unit string for millisecond. */
  private static final String MILLISECOND = "ms";

  private final LongHistogram latencyHistogram;
  private final LongCounter apiCounter;
  private final LongCounter errorCounter;

  /**
   * This bucketing is our first pass guess at what might be interesting to see for latencies. It is
   * not backed by data.
   */
  private static final Aggregation latencyDistribution =
      Aggregation.explicitBucketHistogram(
          List.of(
              0.0, 1.0, 2.0, 5.0, 10.0, 20.0, 40.0, 60.0, 80.0, 100.0, 120.0, 140.0, 160.0, 180.0,
              200.0, 300.0, 400.0, 500.0, 600.0, 700.0, 800.0, 900.0, 1000.0, 2000.0, 4000.0,
              8000.0, 16000.0, 32000.0, 64000.0));

  private static final Aggregation countAggregation = Aggregation.sum();

  /**
   * Fake HTTP status code value for errors that are not HTTP status errors. Useful for including
   * non-HTTP status errors in a single metric.
   */
  @VisibleForTesting static final int GENERIC_UNKNOWN_ERROR_CODE = 1;

  public MetricsHelper(OpenTelemetry openTelemetry) {
    var meter = openTelemetry.getMeter(MetricsHelper.class.getName());
    latencyHistogram =
        meter
            .histogramBuilder(LATENCY_METER_NAME)
            .setDescription("Latency for Google API call")
            .setUnit(MILLISECOND)
            .ofLongs()
            .build();
    apiCounter =
        meter
            .counterBuilder(API_COUNT_METER_NAME)
            .setDescription("Number of the public method being called by CRL clients")
            .setUnit(COUNT)
            .build();
    errorCounter =
        meter
            .counterBuilder(ERROR_COUNT_METER_NAME)
            .setDescription("Number of errors")
            .setUnit(COUNT)
            .build();
  }

  /**
   * Record the latency for Cloud API calls.
   *
   * @param client, the client which use this library.
   * @param method the cloud api
   * @param latency The API latency.
   */
  public void recordLatency(String client, CloudOperation method, Duration latency) {
    latencyHistogram.record(
        latency.toMillis(), Attributes.of(KEY_CLOUD_API, method.toString(), KEY_CLIENT, client));
  }

  /**
   * Records the latency for Cloud API calls.
   *
   * <p>This will be in path /cloud/error with client and methodName as tags,
   *
   * @param client, the client which use this library.
   * @param method The Cloud API calls.
   * @param httpStatusCode the httpStatusCode from cloud. If things goes wrong within CRL, use the
   *     default generic error code(1) instead.
   */
  public void recordError(String client, CloudOperation method, OptionalInt httpStatusCode) {
    int errorCode = httpStatusCode.orElse(GENERIC_UNKNOWN_ERROR_CODE);
    errorCounter.add(
        1,
        Attributes.of(
            KEY_CLOUD_API,
            method.toString(),
            KEY_CLIENT,
            client,
            KEY_ERROR,
            String.valueOf(errorCode)));
  }

  /**
   * Records API sent to Cloud API .
   *
   * <p>This will be in path /cloud/api with client and methodName as tags,
   *
   * @param client the client which use this library.
   * @param method The cloud api where error happens.
   */
  public void recordApiCount(String client, CloudOperation method) {
    apiCounter.add(1, Attributes.of(KEY_CLOUD_API, method.toString(), KEY_CLIENT, client));
  }

  public static Map<String, View> getMetricsViews() {
    var latencyView =
        View.builder()
            .setName(LATENCY_METER_NAME)
            .setDescription("The distribution of latencies")
            .setAggregation(latencyDistribution)
            .setAttributeFilter(Set.of(KEY_CLIENT.getKey(), KEY_CLOUD_API.getKey()))
            .build();
    var apiView =
        View.builder()
            .setName(API_COUNT_METER_NAME)
            .setDescription("The number of cloud api calls")
            .setAggregation(countAggregation)
            .setAttributeFilter(Set.of(KEY_CLOUD_API.getKey(), KEY_CLIENT.getKey()))
            .build();
    var errorView =
        View.builder()
            .setName(ERROR_COUNT_METER_NAME)
            .setDescription("The number and types of errors")
            .setAggregation(countAggregation)
            .setAttributeFilter(
                Set.of(KEY_ERROR.getKey(), KEY_CLOUD_API.getKey(), KEY_CLIENT.getKey()))
            .build();

    return Map.of(
        LATENCY_METER_NAME,
        latencyView,
        API_COUNT_METER_NAME,
        apiView,
        ERROR_COUNT_METER_NAME,
        errorView);
  }
}
