package bio.terra.cloudres.google.crm;

import bio.terra.cloudres.common.ClientConfig;
import com.google.cloud.resourcemanager.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.Mockito;

import java.io.IOException;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThrows;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/** Test for {@link GoogleCloudResourceManager} */
@Tag("unit")
public class GoogleCloudResourceManagerTest {
  private static final String CLIENT = "TestClient";
  private static final String PROJECT_ID = "1111";
  private static final ProjectInfo PROJECT_INFO = ProjectInfo.newBuilder(PROJECT_ID).build();
  private ClientConfig options;
  private GoogleCloudResourceManager googleCloudResourceManager;
  @Mock private ResourceManager mockResourceManager = mock(ResourceManager.class);

  @Mock
  private ResourceManagerOptions mockResourceManagerOptions = mock(ResourceManagerOptions.class);

  @Mock private Project mockProject = mock(Project.class);

  @BeforeEach
  public void setUp() throws Exception {
    options = ClientConfig.Builder.newBuilder().setClient(CLIENT).build();

    when(mockResourceManagerOptions.getService()).thenReturn(mockResourceManager);
    when(mockResourceManager.create(PROJECT_INFO)).thenReturn(mockProject);
    googleCloudResourceManager =
        new GoogleCloudResourceManager(options, mockResourceManagerOptions);
  }

  @Test
  public void testCreateGoogleProject_success() throws Exception {
    assertEquals(googleCloudResourceManager.createProject(PROJECT_INFO), mockProject);
  }

  @Test
  public void testCreateGoogleProject_error() throws Exception {
    Mockito.when(mockResourceManager.create(PROJECT_INFO)).thenThrow(ResourceManagerException.class);

    assertThrows(ResourceManagerException.class, () -> googleCloudResourceManager.createProject(PROJECT_INFO));
  }
}
