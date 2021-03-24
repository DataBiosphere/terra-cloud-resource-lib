package bio.terra.cloudres.google.notebooks;

import static org.hamcrest.MatcherAssert.assertThat;
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
import com.google.api.services.notebooks.v1.model.Binding;
import com.google.api.services.notebooks.v1.model.Instance;
import com.google.api.services.notebooks.v1.model.ListInstancesResponse;
import com.google.api.services.notebooks.v1.model.Operation;
import com.google.api.services.notebooks.v1.model.Policy;
import com.google.api.services.notebooks.v1.model.SetIamPolicyRequest;
import com.google.api.services.notebooks.v1.model.VmImage;
import com.google.common.collect.ImmutableList;
import java.io.IOException;
import java.security.GeneralSecurityException;
import java.time.Duration;
import java.util.stream.Collectors;
import org.hamcrest.Matchers;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

@Tag("integration")
public class AIPlatformNotebooksCowTest {
  private static final AIPlatformNotebooksCow notebooks = defaultNotebooksCow();
  /** A dynamically created Google Project to manipulate AI Notebooks within for testing. */
  private static Project reusableProject;

  // @BeforeAll
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

  /**
   * Creates a notebook instance for the {@link InstanceName}. Blocks until the instance is created
   * successfully or fails
   */
  private void createInstance(InstanceName instanceName) throws IOException, InterruptedException {
    OperationCow<Operation> createOperation =
        notebooks
            .operations()
            .operationCow(notebooks.instances().create(instanceName, defaultInstance()).execute());
    createOperation =
        OperationUtils.pollUntilComplete(
            createOperation, Duration.ofSeconds(30), Duration.ofMinutes(12));
    assertTrue(createOperation.getOperation().getDone());
    assertNull(createOperation.getOperation().getError());
  }

  /** Creates an {@link Instance} that's ready to be created. */
  private static Instance defaultInstance() {
    return new Instance()
        // A VM or Container image is required.
        .setVmImage(
            new VmImage().setProject("deeplearning-platform-release").setImageFamily("common-cpu"))
        // The machine type to used is required.
        .setMachineType("e2-standard-2");
  }

