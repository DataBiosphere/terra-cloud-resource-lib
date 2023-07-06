package bio.terra.cloudres.google.dataproc;

import static bio.terra.cloudres.google.compute.testing.NetworkUtils.createDataprocIngressRule;
import static bio.terra.cloudres.testing.IntegrationCredentials.getAdminGoogleCredentialsOrDie;
import static bio.terra.cloudres.testing.IntegrationUtils.createServiceAccount;
import static bio.terra.cloudres.testing.IntegrationUtils.grantServiceAccountRole;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.Assert.assertThrows;
import static org.junit.jupiter.api.Assertions.assertEquals;

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
import com.google.api.services.dataproc.model.Binding;
import com.google.api.services.dataproc.model.Cluster;
import com.google.api.services.dataproc.model.ClusterConfig;
import com.google.api.services.dataproc.model.GceClusterConfig;
import com.google.api.services.dataproc.model.InstanceGroupConfig;
import com.google.api.services.dataproc.model.LifecycleConfig;
import com.google.api.services.dataproc.model.ListClustersResponse;
import com.google.api.services.dataproc.model.Operation;
import com.google.api.services.dataproc.model.Policy;
import com.google.api.services.dataproc.model.SetIamPolicyRequest;
import com.google.api.services.dataproc.model.TestIamPermissionsRequest;
import com.google.api.services.dataproc.model.TestIamPermissionsResponse;
import com.google.api.services.iam.v1.model.ServiceAccount;
import com.google.common.collect.ImmutableList;
import java.io.IOException;
import java.security.GeneralSecurityException;
import java.time.Duration;
import java.util.List;
import java.util.stream.Collectors;
import org.hamcrest.Matchers;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

@Tag("integration")
public class DataprocCowTest {
  private static final DataprocCow dataproc = defaultDataprocCow();
  /** A dynamically created Google Project to manipulate dataproc within for testing. */
  private static Project reusableProject;

  private static Network reusableNetwork;
  /** A custom dataproc worker service account for dataproc VMs to use. */
  private static ServiceAccount dataprocWorkerServiceAccount;

  /**
   * Configure a dynamic gcp project to support dataproc cluster management. This includes:
   *
   * <p>1. Creating a custom service account with the dataproc worker role to be attached to
   * dataproc vm nodes. See https://cloud.google.com/dataproc/docs/concepts/iam/dataproc-principals.
   *
   * <p>2. Create an allow all ingress firewall rule in the project's vpc network to allow
   * inter-node communication.
   *
   * @throws Exception on failure.
   */
  @BeforeAll
  public static void setupProject() throws Exception {
    reusableProject = ProjectUtils.executeCreateProject();
    CloudBillingUtils.setDefaultProjectBilling(reusableProject.getProjectId());
    ServiceUsageUtils.enableServices(
        reusableProject.getProjectId(), ImmutableList.of("dataproc.googleapis.com"));

    reusableNetwork = NetworkUtils.exceuteCreateNetwork(reusableProject.getProjectId(), true);
    createDataprocIngressRule(reusableProject.getProjectId(), reusableNetwork.getName());

    dataprocWorkerServiceAccount = createServiceAccount(reusableProject, "dataproc-worker");
    grantServiceAccountRole(reusableProject, dataprocWorkerServiceAccount, "roles/dataproc.worker");
  }

  private static DataprocCow defaultDataprocCow() {
    try {
      return DataprocCow.create(
          IntegrationUtils.DEFAULT_CLIENT_CONFIG, getAdminGoogleCredentialsOrDie());
    } catch (GeneralSecurityException | IOException e) {
      throw new IllegalStateException("Unable to create dataproc cow.", e);
    }
  }

  private static ClusterName.Builder defaultClusterName() {
    return ClusterName.builder()
        .projectId(reusableProject.getProjectId())
        .region("us-east1")
        .name("default-cluster-name");
  }

  /**
   * Utility method for creating a dataproc cluster. Blocks until the cluster is created
   * successfully or fails
   */
  private void createCluster(ClusterName clusterName) throws IOException, InterruptedException {
    OperationCow<Operation> createOperation =
        dataproc
            .regionOperations()
            .operationCow(
                dataproc
                    .clusters()
                    .create(clusterName, defaultCluster().setClusterName(clusterName.name()))
                    .execute());
    OperationTestUtils.pollAndAssertSuccess(
        createOperation, Duration.ofSeconds(30), Duration.ofMinutes(20));
  }

