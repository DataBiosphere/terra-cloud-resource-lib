package bio.terra.cloudres.util;

import io.opencensus.common.Scope;
import io.opencensus.stats.*;
import io.opencensus.tags.*;

import java.time.Duration;
import java.util.Arrays;
import java.util.Collections;

/**
 * Util class to hold common variable and method used by OpenCensus in tracing and stats.
 */
public class MetricsHelper {
    private static final Tagger tagger = Tags.getTagger();
    private static final TagMetadata tagMetadata = TagMetadata.create(TagMetadata.TagTtl.UNLIMITED_PROPAGATION);
    private static final StatsRecorder statsRecorder = Stats.getStatsRecorder();

    // Opencensus tag keys
    // The tag "client"
    private static final TagKey KEY_CLIENT = TagKey.create("client");
    // The tag "latency"
    private static final TagKey KEY_LATENCY = TagKey.create("latency");
    // The tag "cloudapi"
    private static final TagKey KEY_CLOUD_API = TagKey.create("cloud_api");
    // The tag "method"
    private static final TagKey KEY_ERROR = TagKey.create("error_code");

    private static final String CLOUD_RESOURCE_PREFIX = "terra/cloudresourcelibrary";

    /**
     * Unit string for count.
     */
    private static final String COUNT = "1";
    /**
     * Unit string for millisecond.
     */
    private static final String MILLISECOND = "ms";

    /**
     * {@link Measure} for latency in milliseconds.
     */
    private static final Measure.MeasureDouble LATENCY =
            Measure.MeasureDouble.create(
                    CLOUD_RESOURCE_PREFIX + "/cloud/latency", "Latency for Google API call", MILLISECOND);

    /**
     * {@link Measure} for number of the Cloud API calls.
     */
    public static final Measure.MeasureDouble API_COUNT =
            Measure.MeasureDouble.create(
                    CLOUD_RESOURCE_PREFIX + "/cloud/api", "Number of the public method being called by CRL clients", COUNT);

    /**
     * {@link Measure} for number of errors.
     */
    public static final Measure.MeasureDouble ERROR_COUNT =
            Measure.MeasureDouble.create(
                    CLOUD_RESOURCE_PREFIX + "/cloud/error", "Number of the errors ", COUNT);

    private static final Aggregation latencyDistribution = Aggregation.Distribution.create(BucketBoundaries.create(
            Arrays.asList(
                    0.0, 1.0, 2.0, 5.0, 10.0, 20.0, 40.0, 60.0, 80.0, 100.0, 120.0, 140.0, 160.0, 180.0, 200.0, 300.0, 400.0,
                    500.0, 600.0, 700.0, 800.0, 900.0, 1000.0, 2000.0, 4000.0, 8000.0, 16000.0, 32000.0, 64000.0)
    ));

    // Define the count aggregation
    private static final Aggregation countAggregation = Aggregation.Count.create();

    // Define all views
    public static final View.Name LATENCY_VIEW_NAME = View.Name.create(CLOUD_RESOURCE_PREFIX + "/cloud/latency");
    public static final View.Name API_VIEW_NAME = View.Name.create(CLOUD_RESOURCE_PREFIX + "/cloud/api");
    public static final View.Name ERROR_VIEW_NAME = View.Name.create(CLOUD_RESOURCE_PREFIX + "/cloud/error");
    public static final View LATENCY_VIEW = View.create(LATENCY_VIEW_NAME, "The distribution of latencies", LATENCY, latencyDistribution, Collections.unmodifiableList(Arrays.asList(KEY_LATENCY, KEY_CLOUD_API)));
    public static final View CLOUD_API_VIEW = View.create(API_VIEW_NAME, "The number of cloud api calls", API_COUNT, countAggregation, Collections.unmodifiableList(Arrays.asList(KEY_CLOUD_API, KEY_CLIENT)));
    public static final View ERROR_VIEW = View.create(ERROR_VIEW_NAME, "The distribution of line lengths", API_COUNT, countAggregation, Collections.unmodifiableList(Arrays.asList(KEY_ERROR, KEY_CLOUD_API, KEY_CLIENT)));
    private static final View[] views = new View[]{LATENCY_VIEW, CLOUD_API_VIEW, ERROR_VIEW};

    public static final ViewManager viewManager = Stats.getViewManager();

    // Register all views
    static {
        for (View view : views)
            viewManager.registerView(view);
    }

    /**
     * Record the latency for Cloud API calls.
     *
     * @param client, the client which use this library.
     * @param method  the cloud api
     * @param latency The API latency.
     */
    public static void recordLatency(String client, CloudApiMethod method, Duration latency) {
        TagContext tctx = tagger.emptyBuilder().put(KEY_LATENCY, TagValue.create(method.toString()), tagMetadata).put(KEY_CLIENT, TagValue.create(client), tagMetadata).build();
        try (Scope ss = tagger.withTagContext(tctx)) {
            statsRecorder.newMeasureMap().put(LATENCY, latency.toMillis()).record(tctx);
        }
    }

    /**
     * Records the latency for Cloud API calls.
     *
     * <p> This will be in path /cloud/error with client and methodName as tags,
     *
     * @param method  The cloud api where error happens.
     * @param error,  the error's code
     * @param client, the client which use this library.
     */
    public static void recordError(String client, CloudApiMethod method, String error) {
        TagContext tctx = tagger.emptyBuilder()
                .put(KEY_ERROR, TagValue.create(error), tagMetadata)
                .put(KEY_CLOUD_API, TagValue.create(method.toString()), tagMetadata)
                .put(KEY_CLIENT, TagValue.create(client), tagMetadata).build();
        statsRecorder.newMeasureMap().put(ERROR_COUNT, 1).record(tctx);
    }

    /**
     * Records API sent to Cloud API .
     *
     * <p> This will be in path /cloud/api with client and methodName as tags,
     *
     * @param method  The cloud api where error happens.
     * @param client, the client which use this library.
     */
    public static void recordApiCount(String client, CloudApiMethod method) {
        TagContext tctx = tagger.emptyBuilder().put(KEY_CLOUD_API, TagValue.create(method.toString()), tagMetadata).put(KEY_CLIENT, TagValue.create(client), tagMetadata).build();
        try (Scope ss = tagger.withTagContext(tctx)) {
            statsRecorder.newMeasureMap().put(API_COUNT, 1).record(tctx);
        }
    }
}
