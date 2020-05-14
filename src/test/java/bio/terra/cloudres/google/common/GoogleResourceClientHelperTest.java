package bio.terra.cloudres.google.common;

import bio.terra.cloudres.util.CloudApiMethod;
import bio.terra.cloudres.util.CloudResourceException;
import bio.terra.cloudres.util.MetricsHelper;
import com.google.auth.Credentials;
import com.google.auth.oauth2.GoogleCredentials;
import com.google.cloud.resourcemanager.Project;
import com.google.cloud.resourcemanager.ResourceManagerException;
import io.opencensus.stats.AggregationData;
import io.opencensus.stats.MeasureMap;
import io.opencensus.stats.Stats;
import io.opencensus.stats.StatsRecorder;
import io.opencensus.tags.TagValue;
import org.junit.Before;
import org.junit.Rule;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Tag;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnit;
import org.mockito.junit.MockitoRule;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Callable;

import static org.mockito.Mockito.*;
import static org.junit.Assert.*;

/**
 * Test for {@link GoogleResourceClientHelper}
 */
@Tag("unit")
public class GoogleResourceClientHelperTest {
    private static final String CLIENT = "TestClient";
    private static final List<TagValue> CLOUD_API_COUNT = Arrays.asList(TagValue.create(CLIENT), TagValue.create(CloudApiMethod.GOOGLE_CREATE_PROJECT.name()));
    private static final List<TagValue> CLOUD_ERROR_COUNT = Arrays.asList(TagValue.create(CLIENT), TagValue.create(CloudApiMethod.GOOGLE_CREATE_PROJECT.name()), null);

    private GoogleResourceClientHelper helper;
    private GoogleResourceClientOptions options;
    private Credentials credentials;

    @Mock
    private Callable<Project> mockCallable = mock(Callable.class);

    @BeforeEach
    public void setUp() throws Exception {
        credentials = GoogleCredentials.getApplicationDefault();
        options = GoogleResourceClientOptions.Builder.newBuilder().setCredential(credentials).setClient(CLIENT).build();
        helper = new GoogleResourceClientHelper(options);
    }

    @Test
    public void testExecuteGoogleCloudCall_success() throws Exception {
        helper.executeGoogleCloudCall(mockCallable, CloudApiMethod.GOOGLE_CREATE_PROJECT);

        // One cloud api count
        assertEquals(AggregationData.CountData.create(1), MetricsHelper.viewManager.getView(MetricsHelper.CLOUD_API_VIEW_NAME).getAggregationMap().get(CLOUD_API_COUNT));
        // no errors
        assertNull(MetricsHelper.viewManager.getView(MetricsHelper.CLOUD_ERROR_VIEW_NAME).getAggregationMap().get(CLOUD_ERROR_COUNT));
    }

    @Test
    public void testExecuteGoogleCloudCall_withException() throws Exception {
        when(mockCallable.call()).thenThrow(ResourceManagerException.class);

        assertThrows(CloudResourceException.class, () -> helper.executeGoogleCloudCall(mockCallable, CloudApiMethod.GOOGLE_CREATE_PROJECT));
        // One cloud api count
        assertEquals(AggregationData.CountData.create(1), MetricsHelper.viewManager.getView(MetricsHelper.CLOUD_API_VIEW_NAME).getAggregationMap().get(CLOUD_API_COUNT));
        // One cloud a errors
        assertNull(MetricsHelper.viewManager.getView(MetricsHelper.CLOUD_API_VIEW_NAME).getAggregationMap().get(CLOUD_API_COUNT));
    }
}
