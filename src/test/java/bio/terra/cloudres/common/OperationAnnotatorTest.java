package bio.terra.cloudres.common;

import bio.terra.cloudres.testing.MetricsTestUtil;
import bio.terra.cloudres.util.MetricsHelper;
import com.google.cloud.resourcemanager.ResourceManagerException;

import java.io.IOException;

import io.opencensus.stats.AggregationData;
import org.junit.Assert;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import static bio.terra.cloudres.testing.MetricsTestUtil.*;
import static org.junit.Assert.assertEquals;

/** Test for {@link OperationAnnotator} */
@Tag("unit")
public class OperationAnnotatorTest {
  private static final String CLIENT = "TestClient";

  private OperationAnnotator helper;
  private ClientConfig options;

  @BeforeEach
  public void setUp() throws Exception {
    options = ClientConfig.Builder.newBuilder().setClient(CLIENT).build();
    helper = new OperationAnnotator(options);
  }

  @Test
  public void testExecuteGoogleCloudCall_success() throws Exception {
    long errorCount =
        MetricsTestUtil.getCurrentCount(
            MetricsTestUtil.ERROR_VIEW_NAME, MetricsTestUtil.ERROR_COUNT);
    long apiCount =
        MetricsTestUtil.getCurrentCount(MetricsTestUtil.API_VIEW_NAME, MetricsTestUtil.API_COUNT);

    helper.executeGoogleCall(() -> {Thread.sleep(4100);
      return null;
    }, CloudOperation.GOOGLE_CREATE_PROJECT);

    sleepForSpansExport();

    // One cloud api count
    MetricsTestUtil.assertCountIncremented(
        MetricsTestUtil.API_VIEW_NAME, MetricsTestUtil.API_COUNT, apiCount, 1);

    // No error
    MetricsTestUtil.assertCountIncremented(
        MetricsTestUtil.ERROR_VIEW_NAME, MetricsTestUtil.ERROR_COUNT, errorCount, 0);

    // This rely on the latency DistributionData defined in {@link MetricHelper} where 4s - 8s are in the same bucket.
    // This would expect the latency falls into the 4s-8s bucket(25th).
    AggregationData.DistributionData data =
            (AggregationData.DistributionData)
                    MetricsHelper.viewManager.getView(LATENCY_VIEW_NAME).getAggregationMap().get(API_COUNT);
    // Total count
    assertEquals(data.getCount(), 1);
    // 4~8s bucket,
    assertEquals(data.getBucketCounts().get(24).longValue(), 1);
  }

  @Test
  public void testExecuteGoogleCloudCall_withException() throws Exception {
    long errorCount =
        MetricsTestUtil.getCurrentCount(
            MetricsTestUtil.ERROR_VIEW_NAME, MetricsTestUtil.ERROR_COUNT);
    long apiCount =
        MetricsTestUtil.getCurrentCount(MetricsTestUtil.API_VIEW_NAME, MetricsTestUtil.API_COUNT);

    Assert.assertThrows(
        ResourceManagerException.class,
        () -> helper.executeGoogleCall(() -> {throw new ResourceManagerException(new IOException("test"));}, CloudOperation.GOOGLE_CREATE_PROJECT));

    sleepForSpansExport();

    // Assert cloud api count increase by 1
    MetricsTestUtil.assertCountIncremented(
        MetricsTestUtil.API_VIEW_NAME, MetricsTestUtil.API_COUNT, apiCount, 1);

    // Assert error count increase by 1
    MetricsTestUtil.assertCountIncremented(
        MetricsTestUtil.ERROR_VIEW_NAME, MetricsTestUtil.ERROR_COUNT, errorCount, 1);
  }
}