  /** Default {@link Cluster} used to create clusters in tests. */
  private static Cluster defaultCluster() {
    return new Cluster()
        .setConfig(
            new ClusterConfig()
                .setGceClusterConfig(
                    new GceClusterConfig()
                        .setNetworkUri(reusableNetwork.getSelfLink())
                        .setServiceAccount(dataprocWorkerServiceAccount.getEmail())
                        .setTags(List.of("dataproc"))) // Set tag required for firewall rule
                .setMasterConfig(
                    // use e2-standard-2 instance because n1-standard-1 instances are not supported
                    // by dataproc
                    new InstanceGroupConfig().setNumInstances(1).setMachineTypeUri("e2-standard-2"))
                .setWorkerConfig(
                    new InstanceGroupConfig().setNumInstances(2).setMachineTypeUri("e2-standard-2"))
                .setLifecycleConfig(
                    // 30m. GCP expects the duration in seconds suffixed with "s"
                    new LifecycleConfig().setAutoDeleteTtl("1800s")));
  }

  @Test
  public void createGetListDeleteDataprocCluster() throws Exception {
    ClusterName clusterName = defaultClusterName().name("create-delete-cluster").build();
    createCluster(clusterName);

    Cluster retrievedCluster = dataproc.clusters().get(clusterName).execute();
    assertEquals(clusterName.name(), retrievedCluster.getClusterName());

    ListClustersResponse listResponse =
        dataproc.clusters().list(clusterName.projectId(), clusterName.region()).execute();

    // There may be clusters from other tests.
    assertThat(listResponse.getClusters().size(), Matchers.greaterThan(0));
    assertThat(
        listResponse.getClusters().stream()
            .map(Cluster::getClusterName)
            .collect(Collectors.toList()),
        Matchers.hasItem(clusterName.name()));

    OperationCow<Operation> deleteOperation =
        dataproc.regionOperations().operationCow(dataproc.clusters().delete(clusterName).execute());
    OperationTestUtils.pollAndAssertSuccess(
        deleteOperation, Duration.ofSeconds(30), Duration.ofMinutes(10));

    GoogleJsonResponseException e =
        assertThrows(
            GoogleJsonResponseException.class,
            () -> dataproc.clusters().get(clusterName).execute());
    assertEquals(404, e.getStatusCode());
  }

  @Test
  public void updateDataprocCluster() throws Exception {
    ClusterName clusterName = defaultClusterName().name("update-cluster").build();
    createCluster(clusterName);

    Cluster retrievedCluster = dataproc.clusters().get(clusterName).execute();
    assertEquals(clusterName.name(), retrievedCluster.getClusterName());

    String updateMask = "config.worker_config.num_instances";
    retrievedCluster.setConfig(
        retrievedCluster
            .getConfig()
            .setWorkerConfig(retrievedCluster.getConfig().getWorkerConfig().setNumInstances(3)));

    OperationCow<Operation> patchOperation =
        dataproc
            .regionOperations()
            .operationCow(
                dataproc
                    .clusters()
                    .patch(clusterName, retrievedCluster, updateMask, null)
                    .execute());

    OperationTestUtils.pollAndAssertSuccess(
        patchOperation, Duration.ofSeconds(10), Duration.ofMinutes(4));

    Cluster updatedCluster = dataproc.clusters().get(clusterName).execute();
    assertEquals(updatedCluster.getConfig().getWorkerConfig().getNumInstances(), 3);

    dataproc.clusters().delete(clusterName).execute();
  }

