package bio.terra.cloudres.util;

import io.opencensus.stats.AggregationData;
import io.opencensus.tags.TagValue;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Tag;

import java.time.Duration;
import java.util.Arrays;
import java.util.List;

import static org.junit.Assert.*;

/**
 * Test for {@link MetricsHelper}
 */
@Tag("unit")
public class MetricsHelperTest {
    private static final String CLIENT = "TestClient";
    private static final List<TagValue> API_COUNT = Arrays.asList(TagValue.create(CLIENT), TagValue.create(CloudApiMethod.GOOGLE_CREATE_PROJECT.name()));
    private static final List<TagValue> ERROR_401_COUNT = Arrays.asList(TagValue.create(CLIENT), TagValue.create(CloudApiMethod.GOOGLE_CREATE_PROJECT.name()), TagValue.create("401"));
    private static final List<TagValue> ERROR_403_COUNT = Arrays.asList(TagValue.create(CLIENT), TagValue.create(CloudApiMethod.GOOGLE_CREATE_PROJECT.name()), TagValue.create("403"));
    private static final List<TagValue> ONE_MS_LATENCY = Arrays.asList(TagValue.create(CLIENT), TagValue.create(CloudApiMethod.GOOGLE_CREATE_PROJECT.name()), TagValue.create("1"));
    private static final List<TagValue> TEN_MS_LATENCY = Arrays.asList(TagValue.create(CLIENT), TagValue.create(CloudApiMethod.GOOGLE_CREATE_PROJECT.name()), TagValue.create("10"));

    @Test
    public void testRecordApiCount() throws Exception {
        MetricsHelper.recordApiCount(CLIENT, CloudApiMethod.GOOGLE_CREATE_PROJECT);
        MetricsHelper.recordApiCount(CLIENT, CloudApiMethod.GOOGLE_CREATE_PROJECT);
        MetricsHelper.recordApiCount(CLIENT, CloudApiMethod.GOOGLE_CREATE_PROJECT);
        MetricsHelper.recordApiCount(CLIENT, CloudApiMethod.GOOGLE_CREATE_PROJECT);

        // One cloud api count
        assertEquals(AggregationData.CountData.create(3), MetricsHelper.viewManager.getView(MetricsHelper.API_VIEW_NAME).getAggregationMap().get(API_COUNT));
        // no errors
        assertNull(MetricsHelper.viewManager.getView(MetricsHelper.ERROR_VIEW_NAME).getAggregationMap().get(ERROR_401_COUNT));
    }

    @Test
    public void testRecordErrorCount() throws Exception {
        MetricsHelper.recordError(CLIENT, CloudApiMethod.GOOGLE_CREATE_PROJECT, "401");
        MetricsHelper.recordError(CLIENT, CloudApiMethod.GOOGLE_CREATE_PROJECT, "401");
        MetricsHelper.recordError(CLIENT, CloudApiMethod.GOOGLE_CREATE_PROJECT, "401");
        MetricsHelper.recordError(CLIENT, CloudApiMethod.GOOGLE_CREATE_PROJECT, "403");

        // One cloud api count
        assertEquals(AggregationData.CountData.create(3), MetricsHelper.viewManager.getView(MetricsHelper.ERROR_VIEW_NAME).getAggregationMap().get(ERROR_401_COUNT));
        assertEquals(AggregationData.CountData.create(1), MetricsHelper.viewManager.getView(MetricsHelper.ERROR_VIEW_NAME).getAggregationMap().get(ERROR_403_COUNT));
    }

    @Test
    public void testRecordLatency() throws Exception {
        MetricsHelper.recordLatency(CLIENT, CloudApiMethod.GOOGLE_CREATE_PROJECT, Duration.ofMillis(1));
        MetricsHelper.recordLatency(CLIENT, CloudApiMethod.GOOGLE_CREATE_PROJECT, Duration.ofMillis(1));
        MetricsHelper.recordLatency(CLIENT, CloudApiMethod.GOOGLE_CREATE_PROJECT, Duration.ofMillis(100));

        // One cloud api count
        assertEquals(AggregationData.CountData.create(2), MetricsHelper.viewManager.getView(MetricsHelper.LATENCY_VIEW_NAME).getAggregationMap().get(ONE_MS_LATENCY));
        assertEquals(AggregationData.CountData.create(1), MetricsHelper.viewManager.getView(MetricsHelper.LATENCY_VIEW_NAME).getAggregationMap().get(TEN_MS_LATENCY));
    }
}
