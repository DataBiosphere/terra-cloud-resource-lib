package bio.terra.cloudres.google.common;

import static bio.terra.cloudres.util.MetricsHelper.CLOUD_RESOURCE_PREFIX;
import static org.junit.Assert.*;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import bio.terra.cloudres.util.CloudOperation;
import bio.terra.cloudres.util.MetricsHelper;
import com.google.auth.Credentials;
import com.google.cloud.NoCredentials;
import com.google.cloud.resourcemanager.Project;
import com.google.cloud.resourcemanager.ResourceManagerException;
import io.opencensus.stats.AggregationData;
import io.opencensus.stats.View;
import io.opencensus.tags.TagValue;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.Callable;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;

/** Test for {@link GoogleResourceClientHelper} */
@Tag("unit")
public class GoogleResourceClientHelperTest {
  private static final String CLIENT = "TestClient";
  private static final List<TagValue> API_COUNT =
      Arrays.asList(
          TagValue.create(CLIENT), TagValue.create(CloudOperation.GOOGLE_CREATE_PROJECT.name()));
  private static final List<TagValue> ERROR_COUNT =
      Arrays.asList(
          TagValue.create(CLIENT),
          TagValue.create(CloudOperation.GOOGLE_CREATE_PROJECT.name()),
          null);

  private static final View.Name LATENCY_VIEW_NAME =
      View.Name.create(CLOUD_RESOURCE_PREFIX + "/cloud/latency");
  private static final View.Name API_VIEW_NAME =
      View.Name.create(CLOUD_RESOURCE_PREFIX + "/cloud/api");
  private static final View.Name ERROR_VIEW_NAME =
      View.Name.create(CLOUD_RESOURCE_PREFIX + "/cloud/error");

  private GoogleResourceClientHelper helper;
  private GoogleClientConfig options;
  private Credentials credentials;

  @Mock private Callable<Project> mockCallable = mock(Callable.class);

  @BeforeEach
  public void setUp() throws Exception {
    credentials = NoCredentials.getInstance();
    options = GoogleClientConfig.Builder.newBuilder().setClient(CLIENT).build();
    helper = new GoogleResourceClientHelper(options);
  }

  @Test
  public void testExecuteGoogleCloudCall_success() throws Exception {
    helper.executeGoogleCloudCall(mockCallable, CloudOperation.GOOGLE_CREATE_PROJECT);

    // One cloud api count
    assertEquals(
        AggregationData.CountData.create(1),
        MetricsHelper.viewManager.getView(API_VIEW_NAME).getAggregationMap().get(API_COUNT));
    // no errors
    assertNull(
        MetricsHelper.viewManager.getView(ERROR_VIEW_NAME).getAggregationMap().get(ERROR_COUNT));
  }

  @Test
  public void testExecuteGoogleCloudCall_withException() throws Exception {
    when(mockCallable.call()).thenThrow(ResourceManagerException.class);

    assertThrows(
        ResourceManagerException.class,
        () -> helper.executeGoogleCloudCall(mockCallable, CloudOperation.GOOGLE_CREATE_PROJECT));
    // One cloud api count
    assertEquals(
        AggregationData.CountData.create(1),
        MetricsHelper.viewManager.getView(API_VIEW_NAME).getAggregationMap().get(API_COUNT));
    // One cloud a errors
    assertNull(MetricsHelper.viewManager.getView(API_VIEW_NAME).getAggregationMap().get(API_COUNT));
  }
}
