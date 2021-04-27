package bio.terra.cloudres.google.iam;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.hasItem;
import static org.junit.jupiter.api.Assertions.*;

import bio.terra.cloudres.google.billing.testing.CloudBillingUtils;
import bio.terra.cloudres.google.cloudresourcemanager.testing.ProjectUtils;
import bio.terra.cloudres.google.serviceusage.testing.ServiceUsageUtils;
import bio.terra.cloudres.testing.IntegrationCredentials;
import bio.terra.cloudres.testing.IntegrationUtils;
import com.google.api.client.googleapis.json.GoogleJsonResponseException;
import com.google.api.services.cloudresourcemanager.model.Project;
import com.google.api.services.iam.v1.model.Binding;
import com.google.api.services.iam.v1.model.CreateRoleRequest;
import com.google.api.services.iam.v1.model.CreateServiceAccountRequest;
import com.google.api.services.iam.v1.model.Policy;
import com.google.api.services.iam.v1.model.Role;
import com.google.api.services.iam.v1.model.ServiceAccount;
import com.google.api.services.iam.v1.model.SetIamPolicyRequest;
import com.google.common.collect.ImmutableList;
import java.io.IOException;
import java.security.GeneralSecurityException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import org.hamcrest.Matchers;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

@Tag("integration")
public class IamCowTest {
  private static IamCow defaultIam() throws GeneralSecurityException, IOException {
    return IamCow.create(
        IntegrationUtils.DEFAULT_CLIENT_CONFIG,
        IntegrationCredentials.getAdminGoogleCredentialsOrDie());
  }

  /** A dynamically created Google Project to manipulate IAM within for testing. */
  private static Project reusableProject;

  @BeforeAll
  public static void createReusableProject() throws Exception {
    reusableProject = createIamPreparedProject();
  }

  @Test
  public void createAndGetAndListAndDeleteServiceAccount() throws Exception {
    IamCow.Projects.ServiceAccounts serviceAccounts = defaultIam().projects().serviceAccounts();
    String projectName = "projects/" + reusableProject.getProjectId();
    String accountId = randomServiceAccountId();
    ServiceAccount serviceAccount =
        serviceAccounts
            .create(projectName, new CreateServiceAccountRequest().setAccountId(accountId))
            .execute();
    ServiceAccountName serviceAccountName =
        ServiceAccountName.builder()
            .projectId(serviceAccount.getProjectId())
            .email(serviceAccount.getEmail())
            .build();
    // Retry a lot of times. It apparently takes a lot of time for GCP to be ready to get a service
    // account that was just created.
    for (int retryNum = 0; retryNum < 20; retryNum++) {
      try {
        serviceAccounts.get(serviceAccountName).execute();
        break;
      } catch (GoogleJsonResponseException e) {
        assertEquals(404, e.getStatusCode());
        Thread.sleep(3000);
      }
    }
    assertEquals(serviceAccount, serviceAccounts.get(serviceAccountName).execute());
    List<ServiceAccount> listResult = serviceAccounts.list(projectName).execute().getAccounts();
    assertThat(listResult, Matchers.contains(serviceAccount));

    serviceAccounts.delete(serviceAccountName).execute();

    GoogleJsonResponseException getAfterDelete = null;
    for (int retryNum = 0; retryNum < 20; retryNum++) {
      try {
        serviceAccounts.get(serviceAccountName).execute();
        Thread.sleep(3000);
      } catch (GoogleJsonResponseException e) {
        getAfterDelete = e;
        break;
      }
    }
    assertNotNull(getAfterDelete);
    assertEquals(404, getAfterDelete.getStatusCode());
  }

