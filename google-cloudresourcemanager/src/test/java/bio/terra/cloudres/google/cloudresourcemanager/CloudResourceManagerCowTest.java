package bio.terra.cloudres.google.cloudresourcemanager;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.*;

import bio.terra.cloudres.google.api.services.common.OperationCow;
import bio.terra.cloudres.google.api.services.common.OperationUtils;
import bio.terra.cloudres.testing.IntegrationCredentials;
import bio.terra.cloudres.testing.IntegrationUtils;
import com.google.api.client.googleapis.json.GoogleJsonResponseException;
import com.google.api.services.cloudresourcemanager.model.*;
import com.google.common.collect.ImmutableList;
import java.io.IOException;
import java.security.GeneralSecurityException;
import java.time.Duration;
import org.hamcrest.Matchers;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

@Tag("integration")
public class CloudResourceManagerCowTest {

  /** What parent resource (organizatino or folder) to create projects within. */
  // TODO(PF-67): Figure out how to pipe configuration to test.
  // Current value from vault 'config/terraform/terra/crl-test/default/container_folder_id'.
  private static final ResourceId PARENT_RESOURCE =
      new ResourceId().setType("folder").setId("866104354540");

  private static CloudResourceManagerCow defaultManager()
      throws GeneralSecurityException, IOException {
    return CloudResourceManagerCow.create(
        IntegrationUtils.DEFAULT_CLIENT_CONFIG,
        IntegrationCredentials.getAdminGoogleCredentialsOrDie());
  }

  private static Project defaultProject(String projectId) {
    return new Project().setProjectId(projectId).setParent(PARENT_RESOURCE);
  }

  @Test
  public void createGetDeleteProject() throws Exception {
    CloudResourceManagerCow managerCow = defaultManager();
    String projectId = randomProjectId();

    assertThrows(
        GoogleJsonResponseException.class, () -> managerCow.projects().get(projectId).execute());

    Operation operation = managerCow.projects().create(defaultProject(projectId)).execute();
    OperationCow<Operation> operationCow = managerCow.operations().operationCow(operation);
    operationCow =
        OperationUtils.pollUntilComplete(
            operationCow, Duration.ofSeconds(5), Duration.ofSeconds(30));
    assertTrue(operationCow.getOperation().getDone());
    assertNull(operationCow.getOperation().getError());

    Project project = managerCow.projects().get(projectId).execute();
    assertEquals(projectId, project.getProjectId());
    assertEquals("ACTIVE", project.getLifecycleState());

    managerCow.projects().delete(projectId).execute();
    // After "deletion," the project still exists for up to 30 days where it can be recovered.
    project = managerCow.projects().get(projectId).execute();
    assertEquals("DELETE_REQUESTED", project.getLifecycleState());
  }

  @Test
  public void getSetIamPolicy() throws Exception {
    CloudResourceManagerCow managerCow = defaultManager();
    String projectId = randomProjectId();
    createProject(managerCow, defaultProject(projectId));

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

  private static void createProject(CloudResourceManagerCow managerCow, Project project)
      throws IOException, InterruptedException {
    Operation operation = managerCow.projects().create(project).execute();
    OperationCow<Operation> operationCow = managerCow.operations().operationCow(operation);
    operationCow =
        OperationUtils.pollUntilComplete(
            operationCow, Duration.ofSeconds(5), Duration.ofSeconds(30));
    assertNull(operationCow.getOperation().getError());
  }

  private static String randomProjectId() {
    // Project ids must start with a letter and be no more than 30 characters long.
    return "p" + IntegrationUtils.randomName().substring(0, 29);
  }
}
