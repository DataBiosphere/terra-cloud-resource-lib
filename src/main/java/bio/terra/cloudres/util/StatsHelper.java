package bio.terra.cloudres.util;

import io.opencensus.stats.Measure;
import io.opencensus.stats.Stats;
import io.opencensus.stats.StatsRecorder;
import io.opencensus.stats.ViewManager;
import io.opencensus.tags.*;
import io.opencensus.common.Scope;
import io.opencensus.trace.Tracer;
import io.opencensus.trace.Tracing;

/** Util class to hold common variable and method used by OpenCensus in tracing and stats. */
public class StatsHelper {
    private static final Tracer tracer = Tracing.getTracer();
    private static final Tagger tagger = Tags.getTagger();
    private static final TagMetadata tagMetadata = TagMetadata.create(TagMetadata.TagTtl.UNLIMITED_PROPAGATION);
    private static final ViewManager viewManager = Stats.getViewManager();
    private static final StatsRecorder statsRecorder = Stats.getStatsRecorder();

    // Opencensus tag keys
    // The tag "client"
    private static final TagKey KEY_CLIENT = TagKey.create("client");
    // The tag "latency"
    private static final TagKey KEY_LATENCY = TagKey.create("latency");
    // The tag "cloudapi"
    private static final TagKey KEY_CLOUD_API = TagKey.create("cloud_api");
    // The tag "method"
    private static final TagKey KEY_METHOD = TagKey.create("method");
    // The tag "method"
    private static final TagKey KEY_ERROR = TagKey.create("error_code");

    private static final String CLOUD_RESOURCE_PREFIX = "terra/cloudresourcelibrary";

    /** Unit string for count. */
    private static final String COUNT = "1";
    /** Unit string for millisecond. */
    private static final String MILLISECOND = "ms";

    /** {@link Measure} for latency in milliseconds. */
    private static final Measure.MeasureDouble CLOUD_GOOGLE_API_LATENCY =
            Measure.MeasureDouble.create(
                    CLOUD_RESOURCE_PREFIX + "/cloud/latency", "Latency for Google API call", MILLISECOND);

    /**
     * {@link Measure} for number of the Cloud API calls.
     * */
    public static final Measure.MeasureDouble CLOUD_API_METHOD_COUNT =
            Measure.MeasureDouble.create(
                    CLOUD_RESOURCE_PREFIX + "/cloud/api", "Number of the public method being called by CRL clients", COUNT);

    /**
     * {@link Measure} for number of the public method being called by CRL clients.
     * */
    public static final Measure.MeasureDouble METHOD_RECEIVED =
            Measure.MeasureDouble.create(
                    CLOUD_RESOURCE_PREFIX + "/method", "Number of the public method being called by CRL clients", COUNT);

    /**
     * {@link Measure} for number of errors.
     * */
    public static final Measure.MeasureDouble ERROR_COUNT =
            Measure.MeasureDouble.create(
                    CLOUD_RESOURCE_PREFIX + "/cloud/error", "Number of the errors ", COUNT);

    /**
     * Record the latency for Cloud API calls.
     *
     * @param method The method where error happens, e.g, GoogleCloudResourceManager.createProject()
     * @param method, The error's code
     * @param latency, The client which use this library.
     * */
    public static void recordCloudApiLatency(String client, String method, Long latency) {
        TagContext tctx = tagger.emptyBuilder().put(KEY_LATENCY, TagValue.create(method), tagMetadata).put(KEY_CLIENT, TagValue.create(client), tagMetadata).build();
        try (Scope ss = tagger.withTagContext(tctx)) {
            statsRecorder.newMeasureMap().put(CLOUD_GOOGLE_API_LATENCY, latency).record(tctx);
        }
    }

    /** Records the latency for Cloud API calls.
     *
     * <p> This will be in path /cloud/latency with client and methodName as tags,
     *
     * @param method The method where error happens, e.g, GoogleCloudResourceManager.createProject()
     * @param error, the error's code
     * @param client, the client which use this library.
     * */
    public static void recordCloudError(String client, String method, String error) {
        TagContext tctx = tagger.emptyBuilder()
                .put(KEY_METHOD, TagValue.create(error), tagMetadata)
                .put(KEY_ERROR, TagValue.create(method), tagMetadata).put(KEY_CLIENT, TagValue.create(client), tagMetadata).build();
        statsRecorder.newMeasureMap().put(ERROR_COUNT, 1).record(tctx);
    }

    /** Records API sent to Cloud API .*/
    public static void recordCloudApiCount(String client, String method) {
        TagContext tctx = tagger.emptyBuilder().put(KEY_CLOUD_API, TagValue.create(method), tagMetadata).put(KEY_CLIENT, TagValue.create(client), tagMetadata).build();
        try (Scope ss = tagger.withTagContext(tctx)) {
            statsRecorder.newMeasureMap().put(ERROR_COUNT, 1).record(tctx);
        }
    }

    /** Records the number of each method are used by clients .*/
    public static void recordClientUsageCount(String client, String method) {
        TagContext tctx = tagger.emptyBuilder().put(KEY_METHOD, TagValue.create(method), tagMetadata).put(KEY_CLIENT, TagValue.create(client), tagMetadata).build();
        try (Scope ss = tagger.withTagContext(tctx)) {
            statsRecorder.newMeasureMap().put(METHOD_RECEIVED, 1).record(tctx);
        }
    }
}