  @Test
  public void setGetTestIamPolicyDataprocCluster() throws Exception {
    ClusterName clusterName = defaultClusterName().name("set-get-iam-cluster").build();
    createCluster(clusterName);

    String userEmail = IntegrationCredentials.getUserGoogleCredentialsOrDie().getClientEmail();
    Binding binding =
        new Binding()
            .setRole("roles/dataproc.viewer")
            .setMembers(ImmutableList.of("serviceAccount:" + userEmail));
    Policy policy = dataproc.clusters().getIamPolicy(clusterName).execute();
    policy.setBindings(ImmutableList.of(binding));

    Policy updatedPolicy =
        dataproc
            .clusters()
            .setIamPolicy(clusterName, new SetIamPolicyRequest().setPolicy(policy))
            .execute();

    assertThat(updatedPolicy.getBindings(), Matchers.hasItem(binding));
    Policy secondRetrieval = dataproc.clusters().getIamPolicy(clusterName).execute();
    assertThat(secondRetrieval.getBindings(), Matchers.hasItem(binding));

    // Test the permissions of the user for which the IAM policy was set.
    DataprocCow userDataprocCow =
        DataprocCow.create(
            IntegrationUtils.DEFAULT_CLIENT_CONFIG,
            IntegrationCredentials.getUserGoogleCredentialsOrDie());
    // Cluster get permission from "roles/dataproc.viewer".
    String getClusterPermission = "dataproc.clusters.get";
    TestIamPermissionsResponse iamResponse =
        userDataprocCow
            .clusters()
            .testIamPermissions(
                clusterName,
                new TestIamPermissionsRequest()
                    .setPermissions(ImmutableList.of(getClusterPermission)))
            .execute();
    assertThat(iamResponse.getPermissions(), Matchers.contains(getClusterPermission));

    dataproc.clusters().delete(clusterName).execute();
  }

  @Test
  public void stopStartDataprocCluster() throws Exception {
    ClusterName clusterName = defaultClusterName().name("stop-start-cluster").build();
    createCluster(clusterName);

    OperationCow<Operation> stopOperation =
        dataproc.regionOperations().operationCow(dataproc.clusters().stop(clusterName).execute());
    OperationTestUtils.pollAndAssertSuccess(
        stopOperation, Duration.ofSeconds(10), Duration.ofMinutes(4));
    assertEquals("STOPPED", dataproc.clusters().get(clusterName).execute().getStatus().getState());

    OperationCow<Operation> startOperation =
        dataproc.regionOperations().operationCow(dataproc.clusters().start(clusterName).execute());
    OperationTestUtils.pollAndAssertSuccess(
        startOperation, Duration.ofSeconds(10), Duration.ofMinutes(4));
    assertEquals("RUNNING", dataproc.clusters().get(clusterName).execute().getStatus().getState());

    dataproc.clusters().delete(clusterName).execute();
  }

  @Test
  public void clusterCreateSerialize() throws Exception {
    String expected =
        "{\"projectId\":\"my-project\",\"region\":\"us-east1\",\"cluster\":{\"config\":{\"gceClusterConfig\":{\"networkUri\":\""
            + reusableNetwork.getSelfLink()
            + "\",\"serviceAccount\":\""
            + dataprocWorkerServiceAccount.getEmail()
            + "\",\"tags\":[\"dataproc\"]},\"lifecycleConfig\":{\"autoDeleteTtl\":\"1800s\"},\"masterConfig\":{\"machineTypeUri\":\"e2-standard-2\",\"numInstances\":1},\"workerConfig\":{\"machineTypeUri\":\"e2-standard-2\",\"numInstances\":2}}}}";
    String actual =
        dataproc
            .clusters()
            .create(
                ClusterName.builder()
                    .projectId("my-project")
                    .region("us-east1")
                    .name("my-id")
                    .build(),
                defaultCluster())
            .serialize()
            .toString();
    assertEquals(expected, actual);
  }

  @Test
  public void clusterGetSerialize() throws Exception {
    String expected =
        "{\"projectId\":\"my-project\",\"region\":\"us-east1\",\"cluster\":\"my-id\"}";
    String actual =
        dataproc
            .clusters()
            .get(
                ClusterName.builder()
                    .projectId("my-project")
                    .region("us-east1")
                    .name("my-id")
                    .build())
            .serialize()
            .toString();
    assertEquals(expected, actual);
  }

  @Test
  public void clusterDeleteSerialize() throws Exception {
    String expected =
        "{\"projectId\":\"my-project\",\"region\":\"us-east1\",\"clusterName\":\"my-id\"}";
    String actual =
        dataproc
            .clusters()
            .delete(
                ClusterName.builder()
                    .projectId("my-project")
                    .region("us-east1")
                    .name("my-id")
                    .build())
            .serialize()
            .toString();
    assertEquals(expected, actual);
  }

