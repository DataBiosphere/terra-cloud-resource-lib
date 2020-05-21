package bio.terra.cloudres.util;

import static bio.terra.cloudres.testing.MetricsTestUtil.*;
import static bio.terra.cloudres.util.MetricsHelper.CLOUD_RESOURCE_PREFIX;
import static org.junit.Assert.assertEquals;

import bio.terra.cloudres.common.CloudOperation;
import io.opencensus.stats.AggregationData;
import io.opencensus.stats.View;
import io.opencensus.tags.TagValue;
import java.time.Duration;
import java.util.Arrays;
import java.util.List;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

/** Test for {@link MetricsHelper} */
@Tag("unit")
public class MetricsHelperTest {
  private static final List<TagValue> ERROR_401_COUNT =
      Arrays.asList(
          TagValue.create(CLIENT),
          TagValue.create(CloudOperation.GOOGLE_CREATE_PROJECT.name()),
          TagValue.create("401"));
  private static final List<TagValue> ERROR_403_COUNT =
      Arrays.asList(
          TagValue.create(CLIENT),
          TagValue.create(CloudOperation.GOOGLE_CREATE_PROJECT.name()),
          TagValue.create("403"));

  private static final View.Name LATENCY_VIEW_NAME =
      View.Name.create(CLOUD_RESOURCE_PREFIX + "/cloud/latency");
  private static final View.Name API_VIEW_NAME =
      View.Name.create(CLOUD_RESOURCE_PREFIX + "/cloud/api");
  private static final View.Name ERROR_VIEW_NAME =
      View.Name.create(CLOUD_RESOURCE_PREFIX + "/cloud/error");

  @Test
  public void testRecordApiCount() throws Exception {
    long apiCount = getCurrentCount(API_VIEW_NAME, API_COUNT);
    MetricsHelper.recordApiCount(CLIENT, CloudOperation.GOOGLE_CREATE_PROJECT);
    MetricsHelper.recordApiCount(CLIENT, CloudOperation.GOOGLE_CREATE_PROJECT);
    MetricsHelper.recordApiCount(CLIENT, CloudOperation.GOOGLE_CREATE_PROJECT);
    MetricsHelper.recordApiCount(CLIENT, CloudOperation.GOOGLE_CREATE_PROJECT);

    // Wait for a duration longer than reporting duration (5s) to ensure spans are exported.
    sleepForSpansExport();

    assertCountEquals(API_VIEW_NAME, API_COUNT, apiCount, 4);
  }

  @Test
  public void testRecordErrorCount() throws Exception {
    long errorCount403 = getCurrentCount(ERROR_VIEW_NAME, ERROR_403_COUNT);
    long errorCount401 = getCurrentCount(ERROR_VIEW_NAME, ERROR_401_COUNT);

    MetricsHelper.recordError(CLIENT, CloudOperation.GOOGLE_CREATE_PROJECT, 401);
    MetricsHelper.recordError(CLIENT, CloudOperation.GOOGLE_CREATE_PROJECT, 401);
    MetricsHelper.recordError(CLIENT, CloudOperation.GOOGLE_CREATE_PROJECT, 401);
    MetricsHelper.recordError(CLIENT, CloudOperation.GOOGLE_CREATE_PROJECT, 403);

    // Wait for a duration longer than reporting duration (5s) to ensure spans are exported.
    sleepForSpansExport();

    assertCountEquals(ERROR_VIEW_NAME, ERROR_401_COUNT, errorCount401, 3);
    assertCountEquals(ERROR_VIEW_NAME, ERROR_403_COUNT, errorCount403, 1);
  }

  @Test
  public void testRecordLatency() throws Exception {
    MetricsHelper.recordLatency(CLIENT, CloudOperation.GOOGLE_CREATE_PROJECT, Duration.ofMillis(1));
    MetricsHelper.recordLatency(CLIENT, CloudOperation.GOOGLE_CREATE_PROJECT, Duration.ofMillis(1));
    MetricsHelper.recordLatency(CLIENT, CloudOperation.GOOGLE_CREATE_PROJECT, Duration.ofMillis(0));

    // Wait for a duration longer than reporting duration (5s) to ensure spans are exported.
    sleepForSpansExport();

    AggregationData.DistributionData data =
        (AggregationData.DistributionData)
            MetricsHelper.viewManager.getView(LATENCY_VIEW_NAME).getAggregationMap().get(API_COUNT);
    // Total count
    assertEquals(data.getCount(), 3);
    // this is mapped to the Distribution defined in MetricsHelper, i.e.
    // 0ms being within the first bucket & 1 ms in the second.

    // 0 ms,
    assertEquals(data.getBucketCounts().get(0).longValue(), 1);
    // 1ms
    assertEquals(data.getBucketCounts().get(1).longValue(), 2);
  }
}
