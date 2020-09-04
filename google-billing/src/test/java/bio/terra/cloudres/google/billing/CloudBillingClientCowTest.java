package bio.terra.cloudres.google.billing;

import static org.junit.jupiter.api.Assertions.assertEquals;

import bio.terra.cloudres.google.cloudresourcemanager.testing.ProjectUtils;
import bio.terra.cloudres.testing.IntegrationCredentials;
import bio.terra.cloudres.testing.IntegrationUtils;
import com.google.api.services.cloudresourcemanager.model.Project;
import com.google.cloud.billing.v1.ProjectBillingInfo;
import java.io.IOException;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

@Tag("integration")
public class CloudBillingClientCowTest {
  private static CloudBillingClientCow defaultBillingCow() throws IOException {
    return new CloudBillingClientCow(
        IntegrationUtils.DEFAULT_CLIENT_CONFIG,
        IntegrationCredentials.getAdminGoogleCredentialsOrDie());
  }

  @Test
  public void getSetProjectBillingInfo() throws Exception {
    Project project = ProjectUtils.executeCreateProject();
    ProjectBillingInfo projectBillingInfo =
        defaultBillingCow().getProjectBillingInfo(project.getProjectId());

    assertEquals(project.getProjectId(), projectBillingInfo.getProjectId());

    ProjectUtils.getManagerCow().projects().delete(project.getProjectId());
  }
}
