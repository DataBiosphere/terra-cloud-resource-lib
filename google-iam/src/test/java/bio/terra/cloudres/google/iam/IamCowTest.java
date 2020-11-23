package bio.terra.cloudres.google.iam;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.*;

import bio.terra.cloudres.google.billing.testing.CloudBillingUtils;
import bio.terra.cloudres.google.cloudresourcemanager.testing.ProjectUtils;
import bio.terra.cloudres.google.iam.testing.ServiceUsageUtils;
import bio.terra.cloudres.testing.IntegrationCredentials;
import bio.terra.cloudres.testing.IntegrationUtils;
import com.google.api.services.cloudresourcemanager.model.Project;
import com.google.api.services.iam.v1.Iam.Projects.ServiceAccounts;
import com.google.api.services.iam.v1.model.CreateServiceAccountRequest;
import com.google.api.services.iam.v1.model.ServiceAccount;
import com.google.common.collect.ImmutableList;
import java.io.IOException;
import java.security.GeneralSecurityException;
import org.hamcrest.Matchers;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

@Tag("integration")
public class IamCowTest {
  // TODO(PF-67): Find solution for piping configs and secrets.
  private static final String BILLING_ACCOUNT_NAME = "billingAccounts/01A82E-CA8A14-367457";

  private static IamCow defaultIam() throws GeneralSecurityException, IOException {
    return IamCow.create(
        IntegrationUtils.DEFAULT_CLIENT_CONFIG,
        IntegrationCredentials.getAdminGoogleCredentialsOrDie());
  }

  @Test
  public void createAndListAndDeleteServiceAccount() throws Exception {
    IamCow iam = defaultIam();
    Project project = createPreparedProject();
    String projectId = project.getProjectId();
    String projectName = "projects/" + projectId;
    String accountId = randomSserviceAccountIdName();
    ServiceAccount serviceAccount =
        iam.projects()
            .serviceAccounts()
            .create(
                "projects/" + projectId, new CreateServiceAccountRequest().setAccountId(accountId))
            .execute();
    String fullSaName = fullServiceAccountName(projectId, serviceAccount.getEmail());
    // Sleep for 3s to make get after create work.
    Thread.sleep(3000);
    assertThat(
        iam.projects().serviceAccounts().list(projectName).execute().getAccounts(),
        Matchers.contains(serviceAccount));

    iam.projects().serviceAccounts().delete(fullSaName).execute();
    // Sleep for 3s to make get after delete work.
    Thread.sleep(3000);
    assertNull(iam.projects().serviceAccounts().list(projectName).execute().getAccounts());
  }

  @Test
  public void createServiceAccountSerialize() throws Exception {
    IamCow.Projects.ServiceAccounts.Create create =
        defaultIam()
            .projects()
            .serviceAccounts()
            .create(
                "projects/my-project", new CreateServiceAccountRequest().setAccountId("accountId"));

    assertEquals(
        "{\"name\":\"projects/my-project\",\"content\":{\"accountId\":\"accountId\"}}",
        create.serialize().toString());
  }

  @Test
  public void listServiceAccountSerialize() throws Exception {
    IamCow.Projects.ServiceAccounts.List list =
        defaultIam().projects().serviceAccounts().list("projects/project-id");

    assertEquals("{\"project_name\":\"projects/project-id\"}", list.serialize().toString());
  }

  @Test
  public void deleteServiceAccountSerialize() throws Exception {
    IamCow.Projects.ServiceAccounts.Delete delete =
        defaultIam()
            .projects()
            .serviceAccounts()
            .delete(fullServiceAccountName("projectId", "saEmail"));

    assertEquals(
        "{\"name\":\"projects/projectId/serviceAccounts/saEmail\"}", delete.serialize().toString());
  }

  /** Create Project then set billing account, enable IAM api */
  private static Project createPreparedProject() throws Exception {
    Project project = ProjectUtils.executeCreateProject();
    CloudBillingUtils.setProjectBillingInfo(project.getProjectId(), BILLING_ACCOUNT_NAME);
    ServiceUsageUtils.enableServices(
        project.getProjectId(), ImmutableList.of("iam.googleapis.com"));
    return project;
  }

  public static String randomSserviceAccountIdName() {
    // SA name ids must start with a letter and be no more than 30 characters long.
    return "sa" + IntegrationUtils.randomName().substring(0, 28);
  }

  /**
   * Create a string matching the region name on {@link ServiceAccounts#delete(String)}}, i.e..
   * projects/{PROJECT_ID}/serviceAccounts/{ACCOUNT}.
   */
  private static String fullServiceAccountName(String projectId, String accountId) {
    return String.format("projects/%s/serviceAccounts/%s", projectId, accountId);
  }
}
