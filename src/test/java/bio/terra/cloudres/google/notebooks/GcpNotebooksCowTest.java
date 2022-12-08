package bio.terra.cloudres.google.notebooks;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import bio.terra.cloudres.google.api.services.common.OperationCow;
import bio.terra.cloudres.google.api.services.common.testing.OperationTestUtils;
import bio.terra.cloudres.google.billing.testing.CloudBillingUtils;
import bio.terra.cloudres.google.cloudresourcemanager.testing.ProjectUtils;
import bio.terra.cloudres.google.compute.testing.NetworkUtils;
import bio.terra.cloudres.google.serviceusage.testing.ServiceUsageUtils;
import bio.terra.cloudres.testing.IntegrationCredentials;
import bio.terra.cloudres.testing.IntegrationUtils;
import com.google.api.client.googleapis.json.GoogleJsonResponseException;
import com.google.api.services.cloudresourcemanager.v3.model.Project;
import com.google.api.services.compute.model.Network;
import com.google.api.services.notebooks.v1.model.Binding;
import com.google.api.services.notebooks.v1.model.Instance;
import com.google.api.services.notebooks.v1.model.ListInstancesResponse;
import com.google.api.services.notebooks.v1.model.Operation;
import com.google.api.services.notebooks.v1.model.Policy;
import com.google.api.services.notebooks.v1.model.SetIamPolicyRequest;
import com.google.api.services.notebooks.v1.model.TestIamPermissionsRequest;
import com.google.api.services.notebooks.v1.model.TestIamPermissionsResponse;
import com.google.api.services.notebooks.v1.model.VmImage;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import java.io.IOException;
import java.security.GeneralSecurityException;
import java.time.Duration;
import java.util.stream.Collectors;
import org.hamcrest.Matchers;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

@Tag("integration")
public class GcpNotebooksCowTest {
  private static final GcpNotebooksCow notebooks = defaultNotebooksCow();
  /** A dynamically created Google Project to manipulate AI Notebooks within for testing. */
  private static Project reusableProject;

  private static Network reusableNetwork;

  @BeforeAll
  public static void createReusableProject() throws Exception {
    reusableProject = ProjectUtils.executeCreateProject();
    CloudBillingUtils.setDefaultProjectBilling(reusableProject.getProjectId());
    ServiceUsageUtils.enableServices(
        reusableProject.getProjectId(), ImmutableList.of("notebooks.googleapis.com"));
    reusableNetwork = NetworkUtils.exceuteCreateNetwork(reusableProject.getProjectId(), true);
  }

  private static GcpNotebooksCow defaultNotebooksCow() {
    try {
      return GcpNotebooksCow.create(
          IntegrationUtils.DEFAULT_CLIENT_CONFIG,
          IntegrationCredentials.getAdminGoogleCredentialsOrDie());
    } catch (GeneralSecurityException | IOException e) {
      throw new IllegalStateException("Unable to create notebooks cow.", e);
    }
  }

  private static GcpNotebookInstanceName.Builder defaultInstanceName() {
    return GcpNotebookInstanceName.builder()
        .projectId(reusableProject.getProjectId())
        .location("us-east1-b")
        .instanceId("default-id");
  }

  /**
   * Creates a notebook instance for the {@link GcpNotebookInstanceName}. Blocks until the instance
   * is created successfully or fails
   */
  private void createInstance(GcpNotebookInstanceName gcpNotebookInstanceName)
      throws IOException, InterruptedException {
    OperationCow<Operation> createOperation =
        notebooks
            .operations()
            .operationCow(
                notebooks.instances().create(gcpNotebookInstanceName, defaultInstance()).execute());
    OperationTestUtils.pollAndAssertSuccess(
        createOperation, Duration.ofSeconds(30), Duration.ofMinutes(12));
  }

  /** Creates an {@link Instance} that's ready to be created. */
  private static Instance defaultInstance() {
    return new Instance()
        // A VM or Container image is required.
        .setVmImage(
            new VmImage().setProject("deeplearning-platform-release").setImageFamily("common-cpu"))
        // The machine type to used is required.
        .setMachineType("e2-standard-2")
        // The default network does not exist in Broad projects, so network is required.
        .setNetwork(reusableNetwork.getSelfLink());
  }

  @Test
  public void createGetListDeleteNotebookInstance() throws Exception {
    GcpNotebookInstanceName gcpNotebookInstanceName = defaultInstanceName().build();
    createInstance(gcpNotebookInstanceName);

    Instance retrievedInstance = notebooks.instances().get(gcpNotebookInstanceName).execute();
    assertEquals(gcpNotebookInstanceName.formatName(), retrievedInstance.getName());

    ListInstancesResponse listResponse =
        notebooks.instances().list(gcpNotebookInstanceName.formatParent()).execute();
    // There may be other notebook instances from other tests.
    assertThat(listResponse.getInstances().size(), Matchers.greaterThan(0));
    assertThat(
        listResponse.getInstances().stream().map(Instance::getName).collect(Collectors.toList()),
        Matchers.hasItem(gcpNotebookInstanceName.formatName()));

    OperationCow<Operation> deleteOperation =
        notebooks
            .operations()
            .operationCow(notebooks.instances().delete(gcpNotebookInstanceName).execute());
    OperationTestUtils.pollAndAssertSuccess(
        deleteOperation, Duration.ofSeconds(30), Duration.ofMinutes(5));

    GoogleJsonResponseException e =
        assertThrows(
            GoogleJsonResponseException.class,
            () -> notebooks.instances().get(gcpNotebookInstanceName).execute());
    assertEquals(404, e.getStatusCode());
  }

