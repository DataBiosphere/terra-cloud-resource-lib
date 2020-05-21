package bio.terra.cloudres.common;

import bio.terra.cloudres.testing.MetricsTestUtil;
import com.google.cloud.resourcemanager.Project;
import com.google.cloud.resourcemanager.ResourceManagerException;
import java.util.concurrent.Callable;
import org.junit.Assert;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.Mockito;

/** Test for {@link OperationAnnotator} */
@Tag("unit")
public class OperationAnnotatorTest {
  private static final String CLIENT = "TestClient";

  private OperationAnnotator helper;
  private ClientConfig options;

  @Mock private Callable<Project> mockCallable = Mockito.mock(Callable.class);

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

    helper.executeGoogleCall(mockCallable, CloudOperation.GOOGLE_CREATE_PROJECT);

    // Wait for a duration longer than reporting duration (5s) to ensure spans are exported.
    Thread.sleep(5100);

    // One cloud api count
    MetricsTestUtil.assertCountIncrease(
        MetricsTestUtil.API_VIEW_NAME, MetricsTestUtil.API_COUNT, apiCount, 1);

    // No error
    MetricsTestUtil.assertCountNotIncrease(
        MetricsTestUtil.ERROR_VIEW_NAME, MetricsTestUtil.ERROR_COUNT, errorCount);
  }

  @Test
  public void testExecuteGoogleCloudCall_withException() throws Exception {
    long errorCount =
        MetricsTestUtil.getCurrentCount(
            MetricsTestUtil.ERROR_VIEW_NAME, MetricsTestUtil.ERROR_COUNT);
    long apiCount =
        MetricsTestUtil.getCurrentCount(MetricsTestUtil.API_VIEW_NAME, MetricsTestUtil.API_COUNT);

    Mockito.when(mockCallable.call()).thenThrow(ResourceManagerException.class);

    Assert.assertThrows(
        ResourceManagerException.class,
        () -> helper.executeGoogleCall(mockCallable, CloudOperation.GOOGLE_CREATE_PROJECT));

    // Wait for a duration longer than reporting duration (5s) to ensure spans are exported.
    Thread.sleep(5100);

    // One cloud api count
    MetricsTestUtil.assertCountIncrease(
        MetricsTestUtil.API_VIEW_NAME, MetricsTestUtil.API_COUNT, apiCount, 1);

    // One cloud a errors
    MetricsTestUtil.assertCountIncrease(
        MetricsTestUtil.ERROR_VIEW_NAME, MetricsTestUtil.ERROR_COUNT, errorCount, 1);
    // One cloud api count
  }
}
