package bio.terra.cloudres.google.crm;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThrows;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import bio.terra.cloudres.common.ClientConfig;
import com.google.cloud.resourcemanager.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.Mockito;

/** Test for {@link ResourceManagerCow} */
@Tag("unit")
public class ResourceManagerCowTest {
  private static final String CLIENT = "TestClient";
  private static final String PROJECT_ID = "1111";
  private static final ProjectInfo PROJECT_INFO = ProjectInfo.newBuilder(PROJECT_ID).build();
  private ClientConfig options;
  private ResourceManagerCow resourceManagerCow;
  @Mock private ResourceManager mockResourceManager = mock(ResourceManager.class);

  @Mock
  private ResourceManagerOptions mockResourceManagerOptions = mock(ResourceManagerOptions.class);

  @Mock private Project mockProject = mock(Project.class);

  @BeforeEach
  public void setUp() throws Exception {
    options = ClientConfig.Builder.newBuilder().setClient(CLIENT).build();

    when(mockResourceManagerOptions.getService()).thenReturn(mockResourceManager);
    when(mockResourceManager.create(PROJECT_INFO)).thenReturn(mockProject);
    resourceManagerCow = new ResourceManagerCow(options, mockResourceManagerOptions);
  }

  @Test
  public void testCreateGoogleProject_success() throws Exception {
    assertEquals(resourceManagerCow.createProject(PROJECT_INFO), mockProject);
  }

  @Test
  public void testCreateGoogleProject_error() throws Exception {
    Mockito.when(mockResourceManager.create(PROJECT_INFO))
        .thenThrow(ResourceManagerException.class);

    assertThrows(
        ResourceManagerException.class, () -> resourceManagerCow.createProject(PROJECT_INFO));
  }
}
