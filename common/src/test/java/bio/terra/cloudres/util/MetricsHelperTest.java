package bio.terra.cloudres.util;

import static bio.terra.cloudres.testing.MetricsTestUtil.*;
import static bio.terra.cloudres.util.MetricsHelper.CLOUD_RESOURCE_PREFIX;
import static bio.terra.cloudres.util.MetricsHelper.GENERIC_UNKNOWN_ERROR_CODE;

import bio.terra.cloudres.testing.StubCloudOperation;
import io.opencensus.stats.View;
import io.opencensus.tags.TagValue;
import java.time.Duration;
import java.util.Arrays;
import java.util.List;
import java.util.OptionalInt;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

/** Test for {@link MetricsHelper} */
@Tag("unit")
public class MetricsHelperTest {
  private static final List<TagValue> ERROR_401_COUNT =
      Arrays.asList(
          TagValue.create(CLIENT),
          TagValue.create(StubCloudOperation.TEST_OPERATION.name()),
          TagValue.create("401"));
  private static final List<TagValue> ERROR_403_COUNT =
      Arrays.asList(
          TagValue.create(CLIENT),
          TagValue.create(StubCloudOperation.TEST_OPERATION.name()),
          TagValue.create("403"));
  private static final List<TagValue> ERROR_GENERIC_COUNT =
      Arrays.asList(
          TagValue.create(CLIENT),
          TagValue.create(StubCloudOperation.TEST_OPERATION.name()),
          TagValue.create(String.valueOf(GENERIC_UNKNOWN_ERROR_CODE)));

  private static final View.Name LATENCY_VIEW_NAME =
      View.Name.create(CLOUD_RESOURCE_PREFIX + "/cloud/latency");
  private static final View.Name API_VIEW_NAME =
      View.Name.create(CLOUD_RESOURCE_PREFIX + "/cloud/api");
  private static final View.Name ERROR_VIEW_NAME =
      View.Name.create(CLOUD_RESOURCE_PREFIX + "/cloud/error");

  @Test
  public void testRecordApiCount() throws Exception {
    long apiCount = getCurrentCount(API_VIEW_NAME, API_COUNT);
    MetricsHelper.recordApiCount(CLIENT, StubCloudOperation.TEST_OPERATION);
    MetricsHelper.recordApiCount(CLIENT, StubCloudOperation.TEST_OPERATION);
    MetricsHelper.recordApiCount(CLIENT, StubCloudOperation.TEST_OPERATION);
    MetricsHelper.recordApiCount(CLIENT, StubCloudOperation.TEST_OPERATION);

    sleepForSpansExport();

    assertCountIncremented(API_VIEW_NAME, API_COUNT, apiCount, 4);
  }

  @Test
  public void testRecordErrorCount() throws Exception {
    long errorCount403 = getCurrentCount(ERROR_VIEW_NAME, ERROR_403_COUNT);
    long errorCount401 = getCurrentCount(ERROR_VIEW_NAME, ERROR_401_COUNT);
    long errorCountGeneric = getCurrentCount(ERROR_VIEW_NAME, ERROR_GENERIC_COUNT);

    MetricsHelper.recordError(CLIENT, StubCloudOperation.TEST_OPERATION, OptionalInt.of(401));
    MetricsHelper.recordError(CLIENT, StubCloudOperation.TEST_OPERATION, OptionalInt.of(401));
    MetricsHelper.recordError(CLIENT, StubCloudOperation.TEST_OPERATION, OptionalInt.of(401));
    MetricsHelper.recordError(CLIENT, StubCloudOperation.TEST_OPERATION, OptionalInt.of(403));
    MetricsHelper.recordError(CLIENT, StubCloudOperation.TEST_OPERATION, OptionalInt.empty());

    sleepForSpansExport();

    assertCountIncremented(ERROR_VIEW_NAME, ERROR_401_COUNT, errorCount401, 3);
    assertCountIncremented(ERROR_VIEW_NAME, ERROR_403_COUNT, errorCount403, 1);
    assertCountIncremented(ERROR_VIEW_NAME, ERROR_GENERIC_COUNT, errorCountGeneric, 1);
  }

  @Test
  public void testRecordLatency() throws Exception {
    // this is mapped to the Distribution defined in MetricsHelper, i.e.
    // 0ms being within the first bucket & 1 ms in the 2nd.
    final int zeroMsBucketIndex = 0;
    final int oneMsBucketIndex = 1;

    long current0MsCount =
        getCurrentDistributionDataCount(LATENCY_VIEW_NAME, API_COUNT, zeroMsBucketIndex);
    long current1MsCount =
        getCurrentDistributionDataCount(LATENCY_VIEW_NAME, API_COUNT, oneMsBucketIndex);

    MetricsHelper.recordLatency(CLIENT, StubCloudOperation.TEST_OPERATION, Duration.ofMillis(1));
    MetricsHelper.recordLatency(CLIENT, StubCloudOperation.TEST_OPERATION, Duration.ofMillis(1));
    MetricsHelper.recordLatency(CLIENT, StubCloudOperation.TEST_OPERATION, Duration.ofMillis(0));

    sleepForSpansExport();

    // 1 ms,
    assertLatencyCountIncremented(
        LATENCY_VIEW_NAME, API_COUNT, current0MsCount, 1, zeroMsBucketIndex);
    // 2ms
    assertLatencyCountIncremented(
        LATENCY_VIEW_NAME, API_COUNT, current1MsCount, 2, oneMsBucketIndex);
  }
}
