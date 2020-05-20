package bio.terra.cloudres.google.common;

import bio.terra.cloudres.util.CloudOperation;
import bio.terra.cloudres.util.MetricsHelper;
import com.google.cloud.resourcemanager.Project;
import com.google.cloud.resourcemanager.ResourceManagerException;
import io.opencensus.stats.AggregationData;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;

import java.util.concurrent.Callable;

import static bio.terra.cloudres.testing.MetricsTestUtil.*;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThrows;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/** Test for {@link GoogleResourceClientHelper} */
@Tag("unit")
public class GoogleResourceClientHelperTest {
  private static final String CLIENT = "TestClient";

  private GoogleResourceClientHelper helper;
  private GoogleClientConfig options;

  @Mock private Callable<Project> mockCallable = mock(Callable.class);

  @BeforeEach
  public void setUp() throws Exception {
    options = GoogleClientConfig.Builder.newBuilder().setClient(CLIENT).build();
    helper = new GoogleResourceClientHelper(options);
  }

  @Test
  public void testExecuteGoogleCloudCall_success() throws Exception {
    helper.executeGoogleCloudCall(mockCallable, CloudOperation.GOOGLE_CREATE_PROJECT);

    long errorCount = getCurrentCount(ERROR_VIEW_NAME, ERROR_COUNT);
    long apiCount = getCurrentCount(API_VIEW_NAME, API_COUNT);

    // One cloud api count
    assertEquals(
        AggregationData.CountData.create(1),
        MetricsHelper.viewManager.getView(API_VIEW_NAME).getAggregationMap().get(API_COUNT));

    assertCountNotIncrease(ERROR_VIEW_NAME, ERROR_COUNT, errorCount);
  }

  @Test
  public void testExecuteGoogleCloudCall_withException() throws Exception {
    when(mockCallable.call()).thenThrow(ResourceManagerException.class);

    long errorCount = getCurrentCount(ERROR_VIEW_NAME, ERROR_COUNT);
    long apiCount = getCurrentCount(API_VIEW_NAME, API_COUNT);

    assertThrows(
        ResourceManagerException.class,
        () -> helper.executeGoogleCloudCall(mockCallable, CloudOperation.GOOGLE_CREATE_PROJECT));

    // One cloud api count
    assertEquals(
        AggregationData.CountData.create(1 + apiCount),
        MetricsHelper.viewManager.getView(API_VIEW_NAME).getAggregationMap().get(API_COUNT));

    System.out.println();
    // One cloud a errors
    assertEquals(
        1 + errorCount,
        MetricsHelper.viewManager.getView(ERROR_VIEW_NAME).getAggregationMap().get(ERROR_COUNT));
  }
}
