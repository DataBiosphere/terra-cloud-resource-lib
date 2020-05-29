package bio.terra.cloudres.util;

import static bio.terra.cloudres.common.OperationAnnotator.GENERIC_UNKNOWN_ERROR_CODE;

import bio.terra.cloudres.common.CloudOperation;
import io.opencensus.common.Scope;
import io.opencensus.stats.*;
import io.opencensus.tags.*;
import java.time.Duration;
import java.util.Arrays;
import java.util.Collections;
import java.util.OptionalInt;

/** Util class to hold common variable and method used by OpenCensus in tracing and stats. */
public class MetricsHelper {
  public static final String CLOUD_RESOURCE_PREFIX = "terra/cloudresourcelibrary";
  public static final ViewManager viewManager = Stats.getViewManager();

  private static final Tagger tagger = Tags.getTagger();
  private static final TagMetadata tagMetadata =
      TagMetadata.create(TagMetadata.TagTtl.UNLIMITED_PROPAGATION);
  private static final StatsRecorder statsRecorder = Stats.getStatsRecorder();
  private static final TagKey KEY_CLIENT = TagKey.create("client");
  private static final TagKey KEY_CLOUD_API = TagKey.create("cloud_api");
  private static final TagKey KEY_ERROR = TagKey.create("error_code");
  /** Unit string for count. */
  private static final String COUNT = "1";
  /** Unit string for millisecond. */
  private static final String MILLISECOND = "ms";
  /** {@link Measure} for latency in milliseconds. */
  private static final Measure.MeasureDouble LATENCY =
      Measure.MeasureDouble.create(
          CLOUD_RESOURCE_PREFIX + "/cloud/latency", "Latency for Google API call", MILLISECOND);
  /** {@link Measure} for number of the Cloud API calls. */
  private static final Measure.MeasureDouble API_COUNT =
      Measure.MeasureDouble.create(
          CLOUD_RESOURCE_PREFIX + "/cloud/api",
          "Number of the public method being called by CRL clients",
          COUNT);
  /** {@link Measure} for number of errors from cloud call. */
  private static final Measure.MeasureDouble ERROR_COUNT =
      Measure.MeasureDouble.create(
          CLOUD_RESOURCE_PREFIX + "/cloud/error", "Number of errors", COUNT);

  /**
   * This bucketing is our first pass guess at what might be interesting to see for latencies. It is
   * not backed by data.
   */
  private static final Aggregation latencyDistribution =
      Aggregation.Distribution.create(
          BucketBoundaries.create(
              Arrays.asList(
                  0.0, 1.0, 2.0, 5.0, 10.0, 20.0, 40.0, 60.0, 80.0, 100.0, 120.0, 140.0, 160.0,
                  180.0, 200.0, 300.0, 400.0, 500.0, 600.0, 700.0, 800.0, 900.0, 1000.0, 2000.0,
                  4000.0, 8000.0, 16000.0, 32000.0, 64000.0)));

  private static final Aggregation countAggregation = Aggregation.Count.create();
  private static final View.Name LATENCY_VIEW_NAME =
      View.Name.create(CLOUD_RESOURCE_PREFIX + "/cloud/latency");
  private static final View.Name API_VIEW_NAME =
      View.Name.create(CLOUD_RESOURCE_PREFIX + "/cloud/api");
  private static final View.Name ERROR_VIEW_NAME =
      View.Name.create(CLOUD_RESOURCE_PREFIX + "/cloud/error");
  private static final View LATENCY_VIEW =
      View.create(
          LATENCY_VIEW_NAME,
          "The distribution of latencies",
          LATENCY,
          latencyDistribution,
          Collections.unmodifiableList(Arrays.asList(KEY_CLIENT, KEY_CLOUD_API)));
  private static final View CLOUD_API_VIEW =
      View.create(
          API_VIEW_NAME,
          "The number of cloud api calls",
          API_COUNT,
          countAggregation,
          Collections.unmodifiableList(Arrays.asList(KEY_CLOUD_API, KEY_CLIENT)));
  private static final View ERROR_VIEW =
      View.create(
          ERROR_VIEW_NAME,
          "The number and types of errors",
          ERROR_COUNT,
          countAggregation,
          Collections.unmodifiableList(Arrays.asList(KEY_ERROR, KEY_CLOUD_API, KEY_CLIENT)));
  private static final View[] views = new View[] {LATENCY_VIEW, CLOUD_API_VIEW, ERROR_VIEW};

  // Register all views
  static {
    for (View view : views) viewManager.registerView(view);
  }

  /**
   * Record the latency for Cloud API calls.
   *
   * @param client, the client which use this library.
   * @param method the cloud api
   * @param latency The API latency.
   */
  public static void recordLatency(String client, CloudOperation method, Duration latency) {
    TagContext tctx =
        tagger
            .emptyBuilder()
            .put(KEY_CLOUD_API, TagValue.create(method.toString()), tagMetadata)
            .put(KEY_CLIENT, TagValue.create(client), tagMetadata)
            .build();
    try (Scope ss = tagger.withTagContext(tctx)) {
      statsRecorder.newMeasureMap().put(LATENCY, latency.toMillis()).record(tctx);
    }
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
  public static void recordError(String client, CloudOperation method, OptionalInt httpStatusCode) {
    int errorCode = httpStatusCode.orElse(GENERIC_UNKNOWN_ERROR_CODE);

    TagContext tctx =
        tagger
            .emptyBuilder()
            .put(KEY_ERROR, TagValue.create(String.valueOf(errorCode)), tagMetadata)
            .put(KEY_CLOUD_API, TagValue.create(method.toString()), tagMetadata)
            .put(KEY_CLIENT, TagValue.create(client), tagMetadata)
            .build();
    try (Scope ss = tagger.withTagContext(tctx)) {
      statsRecorder.newMeasureMap().put(ERROR_COUNT, 1).record(tctx);
    }
  }

  /**
   * Records API sent to Cloud API .
   *
   * <p>This will be in path /cloud/api with client and methodName as tags,
   *
   * @param client the client which use this library.
   * @param method The cloud api where error happens.
   */
  public static void recordApiCount(String client, CloudOperation method) {
    TagContext tctx =
        tagger
            .emptyBuilder()
            .put(KEY_CLOUD_API, TagValue.create(method.toString()), tagMetadata)
            .put(KEY_CLIENT, TagValue.create(client), tagMetadata)
            .build();
    try (Scope ss = tagger.withTagContext(tctx)) {
      statsRecorder.newMeasureMap().put(API_COUNT, 1).record(tctx);
    }
  }
}