  @Test
  public void updateNotebookInstanceMetadata() throws Exception {
    GcpNotebookInstanceName gcpNotebookInstanceName =
        defaultInstanceName().instanceId("instance-with-foobar-metadata").build();
    createInstance(gcpNotebookInstanceName);

    Instance retrievedInstance = notebooks.instances().get(gcpNotebookInstanceName).execute();
    assertEquals(gcpNotebookInstanceName.formatName(), retrievedInstance.getName());

    notebooks
        .instances()
        .updateMetadataItems(
            gcpNotebookInstanceName.formatName(), ImmutableMap.of("foo", "bar", "count", "3"))
        .execute();

    retrievedInstance = notebooks.instances().get(gcpNotebookInstanceName).execute();
    var metadata = retrievedInstance.getMetadata();
    assertEquals("bar", metadata.get("foo"));
    assertEquals("3", metadata.get("count"));

    notebooks.instances().delete(gcpNotebookInstanceName).execute();
  }

  @Test
  public void setGetTestIamPolicyNotebookInstance() throws Exception {
    GcpNotebookInstanceName gcpNotebookInstanceName =
        defaultInstanceName().instanceId("set-get-iam").build();
    createInstance(gcpNotebookInstanceName);

    String userEmail = IntegrationCredentials.getUserGoogleCredentialsOrDie().getClientEmail();
    Binding binding =
        new Binding()
            .setRole("roles/notebooks.viewer")
            .setMembers(ImmutableList.of("serviceAccount:" + userEmail));
    Policy policy = notebooks.instances().getIamPolicy(gcpNotebookInstanceName).execute();
    policy.setBindings(ImmutableList.of(binding));

    Policy updatedPolicy =
        notebooks
            .instances()
            .setIamPolicy(gcpNotebookInstanceName, new SetIamPolicyRequest().setPolicy(policy))
            .execute();

    assertThat(updatedPolicy.getBindings(), Matchers.hasItem(binding));
    Policy secondRetrieval = notebooks.instances().getIamPolicy(gcpNotebookInstanceName).execute();
    assertThat(secondRetrieval.getBindings(), Matchers.hasItem(binding));

    // Test the permissions of the user for which the IAM policy was set.
    GcpNotebooksCow userNotebooks =
        GcpNotebooksCow.create(
            IntegrationUtils.DEFAULT_CLIENT_CONFIG,
            IntegrationCredentials.getUserGoogleCredentialsOrDie());
    // Notebook get permission from "roles/notebooks.viewer".
    String getNotebookPermission = "notebooks.instances.get";
    TestIamPermissionsResponse iamResponse =
        userNotebooks
            .instances()
            .testIamPermissions(
                gcpNotebookInstanceName,
                new TestIamPermissionsRequest()
                    .setPermissions(ImmutableList.of(getNotebookPermission)))
            .execute();
    assertThat(iamResponse.getPermissions(), Matchers.contains(getNotebookPermission));

    notebooks.instances().delete(gcpNotebookInstanceName).execute();
  }

  @Test
  public void stopStartNotebookInstance() throws Exception {
    GcpNotebookInstanceName gcpNotebookInstanceName =
        defaultInstanceName().instanceId("stop-start").build();
    createInstance(gcpNotebookInstanceName);

    OperationCow<Operation> stopOperation =
        notebooks
            .operations()
            .operationCow(notebooks.instances().stop(gcpNotebookInstanceName).execute());
    OperationTestUtils.pollAndAssertSuccess(
        stopOperation, Duration.ofSeconds(10), Duration.ofMinutes(4));
    assertEquals(
        "STOPPED", notebooks.instances().get(gcpNotebookInstanceName).execute().getState());

    OperationCow<Operation> startOperation =
        notebooks
            .operations()
            .operationCow(notebooks.instances().start(gcpNotebookInstanceName).execute());
    OperationTestUtils.pollAndAssertSuccess(
        startOperation, Duration.ofSeconds(10), Duration.ofMinutes(4));
    assertEquals(
        "PROVISIONING", notebooks.instances().get(gcpNotebookInstanceName).execute().getState());

    notebooks.instances().delete(gcpNotebookInstanceName).execute();
  }

  @Test
  public void instanceCreateSerialize() throws Exception {
    assertEquals(
        "{\"projectId\":\"my-project\",\"locations\":\"us-east1-b\","
            + "\"instanceId\":\"my-id\","
            + "\"instance\":{\"machineType\":\"e2-standard-2\",\"network\":\""
            + reusableNetwork.getSelfLink()
            + "\","
            + "\"vmImage\":{\"imageFamily\":\"common-cpu\",\"project\":\"deeplearning-platform-release\"}}}",
        notebooks
            .instances()
            .create(
                GcpNotebookInstanceName.builder()
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
                GcpNotebookInstanceName.builder()
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
                GcpNotebookInstanceName.builder()
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
                GcpNotebookInstanceName.builder()
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
                GcpNotebookInstanceName.builder()
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
                GcpNotebookInstanceName.builder()
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
                GcpNotebookInstanceName.builder()
                    .projectId("my-project")
                    .location("us-east1-b")
                    .instanceId("my-id")
                    .build())
            .serialize()
            .toString());
  }

  @Test
  public void instanceTestIamPermissionsSerialize() throws Exception {
    assertEquals(
        "{\"projectId\":\"my-project\",\"locations\":\"us-east1-b\","
            + "\"instanceId\":\"my-id\",\"content\":{\"permissions\":[\"myPermission\"]}}",
        notebooks
            .instances()
            .testIamPermissions(
                GcpNotebookInstanceName.builder()
                    .projectId("my-project")
                    .location("us-east1-b")
                    .instanceId("my-id")
                    .build(),
                new TestIamPermissionsRequest().setPermissions(ImmutableList.of("myPermission")))
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