  @Test
  public void clusterListSerialize() throws Exception {
    String expected =
        "{\"project\":\"my-project\",\"region\":\"us-east1\",\"filter\":null,\"page_size\":10,\"page_token\":\"my-page-token\"}";
    String actual =
        dataproc
            .clusters()
            .list("my-project", "us-east1")
            .setPageSize(10)
            .setPageToken("my-page-token")
            .serialize()
            .toString();
    assertEquals(expected, actual);
  }

  @Test
  public void clusterGetIamPolicySerialize() throws Exception {
    String expected =
        "{\"resource\":\"projects/my-project/regions/us-east1/clusters/my-id\",\"content\":{}}";
    String actual =
        dataproc
            .clusters()
            .getIamPolicy(
                ClusterName.builder()
                    .projectId("my-project")
                    .region("us-east1")
                    .name("my-id")
                    .build())
            .serialize()
            .toString();
    assertEquals(expected, actual);
  }

  @Test
  public void clusterUpdateSerialize() throws Exception {
    Cluster defaultCluster = defaultCluster().setClusterName("my-id");
    defaultCluster.setConfig(
        defaultCluster
            .getConfig()
            .setWorkerConfig(defaultCluster.getConfig().getWorkerConfig().setNumInstances(3)));
    String updateMask = "config.worker_config.num_instances";

    String expected =
        "{\"projectId\":\"my-project\",\"region\":\"us-east1\",\"clusterName\":\"my-id\",\"updateMask\":\"config.worker_config.num_instances\",\"gracefulDecommissionTimeout\":null}";
    String actual =
        dataproc
            .clusters()
            .patch(
                ClusterName.builder()
                    .projectId("my-project")
                    .region("us-east1")
                    .name("my-id")
                    .build(),
                defaultCluster,
                updateMask,
                null)
            .serialize()
            .toString();
    assertEquals(expected, actual);
  }

  @Test
  public void clusterSetIamPolicySerialize() throws Exception {
    Binding binding =
        new Binding()
            .setRole("roles/dataproc.viewer")
            .setMembers(ImmutableList.of("userEmail:foo@gmail.com"));
    SetIamPolicyRequest request =
        new SetIamPolicyRequest().setPolicy(new Policy().setBindings(ImmutableList.of(binding)));

    String expected =
        "{\"resource\":\"projects/my-project/regions/us-east1/clusters/my-id\",\"content\":{\"policy\":{\"bindings\":[{\"members\":[\"userEmail:foo@gmail.com\"],\"role\":\"roles/dataproc.viewer\"}]}}}";
    String actual =
        dataproc
            .clusters()
            .setIamPolicy(
                ClusterName.builder()
                    .projectId("my-project")
                    .region("us-east1")
                    .name("my-id")
                    .build(),
                request)
            .serialize()
            .toString();
    assertEquals(expected, actual);
  }

  @Test
  public void clusterStartSerialize() throws Exception {
    String expected =
        "{\"projectId\":\"my-project\",\"region\":\"us-east1\",\"clusterName\":\"my-id\"}";
    String actual =
        dataproc
            .clusters()
            .start(
                ClusterName.builder()
                    .projectId("my-project")
                    .region("us-east1")
                    .name("my-id")
                    .build())
            .serialize()
            .toString();
    assertEquals(expected, actual);
  }

  @Test
  public void clusterStopSerialize() throws Exception {
    String expected =
        "{\"projectId\":\"my-project\",\"region\":\"us-east1\",\"cluster\":\"my-id\"}";
    String actual =
        dataproc
            .clusters()
            .stop(
                ClusterName.builder()
                    .projectId("my-project")
                    .region("us-east1")
                    .name("my-id")
                    .build())
            .serialize()
            .toString();
    assertEquals(expected, actual);
  }

  @Test
  public void clusterTestIamPermissionsSerialize() throws Exception {
    String expected =
        "{\"resource\":\"projects/my-project/regions/us-east1/clusters/my-id\",\"content\":{\"permissions\":[\"myPermission\"]}}";
    String actual =
        dataproc
            .clusters()
            .testIamPermissions(
                ClusterName.builder()
                    .projectId("my-project")
                    .region("us-east1")
                    .name("my-id")
                    .build(),
                new TestIamPermissionsRequest().setPermissions(ImmutableList.of("myPermission")))
            .serialize()
            .toString();
    assertEquals(expected, actual);
  }
}
