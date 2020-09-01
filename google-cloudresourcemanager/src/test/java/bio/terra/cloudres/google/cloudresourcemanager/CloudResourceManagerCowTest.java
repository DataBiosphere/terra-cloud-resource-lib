package bio.terra.cloudres.google.cloudresourcemanager;

import bio.terra.cloudres.testing.IntegrationCredentials;
import bio.terra.cloudres.testing.IntegrationUtils;
import com.google.api.client.googleapis.javanet.GoogleNetHttpTransport;
import com.google.api.client.http.HttpTransport;
import com.google.api.client.json.jackson2.JacksonFactory;
import com.google.api.services.cloudresourcemanager.CloudResourceManager;
import com.google.api.services.cloudresourcemanager.CloudResourceManagerScopes;
import com.google.api.services.cloudresourcemanager.model.Operation;
import com.google.api.services.cloudresourcemanager.model.Project;
import com.google.api.services.cloudresourcemanager.model.ResourceId;
import com.google.auth.http.HttpCredentialsAdapter;
import java.io.IOException;
import java.security.GeneralSecurityException;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

@Tag("integration")
public class CloudResourceManagerCowTest {

  /** What parent resource (organizatino or folder) to create projects within. */
  // TODO(PF-67): Figure out how to pipe configuration to test.
  // Current value from vault 'config/terraform/terra/crl-test/default/container_folder_id'.
  private static final ResourceId PARENT_RESOURCE =
      new ResourceId().setType("folder").setId("866104354540");

  private static CloudResourceManagerCow defaultManager() {
    HttpTransport httpTransport;
    try {
      httpTransport = GoogleNetHttpTransport.newTrustedTransport();
    } catch (GeneralSecurityException | IOException e) {
      throw new RuntimeException("Unable to create HttpTransport.", e);
    }
    return new CloudResourceManagerCow(
        IntegrationUtils.DEFAULT_CLIENT_CONFIG,
        new CloudResourceManager.Builder(
                httpTransport,
                JacksonFactory.getDefaultInstance(),
                new HttpCredentialsAdapter(
                    IntegrationCredentials.getAdminGoogleCredentialsOrDie()
                        .createScoped(CloudResourceManagerScopes.all())))
            .setApplicationName("crl-test"));
  }

  @Test
  public void createDeleteProject() throws Exception {
    CloudResourceManagerCow managerCow = defaultManager();
    String projectId = randomProjectId();
    // TODO poll operation.
    Operation operation =
        managerCow
            .projects()
            .create(new Project().setProjectId(projectId).setParent(PARENT_RESOURCE))
            .execute();
    Thread.sleep(30000);
    managerCow.projects().delete(projectId).execute();
  }

  private static String randomProjectId() {
    // Project ids must start with a letter and be no more than 30 characters long.
    return "p" + IntegrationUtils.randomName().substring(0, 29);
  }
}
