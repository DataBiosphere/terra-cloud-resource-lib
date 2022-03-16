package bio.terra.cloudres.testing;

import static bio.terra.cloudres.util.MetricsHelper.CLOUD_RESOURCE_PREFIX;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

import bio.terra.cloudres.util.MetricsHelper;
import io.opencensus.stats.AggregationData;
import io.opencensus.stats.View;
import io.opencensus.tags.TagValue;
import java.util.Arrays;
import java.util.List;

/** Helper class for metrics in tests. */
public class MetricsTestUtil {
  public static final String CLIENT = "TestClient";
  public static final List<TagValue> API_COUNT =
      Arrays.asList(
          TagValue.create(CLIENT), TagValue.create(StubCloudOperation.TEST_OPERATION.name()));
  public static final List<TagValue> ERROR_COUNT_404 =
      Arrays.asList(
          TagValue.create(CLIENT),
          TagValue.create(StubCloudOperation.TEST_OPERATION.name()),
          TagValue.create("404"));

  public static final View.Name API_VIEW_NAME =
      View.Name.create(CLOUD_RESOURCE_PREFIX + "/cloud/api");
  public static final View.Name ERROR_VIEW_NAME =
      View.Name.create(CLOUD_RESOURCE_PREFIX + "/cloud/error");
  public static final View.Name LATENCY_VIEW_NAME =
      View.Name.create(CLOUD_RESOURCE_PREFIX + "/cloud/latency");

  /**
   * Helper method to get current stats before test.
   *
   * <p>The clear stats in opencensus is not public, so we have to keep track of each stats and
   * verify the increment.
   */
  public static long getCurrentCount(View.Name viewName, List<TagValue> tags) {
    AggregationData.CountData currentCount =
        (AggregationData.CountData)
            (MetricsHelper.viewManager.getView(viewName).getAggregationMap().get(tags));
    return currentCount == null ? 0 : currentCount.getCount();
  }

  /**
   * Assert the count is a value. 0 is equivalent to no count being present'
   *
   * <p>The clear stats in opencensus is not public, so we have to keep track of each stats and
   * verify the increment.
   */
  public static void assertCountIncremented(
      View.Name viewName, List<TagValue> tags, long previous, long increment) {
    if (previous == 0 && increment == 0) {
      assertNull(MetricsHelper.viewManager.getView(viewName).getAggregationMap().get(tags));
    } else {
      assertEquals(
          AggregationData.CountData.create(increment + previous),
          MetricsHelper.viewManager.getView(viewName).getAggregationMap().get(tags));
    }
  }

  /**
   * Helper method to get current DistributionData stats before test.
   *
   * <p>The clear stats in opencensus is not public, so we have to keep track of each stats and
   * verify the increment.
   */
  public static long getCurrentDistributionDataCount(
      View.Name viewName, List<TagValue> tags, int bucketIndex) {
    AggregationData.DistributionData data =
        (AggregationData.DistributionData)
            MetricsHelper.viewManager.getView(viewName).getAggregationMap().get(tags);
    return data == null ? 0 : data.getBucketCounts().get(bucketIndex).longValue();
  }

  /**
   * Assert the DistributionData count is a value. 0 is equivalent to no count being present.
   *
   * <p>The clear stats in opencensus is not public, so we have to keep track of each stats and
   * verify the increment.
   */
  public static void assertLatencyCountIncremented(
      View.Name viewName, List<TagValue> tags, long previous, long increment, int bucketIndex) {
    long currentCount = getCurrentDistributionDataCount(viewName, tags, bucketIndex);

    assertEquals(increment, currentCount - previous);
  }

  /**
   * Wait for a duration longer than reporting duration (5s) to ensure spans are exported.
   *
   * <p>Values from
   * https://github.com/census-instrumentation/opencensus-java/blob/5be70440b53815eec1ab59513390aadbcec5cc9c/examples/src/main/java/io/opencensus/examples/helloworld/QuickStart.java#L106
   */
  public static void sleepForSpansExport() throws InterruptedException {
    Thread.sleep(5100);
  }
}
