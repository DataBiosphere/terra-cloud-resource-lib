package bio.terra.cloudres.google.cloudresourcemanager.testing;

import bio.terra.cloudres.google.api.services.common.OperationCow;
import bio.terra.cloudres.google.api.services.common.testing.OperationTestUtils;
import bio.terra.cloudres.google.cloudresourcemanager.CloudResourceManagerCow;
import bio.terra.cloudres.testing.IntegrationCredentials;
import bio.terra.cloudres.testing.IntegrationUtils;
import com.google.api.services.cloudresourcemanager.v3.model.Operation;
import com.google.api.services.cloudresourcemanager.v3.model.Project;
import java.time.Duration;

/** Testing utilities for projects. */
public class ProjectUtils {
  /** What parent resource (organization or folder) to create projects within. */
  // TODO(PF-67): Figure out how to pipe configuration to test.
  // Current value from vault 'config/terraform/terra/crl-test/default/container_folder_id'.
  public static final String PARENT_RESOURCE = "folders/866104354540";

  private static CloudResourceManagerCow managerCow;

  public static CloudResourceManagerCow getManagerCow() throws Exception {
    if (managerCow == null) {
      managerCow =
          CloudResourceManagerCow.create(
              IntegrationUtils.DEFAULT_CLIENT_CONFIG,
              IntegrationCredentials.getAdminGoogleCredentialsOrDie());
    }
    return managerCow;
  }

  /** Creates a new Google Project in GCP for testing. */
  public static Project executeCreateProject() throws Exception {
    Project project = new Project().setProjectId(randomProjectId()).setParent(PARENT_RESOURCE);
    Operation operation = getManagerCow().projects().create(project).execute();
    OperationCow<Operation> operationCow = managerCow.operations().operationCow(operation);
    OperationTestUtils.pollAndAssertSuccess(
        operationCow, Duration.ofSeconds(5), Duration.ofSeconds(30));
    return managerCow.projects().get(project.getProjectId()).execute();
  }

  public static String randomProjectId() {
    // Project ids must start with a letter and be no more than 30 characters long.
    return "p" + IntegrationUtils.randomName().substring(0, 29);
  }
}
