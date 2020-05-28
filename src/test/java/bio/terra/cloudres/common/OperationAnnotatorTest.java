package bio.terra.cloudres.common;

import static bio.terra.cloudres.testing.MetricsTestUtil.*;
import static org.junit.Assert.assertEquals;

import bio.terra.cloudres.util.MetricsHelper;
import com.google.cloud.resourcemanager.ResourceManagerException;
import io.opencensus.stats.AggregationData;
import java.io.IOException;
import org.junit.Assert;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

/** Test for {@link OperationAnnotator} */
@Tag("unit")
public class OperationAnnotatorTest {
  private static final String CLIENT = "TestClient";

  private OperationAnnotator operationAnnotator;
  private ClientConfig clientConfig;

  @BeforeEach
  public void setUp() throws Exception {
    clientConfig = ClientConfig.Builder.newBuilder().setClient(CLIENT).build();
    operationAnnotator = new OperationAnnotator(clientConfig);
  }

  @Test
  public void testExecuteGoogleCloudCall_success() throws Exception {
    long errorCount = getCurrentCount(ERROR_VIEW_NAME, ERROR_COUNT);
    long apiCount = getCurrentCount(API_VIEW_NAME, API_COUNT);

    operationAnnotator.executeGoogleCall(
        () -> {
          try {
            Thread.sleep(4100);
          } catch (InterruptedException e) {
            throw new RuntimeException(e);
          }
          return null;
        },
        CloudOperation.GOOGLE_CREATE_PROJECT);

    sleepForSpansExport();

    // One cloud api count
    assertCountIncremented(API_VIEW_NAME, API_COUNT, apiCount, 1);

    // No error
    assertCountIncremented(ERROR_VIEW_NAME, ERROR_COUNT, errorCount, 0);

    // This rely on the latency DistributionData defined in {@link MetricHelper} where 4s - 8s are
    // in the same bucket.
    // This would expect the latency falls into the 4s-8s bucket(25th).
    AggregationData.DistributionData data =
        (AggregationData.DistributionData)
            MetricsHelper.viewManager.getView(LATENCY_VIEW_NAME).getAggregationMap().get(API_COUNT);

    // 4~8s bucket,
    assertEquals(data.getBucketCounts().get(24).longValue(), 1);
  }

  @Test
  public void testExecuteGoogleCloudCall_withException() throws Exception {
    long errorCount = getCurrentCount(ERROR_VIEW_NAME, ERROR_COUNT);
    long apiCount = getCurrentCount(API_VIEW_NAME, API_COUNT);

    Assert.assertThrows(
        ResourceManagerException.class,
        () ->
            operationAnnotator.executeGoogleCall(
                () -> {
                  throw new ResourceManagerException(new IOException("test"));
                },
                CloudOperation.GOOGLE_CREATE_PROJECT));

    sleepForSpansExport();

    // Assert cloud api count increase by 1
    assertCountIncremented(API_VIEW_NAME, API_COUNT, apiCount, 1);

    // Assert error count increase by 1
    assertCountIncremented(ERROR_VIEW_NAME, ERROR_COUNT, errorCount, 1);
  }
}
