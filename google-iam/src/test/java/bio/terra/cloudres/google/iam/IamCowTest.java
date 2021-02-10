package bio.terra.cloudres.google.iam;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.*;

import bio.terra.cloudres.google.billing.testing.CloudBillingUtils;
import bio.terra.cloudres.google.cloudresourcemanager.testing.ProjectUtils;
import bio.terra.cloudres.google.serviceusage.testing.ServiceUsageUtils;
import bio.terra.cloudres.testing.IntegrationCredentials;
import bio.terra.cloudres.testing.IntegrationUtils;
import com.google.api.services.cloudresourcemanager.model.Project;
import com.google.api.services.iam.v1.Iam.Projects.ServiceAccounts;
import com.google.api.services.iam.v1.model.CreateRoleRequest;
import com.google.api.services.iam.v1.model.CreateServiceAccountRequest;
import com.google.api.services.iam.v1.model.Role;
import com.google.api.services.iam.v1.model.ServiceAccount;
import com.google.common.collect.ImmutableList;
import java.io.IOException;
import java.security.GeneralSecurityException;
import java.util.Collections;
import java.util.List;
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
    String resourceName = "projects/" + projectId;
    String accountId = randomServiceAccountIdName();
    ServiceAccount serviceAccount =
        iam.projects()
            .serviceAccounts()
            .create(resourceName, new CreateServiceAccountRequest().setAccountId(accountId))
            .execute();
    String fullSaName = fullServiceAccountName(projectId, serviceAccount.getEmail());
    // Retry 6 times to make sure get after create works.
    List<ServiceAccount> listResult = null;
    for(int retryNum = 0; retryNum < 6; retryNum++) {
      listResult = iam.projects().serviceAccounts().list(resourceName).execute().getAccounts();
      if (listResult != null) {
        break;
      }
      Thread.sleep(3000);
    }
    assertThat(listResult, Matchers.contains(serviceAccount));

    iam.projects().serviceAccounts().delete(fullSaName).execute();
    // Sleep for 3s to make get after delete works.
    Thread.sleep(3000);
    assertNull(iam.projects().serviceAccounts().list(resourceName).execute().getAccounts());
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

  @Test
  public void createGetListPatchDeleteRoles() throws Exception {
    IamCow iam = defaultIam();
    Project project = createPreparedProject();
    String projectId = project.getProjectId();
    String resourceName = "projects/" + projectId;
    String roleId = "myCustomRoleId";
    Role createdRole =
        iam.projects()
            .roles()
            .create(
                resourceName,
                new CreateRoleRequest().setRole(roleWithSinglePermission()).setRoleId(roleId))
            .execute();
    // Retry 6 times to make sure get after create works.
    List<Role> listResult = null;
    for(int retryNum = 0; retryNum < 6; retryNum++) {
      listResult = iam.projects().roles().list(resourceName).execute().getRoles();
      if (listResult != null) {
        break;
      }
      Thread.sleep(3000);
    }
    Role retrievedResult = iam.projects().roles().get(createdRole.getName()).execute();
    assertThat(retrievedResult, Matchers.equalTo(createdRole));
    // By default, enumerate does not include the list of included permissions.
    Role basicEnumerateResult =
        new Role().setEtag(createdRole.getEtag()).setName(createdRole.getName());
    assertThat(listResult, Matchers.contains(basicEnumerateResult));
    // Alternatively, we can fetch the full view.
    List<Role> fullListResult =
        iam.projects().roles().list(resourceName).setView("FULL").execute().getRoles();
    assertThat(fullListResult, Matchers.contains(createdRole));

    Role patchRole =
        new Role().setIncludedPermissions(Collections.singletonList("iam.roles.delete"));
    Role modifiedRole = iam.projects().roles().patch(createdRole.getName(), patchRole).execute();
    // Sleep for 3s to make get after patch works.
    Thread.sleep(3000);
    retrievedResult = iam.projects().roles().get(modifiedRole.getName()).execute();
    assertThat(retrievedResult, Matchers.equalTo(modifiedRole));

    iam.projects().roles().delete(modifiedRole.getName()).execute();
    // Sleep for 3s to make get after delete works.
    Thread.sleep(3000);
    // Note that roles take 7 days to truly delete, but will be marked as "deleted" sooner.
    assertTrue(iam.projects().roles().get(modifiedRole.getName()).execute().getDeleted());
  }

  @Test
  public void createRoleSerialize() throws Exception {
    IamCow.Projects.Roles.Create create =
        defaultIam()
            .projects()
            .roles()
            .create(
                "projects/my-project",
                new CreateRoleRequest().setRoleId("roleId").setRole(roleWithSinglePermission()));

    assertEquals(
        "{\"parent\":\"projects/my-project\",\"content\":{\"role\":{\"includedPermissions\":[\"iam.roles.create\"]},\"roleId\":\"roleId\"}}",
        create.serialize().toString());
  }

  @Test
  public void listRoleSerialize() throws Exception {
    IamCow.Projects.Roles.List list = defaultIam().projects().roles().list("projects/project-id");

    assertEquals("{\"parent\":\"projects/project-id\"}", list.serialize().toString());
  }

  @Test
  public void getRoleSerialize() throws Exception {
    IamCow.Projects.Roles.Get get =
        defaultIam().projects().roles().get("projects/project-id/roles/role-id");

    assertEquals("{\"name\":\"projects/project-id/roles/role-id\"}", get.serialize().toString());
  }

  @Test
  public void deleteRoleSerialize() throws Exception {
    IamCow.Projects.Roles.Delete delete =
        defaultIam().projects().roles().delete(("projects/project-id/roles/role-id"));

    assertEquals("{\"name\":\"projects/project-id/roles/role-id\"}", delete.serialize().toString());
  }

  @Test
  public void patchRoleSerialize() throws Exception {
    IamCow.Projects.Roles.Patch patch =
        defaultIam()
            .projects()
            .roles()
            .patch("projects/project-id/roles/role-id", roleWithSinglePermission());

    assertEquals(
        "{\"name\":\"projects/project-id/roles/role-id\",\"content\":{\"includedPermissions\":[\"iam.roles.create\"]}}",
        patch.serialize().toString());
  }

  /** Create Project then set billing account, enable IAM api */
  private static Project createPreparedProject() throws Exception {
    Project project = ProjectUtils.executeCreateProject();
    CloudBillingUtils.setProjectBillingInfo(project.getProjectId(), BILLING_ACCOUNT_NAME);
    ServiceUsageUtils.enableServices(
        project.getProjectId(), ImmutableList.of("iam.googleapis.com"));
    return project;
  }

  public static String randomServiceAccountIdName() {
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

  /**
   * Create a Role object with the permission iam.roles.create and no other fields specified.
   */
  private static Role roleWithSinglePermission() {
    return new Role().setIncludedPermissions(Collections.singletonList("iam.roles.create"));
  }
}
