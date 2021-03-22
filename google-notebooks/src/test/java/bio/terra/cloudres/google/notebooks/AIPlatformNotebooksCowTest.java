package bio.terra.cloudres.google.notebooks;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import bio.terra.cloudres.google.api.services.common.OperationCow;
import bio.terra.cloudres.google.api.services.common.OperationUtils;
import bio.terra.cloudres.google.billing.testing.CloudBillingUtils;
import bio.terra.cloudres.google.cloudresourcemanager.testing.ProjectUtils;
import bio.terra.cloudres.google.serviceusage.testing.ServiceUsageUtils;
import bio.terra.cloudres.testing.IntegrationCredentials;
import bio.terra.cloudres.testing.IntegrationUtils;
import com.google.api.client.googleapis.json.GoogleJsonResponseException;
import com.google.api.services.cloudresourcemanager.model.Project;
import com.google.api.services.notebooks.v1.model.Instance;
import com.google.api.services.notebooks.v1.model.Operation;
import com.google.api.services.notebooks.v1.model.VmImage;
import com.google.common.collect.ImmutableList;
import java.io.IOException;
import java.security.GeneralSecurityException;
import java.time.Duration;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

@Tag("integration")
public class AIPlatformNotebooksCowTest {
  private static final AIPlatformNotebooksCow notebooks = defaultNotebooksCow();
  /** A dynamically created Google Project to manipulate AI Notebooks within for testing. */
  private static Project reusableProject;

  @BeforeAll
  public static void createReusableProject() throws Exception {
    reusableProject = ProjectUtils.executeCreateProject();
    CloudBillingUtils.setDefaultProjectBilling(reusableProject.getProjectId());
    ServiceUsageUtils.enableServices(
        reusableProject.getProjectId(), ImmutableList.of("notebooks.googleapis.com"));
  }

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
        .projectId(reusableProject.getProjectId())
        .location("us-east1-b")
        .instanceId("default-id");
  }

  /** Creates an {@link Instance} that's ready to be created. */
  private static Instance createInstance() {
    return new Instance()
        // A VM or Container image is required.
        .setVmImage(
            new VmImage().setProject("deeplearning-platform-release").setImageFamily("common-cpu"))
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

    GoogleJsonResponseException e =
        assertThrows(
            GoogleJsonResponseException.class,
            () -> notebooks.instances().get(instanceName).execute());
    assertEquals(404, e.getStatusCode());
  }

  @Test
  public void instanceCreateSerialize() throws Exception {
    assertEquals(
        "{\"projectId\":\"my-project\",\"locations\":\"us-east1-b\","
            + "\"instanceId\":\"my-id\","
            + "\"instance\":{\"machineType\":\"e2-standard-2\",\"vmImage\":{\"imageFamily\":\"common-cpu\",\"project\":\"deeplearning-platform-release\"}}}",
        notebooks
            .instances()
            .create(
                InstanceName.builder()
                    .projectId("my-project")
                    .location("us-east1-b")
                    .instanceId("my-id")
                    .build(),
                createInstance())
            .serialize()
            .toString());
  }

  @Test
  public void instanceGetSerialize() throws Exception {
    assertEquals(
        "{\"projectId\":\"my-project\",\"locations\":\"us-east1-b\",\"instanceId\":\"my-id\"}",
        notebooks
            .instances()
            .get(
                InstanceName.builder()
                    .projectId("my-project")
                    .location("us-east1-b")
                    .instanceId("my-id")
                    .build())
            .serialize()
            .toString());
  }

  @Test
  public void instanceDeleteSerialize() throws Exception {
    assertEquals(
        "{\"projectId\":\"my-project\",\"locations\":\"us-east1-b\",\"instanceId\":\"my-id\"}",
        notebooks
            .instances()
            .delete(
                InstanceName.builder()
                    .projectId("my-project")
                    .location("us-east1-b")
                    .instanceId("my-id")
                    .build())
            .serialize()
            .toString());
  }

  @Test
  public void operationGetSerialize() throws Exception {
    assertEquals(
        "{\"operation_name\":\"projects/my-project/locations/us-east1-b/operations/foo\"}",
        notebooks
            .operations()
            .get("projects/my-project/locations/us-east1-b/operations/foo")
            .serialize()
            .toString());
  }
}
