package bio.terra.cloudres.google.common;

import bio.terra.cloudres.util.CloudApiMethod;
import bio.terra.cloudres.util.MetricsHelper;
import com.google.auth.Credentials;
import com.google.cloud.NoCredentials;
import com.google.cloud.resourcemanager.Project;
import com.google.cloud.resourcemanager.ResourceManagerException;
import io.opencensus.stats.AggregationData;
import io.opencensus.tags.TagValue;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;

import java.util.Arrays;
import java.util.List;
import java.util.concurrent.Callable;

import static org.junit.Assert.*;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * Test for {@link GoogleResourceClientHelper}
 */
@Tag("unit")
public class GoogleResourceClientHelperTest {
    private static final String CLIENT = "TestClient";
    private static final List<TagValue> API_COUNT = Arrays.asList(TagValue.create(CLIENT), TagValue.create(CloudApiMethod.GOOGLE_CREATE_PROJECT.name()));
    private static final List<TagValue> ERROR_COUNT = Arrays.asList(TagValue.create(CLIENT), TagValue.create(CloudApiMethod.GOOGLE_CREATE_PROJECT.name()), null);

    private GoogleResourceClientHelper helper;
    private GoogleClientConfig options;
    private Credentials credentials;

    @Mock
    private Callable<Project> mockCallable = mock(Callable.class);

    @BeforeEach
    public void setUp() throws Exception {
        credentials = NoCredentials.getInstance();
        options = GoogleClientConfig.Builder.newBuilder().setCredential(credentials).setClient(CLIENT).build();
        helper = new GoogleResourceClientHelper(options);
    }

    @Test
    public void testExecuteGoogleCloudCall_success() throws Exception {
        helper.executeGoogleCloudCall(mockCallable, CloudApiMethod.GOOGLE_CREATE_PROJECT);

        // One cloud api count
        assertEquals(AggregationData.CountData.create(1), MetricsHelper.viewManager.getView(MetricsHelper.API_VIEW_NAME).getAggregationMap().get(API_COUNT));
        // no errors
        assertNull(MetricsHelper.viewManager.getView(MetricsHelper.ERROR_VIEW_NAME).getAggregationMap().get(ERROR_COUNT));
    }

    @Test
    public void testExecuteGoogleCloudCall_withException() throws Exception {
        when(mockCallable.call()).thenThrow(ResourceManagerException.class);

        assertThrows(ResourceManagerException.class, () -> helper.executeGoogleCloudCall(mockCallable, CloudApiMethod.GOOGLE_CREATE_PROJECT));
        // One cloud api count
        assertEquals(AggregationData.CountData.create(1), MetricsHelper.viewManager.getView(MetricsHelper.API_VIEW_NAME).getAggregationMap().get(API_COUNT));
        // One cloud a errors
        assertNull(MetricsHelper.viewManager.getView(MetricsHelper.API_VIEW_NAME).getAggregationMap().get(API_COUNT));
    }
}
