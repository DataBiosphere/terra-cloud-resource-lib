package bio.terra.cloudres.testing;

import bio.terra.cloudres.common.CloudOperation;
import bio.terra.cloudres.util.MetricsHelper;
import io.opencensus.stats.AggregationData;
import io.opencensus.stats.View;
import io.opencensus.tags.TagValue;

import java.util.Arrays;
import java.util.List;

import static bio.terra.cloudres.util.MetricsHelper.CLOUD_RESOURCE_PREFIX;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

/** Helper class for metrics in tests. */
public class MetricsTestUtil {
  public static final String CLIENT = "TestClient";
  public static final List<TagValue> API_COUNT =
      Arrays.asList(
          TagValue.create(CLIENT), TagValue.create(CloudOperation.GOOGLE_CREATE_PROJECT.name()));
  public static final List<TagValue> ERROR_COUNT =
      Arrays.asList(
          TagValue.create(CLIENT),
          TagValue.create(CloudOperation.GOOGLE_CREATE_PROJECT.name()),
          TagValue.create("0"));

  public static final View.Name API_VIEW_NAME =
      View.Name.create(CLOUD_RESOURCE_PREFIX + "/cloud/api");
  public static final View.Name ERROR_VIEW_NAME =
      View.Name.create(CLOUD_RESOURCE_PREFIX + "/cloud/error");

  /**
   * Helper method to get current stats before test.
   *
   * <p>The clear stats in opencensue is not public, so we have to keep track of each stats and
   * verify the increment.
   */
  public static long getCurrentCount(View.Name viewName, List<TagValue> tags) {
    AggregationData.CountData currentErrorCount =
        (AggregationData.CountData)
            (MetricsHelper.viewManager.getView(viewName).getAggregationMap().get(tags));
    return currentErrorCount == null ? 0 : currentErrorCount.getCount();
  }

  /**
   * Assert the count increment not increase
   *
   * <p>The clear stats in opencensue is not public, so we have to keep track of each stats and
   * verify the increment.
   */
  public static void assertCountNotIncrease(
      View.Name viewName, List<TagValue> tags, long currentCount) {
    if (currentCount == 0) {
      assertNull(MetricsHelper.viewManager.getView(viewName).getAggregationMap().get(tags));
    } else {
      assertEquals(
          currentCount, MetricsHelper.viewManager.getView(viewName).getAggregationMap().get(tags));
    }
  }

  /**
   * Assert the count increment increase by one
   *
   * <p>The clear stats in opencensue is not public, so we have to keep track of each stats and
   * verify the increment.
   */
  public static void assertCountIncrease(
      View.Name viewName, List<TagValue> tags, long currentCount, long expected) {
    assertEquals(
        AggregationData.CountData.create(expected + currentCount),
        MetricsHelper.viewManager.getView(viewName).getAggregationMap().get(tags));
  }
}