  @Test
  public void getAndSetIamOnServiceAccount() throws Exception {
    IamCow.Projects.ServiceAccounts serviceAccounts = defaultIam().projects().serviceAccounts();
    String projectName = "projects/" + reusableProject.getProjectId();
    ServiceAccount serviceAccount =
        serviceAccounts
            .create(
                projectName,
                new CreateServiceAccountRequest().setAccountId(randomServiceAccountId()))
            .execute();
    ServiceAccountName serviceAccountName =
        ServiceAccountName.builder()
            .projectId(serviceAccount.getProjectId())
            .email(serviceAccount.getEmail())
            .build();

    Policy policy = serviceAccounts.getIamPolicy(serviceAccountName).execute();
    assertNotNull(policy);

    List<Binding> bindingList = new ArrayList<>();
    String member =
        String.format(
            "serviceAccount:%s",
            IntegrationCredentials.getUserGoogleCredentialsOrDie().getClientEmail());
    Binding newBinding =
        new Binding()
            .setRole("roles/iam.serviceAccountUser")
            .setMembers(Collections.singletonList(member));
    bindingList.add(newBinding);
    policy.setBindings(bindingList);

    Policy updatedPolicy =
        serviceAccounts
            .setIamPolicy(serviceAccountName, new SetIamPolicyRequest().setPolicy(policy))
            .execute();
    assertThat(updatedPolicy.getBindings(), hasItem(newBinding));
    assertEquals(updatedPolicy, serviceAccounts.getIamPolicy(serviceAccountName).execute());
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
  public void deleteServiceAccountSerialize() throws Exception {
    IamCow.Projects.ServiceAccounts.Delete delete =
        defaultIam()
            .projects()
            .serviceAccounts()
            .delete(ServiceAccountName.builder().projectId("projectId").email("saEmail").build());
    assertEquals(
        "{\"name\":\"projects/projectId/serviceAccounts/saEmail\"}", delete.serialize().toString());
  }

  @Test
  public void getServiceAccountSerialize() throws Exception {
    IamCow.Projects.ServiceAccounts.Get get =
        defaultIam()
            .projects()
            .serviceAccounts()
            .get(ServiceAccountName.builder().projectId("projectId").email("saEmail").build());
    assertEquals(
        "{\"name\":\"projects/projectId/serviceAccounts/saEmail\"}", get.serialize().toString());
  }

  @Test
  public void getIamPolicyServiceAccountSerialize() throws Exception {
    IamCow.Projects.ServiceAccounts.GetIamPolicy getIamPolicy =
        defaultIam()
            .projects()
            .serviceAccounts()
            .getIamPolicy(
                ServiceAccountName.builder().projectId("projectId").email("saEmail").build());

    assertEquals(
        "{\"resource\":\"projects/projectId/serviceAccounts/saEmail\"}",
        getIamPolicy.serialize().toString());
  }

  @Test
  public void listServiceAccountSerialize() throws Exception {
    IamCow.Projects.ServiceAccounts.List list =
        defaultIam().projects().serviceAccounts().list("projects/project-id");
    assertEquals("{\"project_name\":\"projects/project-id\"}", list.serialize().toString());
  }

  @Test
  public void setIamPolicyServiceAccountSerialize() throws Exception {
    IamCow.Projects.ServiceAccounts.SetIamPolicy setIamPolicy =
        defaultIam()
            .projects()
            .serviceAccounts()
            .setIamPolicy(
                ServiceAccountName.builder().projectId("projectId").email("saEmail").build(),
                new SetIamPolicyRequest().setPolicy(new Policy().setEtag("myEtag")));

    assertEquals(
        "{\"resource\":\"projects/projectId/serviceAccounts/saEmail\","
            + "\"content\":{\"policy\":{\"etag\":\"myEtag\"}}}",
        setIamPolicy.serialize().toString());
  }

  @Test
  public void createGetListPatchDeleteRoles() throws Exception {
    IamCow iam = defaultIam();
    String resourceName = "projects/" + reusableProject.getProjectId();
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
    for (int retryNum = 0; retryNum < 6; retryNum++) {
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
    IamCow.Projects.Roles.List list =
        defaultIam().projects().roles().list("projects/project-id").setView("FULL");

    assertEquals(
        "{\"parent\":\"projects/project-id\",\"view\":\"FULL\"}", list.serialize().toString());
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
  private static Project createIamPreparedProject() throws Exception {
    Project project = ProjectUtils.executeCreateProject();
    CloudBillingUtils.setDefaultProjectBilling(project.getProjectId());
    ServiceUsageUtils.enableServices(
        project.getProjectId(), ImmutableList.of("iam.googleapis.com"));
    return project;
  }

  private static String randomServiceAccountId() {
    // SA name ids must start with a letter and be no more than 30 characters long.
    return "sa" + IntegrationUtils.randomName().substring(0, 28);
  }

  /** Create a Role object with the permission iam.roles.create and no other fields specified. */
  private static Role roleWithSinglePermission() {
    return new Role().setIncludedPermissions(Collections.singletonList("iam.roles.create"));
  }
}
