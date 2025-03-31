package bio.terra.cloudres.google.cloudresourcemanager;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.*;

import bio.terra.cloudres.google.api.services.common.testing.OperationTestUtils;
import bio.terra.cloudres.google.cloudresourcemanager.testing.ProjectUtils;
import bio.terra.cloudres.testing.IntegrationCredentials;
import bio.terra.cloudres.testing.IntegrationUtils;
import com.google.api.client.googleapis.json.GoogleJsonResponseException;
import com.google.api.services.cloudresourcemanager.v3.model.Binding;
import com.google.api.services.cloudresourcemanager.v3.model.GetIamPolicyRequest;
import com.google.api.services.cloudresourcemanager.v3.model.Operation;
import com.google.api.services.cloudresourcemanager.v3.model.Policy;
import com.google.api.services.cloudresourcemanager.v3.model.Project;
import com.google.api.services.cloudresourcemanager.v3.model.SetIamPolicyRequest;
import com.google.api.services.cloudresourcemanager.v3.model.TestIamPermissionsRequest;
import com.google.api.services.cloudresourcemanager.v3.model.TestIamPermissionsResponse;
import com.google.common.collect.ImmutableList;
import java.io.IOException;
import java.security.GeneralSecurityException;
import java.time.Duration;
import org.hamcrest.Matchers;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

@Tag("integration")
public class CloudResourceManagerCowTest {

  private static CloudResourceManagerCow defaultManager()
      throws GeneralSecurityException, IOException {
    return CloudResourceManagerCow.create(
        IntegrationUtils.DEFAULT_CLIENT_CONFIG,
        IntegrationCredentials.getAdminGoogleCredentialsOrDie());
  }

  private static Project defaultProject(String projectId) {
    return new Project().setProjectId(projectId).setParent(ProjectUtils.PARENT_RESOURCE);
  }

  @Test
  public void testIamPermissionsFolder() throws Exception {
    String getProjectPermission = "resourcemanager.projects.get";
    TestIamPermissionsResponse response =
        defaultManager()
            .folders()
            .testIamPermissions(
                ProjectUtils.PARENT_RESOURCE,
                new TestIamPermissionsRequest()
                    .setPermissions(ImmutableList.of(getProjectPermission)))
            .execute();
    assertThat(response.getPermissions(), Matchers.contains(getProjectPermission));
  }

  @Test
  public void createGetDeleteProject() throws Exception {
    CloudResourceManagerCow managerCow = defaultManager();
    String projectId = ProjectUtils.randomProjectId();

    assertThrows(
        GoogleJsonResponseException.class, () -> managerCow.projects().get(projectId).execute());

    Operation operation = managerCow.projects().create(defaultProject(projectId)).execute();
    OperationTestUtils.pollAndAssertSuccess(
        managerCow.operations().operationCow(operation),
        Duration.ofSeconds(5),
        Duration.ofSeconds(30));

    Project project = managerCow.projects().get(projectId).execute();
    assertEquals(projectId, project.getProjectId());
    assertEquals("ACTIVE", project.getState());

    Operation deleteOperation = managerCow.projects().delete(projectId).execute();
    OperationTestUtils.pollAndAssertSuccess(
        managerCow.operations().operationCow(deleteOperation),
        Duration.ofSeconds(5),
        Duration.ofSeconds(30));

    // After "deletion," the project still exists for up to 30 days where it can be recovered.
    project = managerCow.projects().get(projectId).execute();
    assertEquals("DELETE_REQUESTED", project.getState());
  }

  @Test
  public void getSetIamPolicy() throws Exception {
    CloudResourceManagerCow managerCow = defaultManager();
    String projectId = ProjectUtils.executeCreateProject().getProjectId();

    String userEmail = IntegrationCredentials.getUserGoogleCredentialsOrDie().getClientEmail();

    Policy policy =
        managerCow.projects().getIamPolicy(projectId, new GetIamPolicyRequest()).execute();
    Binding binding =
        new Binding()
            .setRole("roles/viewer")
            .setMembers(ImmutableList.of("serviceAccount:" + userEmail));
    policy.getBindings().add(binding);
    Policy updatedPolicy =
        managerCow
            .projects()
            .setIamPolicy(projectId, new SetIamPolicyRequest().setPolicy(policy))
            .execute();

    assertThat(updatedPolicy.getBindings(), Matchers.hasItem(binding));
    Policy secondRetrieval =
        managerCow.projects().getIamPolicy(projectId, new GetIamPolicyRequest()).execute();
    assertThat(secondRetrieval.getBindings(), Matchers.hasItem(binding));

    managerCow.projects().delete(projectId).execute();
  }

  @Test
  public void testIamPermissionsFolderSerializer() throws Exception {
    assertEquals(
        "{\"resource\":\"folders/1234\",\"content\":{\"permissions\":[\"myPermission\"]}}",
        defaultManager()
            .folders()
            .testIamPermissions(
                "folders/1234",
                new TestIamPermissionsRequest().setPermissions(ImmutableList.of("myPermission")))
            .serialize()
            .toString());
  }
}