  @Test
  public void createGetListDeleteNotebookInstance() throws Exception {
    InstanceName instanceName = defaultInstanceName().build();
    createInstance(instanceName);

    Instance retrievedInstance = notebooks.instances().get(instanceName).execute();
    assertEquals(instanceName.formatName(), retrievedInstance.getName());

    ListInstancesResponse listResponse =
        notebooks.instances().list(instanceName.formatParent()).execute();
    // There may be other notebook instances from other tests.
    assertThat(listResponse.getInstances().size(), Matchers.greaterThan(0));
    assertThat(
        listResponse.getInstances().stream().map(Instance::getName).collect(Collectors.toList()),
        Matchers.hasItem(instanceName.formatName()));

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
  public void setGetIamPolicyNotebookInstance() throws Exception {
    InstanceName instanceName = defaultInstanceName().instanceId("set-get-iam").build();
    createInstance(instanceName);

    String userEmail = IntegrationCredentials.getUserGoogleCredentialsOrDie().getClientEmail();
    Binding binding =
        new Binding()
            .setRole("roles/notebooks.viewer")
            .setMembers(ImmutableList.of("serviceAccount:" + userEmail));
    Policy policy = notebooks.instances().getIamPolicy(instanceName).execute();
    policy.setBindings(ImmutableList.of(binding));

    Policy updatedPolicy =
        notebooks
            .instances()
            .setIamPolicy(instanceName, new SetIamPolicyRequest().setPolicy(policy))
            .execute();

    assertThat(updatedPolicy.getBindings(), Matchers.hasItem(binding));
    Policy secondRetrieval = notebooks.instances().getIamPolicy(instanceName).execute();
    assertThat(secondRetrieval.getBindings(), Matchers.hasItem(binding));

    notebooks.instances().delete(instanceName).execute();
  }

  @Test
  public void stopStartNotebookInstance() throws Exception {
    InstanceName instanceName = defaultInstanceName().instanceId("stop-start").build();
    createInstance(instanceName);

    OperationCow<Operation> stopOperation =
        notebooks.operations().operationCow(notebooks.instances().stop(instanceName).execute());
    stopOperation =
        OperationUtils.pollUntilComplete(
            stopOperation, Duration.ofSeconds(10), Duration.ofMinutes(4));
    assertTrue(stopOperation.getOperation().getDone());
    assertNull(stopOperation.getOperation().getError());
    assertEquals("STOPPED", notebooks.instances().get(instanceName).execute().getState());

    OperationCow<Operation> startOperation =
        notebooks.operations().operationCow(notebooks.instances().start(instanceName).execute());
    startOperation =
        OperationUtils.pollUntilComplete(
            startOperation, Duration.ofSeconds(10), Duration.ofMinutes(4));
    assertTrue(startOperation.getOperation().getDone());
    assertNull(startOperation.getOperation().getError());
    assertEquals("PROVISIONING", notebooks.instances().get(instanceName).execute().getState());

    notebooks.instances().delete(instanceName).execute();
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
                defaultInstance())
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
  public void instanceListSerialize() throws Exception {
    assertEquals(
        "{\"parent\":\"projects/my-project/locations/us-east1-b\",\"page_size\":10,"
            + "\"page_token\":\"my-page-token\"}",
        notebooks
            .instances()
            .list("projects/my-project/locations/us-east1-b")
            .setPageSize(10)
            .setPageToken("my-page-token")
            .serialize()
            .toString());
  }

  @Test
  public void instanceGetIamPolicySerialize() throws Exception {
    assertEquals(
        "{\"projectId\":\"my-project\",\"locations\":\"us-east1-b\","
            + "\"instanceId\":\"my-id\",\"options_requested_policy_version\":3}",
        notebooks
            .instances()
            .getIamPolicy(
                InstanceName.builder()
                    .projectId("my-project")
                    .location("us-east1-b")
                    .instanceId("my-id")
                    .build())
            .setOptionsRequestedPolicyVersion(3)
            .serialize()
            .toString());
  }

  @Test
  public void instanceSetIamPolicySerialize() throws Exception {
    Binding binding =
        new Binding()
            .setRole("roles/notebooks.viewer")
            .setMembers(ImmutableList.of("userEmail:foo@gmail.com"));
    SetIamPolicyRequest request =
        new SetIamPolicyRequest().setPolicy(new Policy().setBindings(ImmutableList.of(binding)));
    assertEquals(
        "{\"projectId\":\"my-project\",\"locations\":\"us-east1-b\","
            + "\"instanceId\":\"my-id\",\"content\":{\"policy\":{"
            + "\"bindings\":[{\"members\":[\"userEmail:foo@gmail.com\"],"
            + "\"role\":\"roles/notebooks.viewer\"}]}}}",
        notebooks
            .instances()
            .setIamPolicy(
                InstanceName.builder()
                    .projectId("my-project")
                    .location("us-east1-b")
                    .instanceId("my-id")
                    .build(),
                request)
            .serialize()
            .toString());
  }

  @Test
  public void instanceStartSerialize() throws Exception {
    assertEquals(
        "{\"projectId\":\"my-project\",\"locations\":\"us-east1-b\",\"instanceId\":\"my-id\"}",
        notebooks
            .instances()
            .start(
                InstanceName.builder()
                    .projectId("my-project")
                    .location("us-east1-b")
                    .instanceId("my-id")
                    .build())
            .serialize()
            .toString());
  }

  @Test
  public void instanceStopSerialize() throws Exception {
    assertEquals(
        "{\"projectId\":\"my-project\",\"locations\":\"us-east1-b\",\"instanceId\":\"my-id\"}",
        notebooks
            .instances()
            .stop(
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
