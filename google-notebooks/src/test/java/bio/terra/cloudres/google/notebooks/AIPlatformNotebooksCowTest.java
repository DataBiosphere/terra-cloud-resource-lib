package bio.terra.cloudres.google.notebooks;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import bio.terra.cloudres.google.api.services.common.OperationCow;
import bio.terra.cloudres.google.api.services.common.OperationUtils;
import bio.terra.cloudres.testing.IntegrationCredentials;
import bio.terra.cloudres.testing.IntegrationUtils;
import com.google.api.services.notebooks.v1.model.Instance;
import com.google.api.services.notebooks.v1.model.Operation;
import java.io.IOException;
import java.security.GeneralSecurityException;
import java.time.Duration;
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
        .location("useast1-b")
        .instanceId("default-id");
  }

  @Test
  public void createGetDeleteNotebookInstance() throws Exception {
    InstanceName instanceName = defaultInstanceName().build();
    Instance instance = new Instance();
    OperationCow<Operation> createOperation =
        notebooks
            .operations()
            .operationCow(notebooks.instances().create(instanceName, instance).execute());

    createOperation =
        OperationUtils.pollUntilComplete(
            createOperation, Duration.ofSeconds(30), Duration.ofMinutes(12));
    assertTrue(createOperation.getOperation().getDone());

    Instance retrievedInstance = notebooks.instances().get(instanceName).execute();
    assertEquals(instanceName.formatName(), retrievedInstance.getName());

    OperationCow<Operation> deleteOperation =
        notebooks.operations().operationCow(notebooks.instances().delete(instanceName).execute());
    deleteOperation =
        OperationUtils.pollUntilComplete(
            deleteOperation, Duration.ofSeconds(30), Duration.ofMinutes(5));
    assertTrue(deleteOperation.getOperation().getDone());
    assertNull(notebooks.instances().get(instanceName).execute());
  }

  // TODO write serialize tests.
}
