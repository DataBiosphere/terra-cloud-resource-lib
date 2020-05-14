package bio.terra.cloudres.google.crm;

import bio.terra.cloudres.google.common.GoogleResourceClientHelper;
import bio.terra.cloudres.google.common.GoogleResourceClientOptions;
import bio.terra.cloudres.util.CloudApiMethod;
import com.google.auth.Credentials;
import com.google.auth.oauth2.GoogleCredentials;
import com.google.cloud.resourcemanager.ProjectInfo;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Tag;
import org.mockito.Mock;
import static org.mockito.Mockito.*;

/**
 * Test for {@link GoogleCloudResourceManager}
 */
@Tag("unit")
public class GoogleCloudResourceManagerTest {
    private static final String CLIENT = "TestClient";

    private GoogleResourceClientOptions options;
    private Credentials credentials;
    private GoogleCloudResourceManager googleCloudResourceManager;

    private static final String PROJECT_ID = "projectId1";
    private static final ProjectInfo PROJECT_INFO = ProjectInfo.newBuilder(PROJECT_ID).build();

    @Mock
    private GoogleResourceClientHelper mockHelper = mock(GoogleResourceClientHelper.class);

    @BeforeEach
    public void setUp() throws Exception {
        credentials = GoogleCredentials.getApplicationDefault();
        options = GoogleResourceClientOptions.Builder.newBuilder().setCredential(credentials).setClient(CLIENT).build();
        googleCloudResourceManager = new GoogleCloudResourceManager(options);
    }

    @Test
    public void testCreateGoogleProject_success() throws Exception {
        googleCloudResourceManager.createProject(PROJECT_INFO);
        verify(mockHelper).executeGoogleCloudCall(() -> googleCloudResourceManager.createProject(PROJECT_INFO), CloudApiMethod.GOOGLE_CREATE_PROJECT);
    }
}
