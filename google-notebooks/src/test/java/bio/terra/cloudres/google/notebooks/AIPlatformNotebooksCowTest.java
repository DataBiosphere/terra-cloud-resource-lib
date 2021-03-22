package bio.terra.cloudres.google.notebooks;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import bio.terra.cloudres.google.api.services.common.OperationCow;
import bio.terra.cloudres.google.api.services.common.OperationUtils;
import bio.terra.cloudres.google.cloudresourcemanager.testing.ProjectUtils;
import bio.terra.cloudres.testing.IntegrationCredentials;
import bio.terra.cloudres.testing.IntegrationUtils;
import com.google.api.services.cloudresourcemanager.model.Project;
import com.google.api.services.notebooks.v1.model.ContainerImage;
import com.google.api.services.notebooks.v1.model.Instance;
import com.google.api.services.notebooks.v1.model.Operation;
import java.io.IOException;
import java.security.GeneralSecurityException;
import java.time.Duration;

import com.google.api.services.notebooks.v1.model.VmImage;
import com.google.common.collect.ImmutableList;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

@Tag("integration")
public class AIPlatformNotebooksCowTest {
  private static final AIPlatformNotebooksCow notebooks = defaultNotebooksCow();
  private static final String projectId =
      IntegrationCredentials.getAdminGoogleCredentialsOrDie().getProjectId();

  private static AIPlatformNotebooksCow defaultNotebooksCow() {
    try {
      return AIPlatformNotebooksCow.create(
          IntegrationUtils.DEFAULT_CLIENT_CONFIG,
          IntegrationCredentials.getAdminGoogleCredentialsOrDie());
    } catch (GeneralSecurityException | IOException e) {
      throw new IllegalStateException("Unable to create notebooks cow.", e);
    }
  }

  private static InstanceName.Builder defaultInstanceName() {
    return InstanceName.builder()
        .projectId(projectId)
        .location("us-east1-b")
        .instanceId("default-id");
  }

  /** Creates an {@link Instance} that's ready to be created. */
  private static Instance createInstance() {
    return new Instance()
    // A VM or Container image is required.
    .setVmImage(new VmImage().setProject("deeplearning-platform-release")
            .setImageFamily("common-cpu"))
            // The machine type to used is required.
    .setMachineType("e2-standard-2");
  }

  @Test
  public void createGetDeleteNotebookInstance() throws Exception {
    InstanceName instanceName = defaultInstanceName().build();
    OperationCow<Operation> createOperation =
        notebooks
            .operations()
            .operationCow(notebooks.instances().create(instanceName, createInstance()).execute());

    createOperation =
        OperationUtils.pollUntilComplete(
            createOperation, Duration.ofSeconds(30), Duration.ofMinutes(12));
    assertTrue(createOperation.getOperation().getDone());
    assertNull(createOperation.getOperation().getError());

    Instance retrievedInstance = notebooks.instances().get(instanceName).execute();
    assertEquals(instanceName.formatName(), retrievedInstance.getName());

    OperationCow<Operation> deleteOperation =
        notebooks.operations().operationCow(notebooks.instances().delete(instanceName).execute());
    deleteOperation =
        OperationUtils.pollUntilComplete(
            deleteOperation, Duration.ofSeconds(30), Duration.ofMinutes(5));
    assertTrue(deleteOperation.getOperation().getDone());
    assertNull(deleteOperation.getOperation().getError());
    assertNull(notebooks.instances().get(instanceName).execute());
  }


  /** Create Project then set billing account, enable compute compute service */
  private static Project createPreparedProject() throws Exception {
    Project project = ProjectUtils.executeCreateProject();
 //   CloudBillingUtils.setProjectBillingInfo(project.getProjectId(), BILLING_ACCOUNT_NAME);
   // ServiceUsageUtils.enableServices(project.getProjectId(), ImmutableList.of(COMPUTE_SERVICE_ID));
    return project;
  }

  // TODO write serialize tests.
}
