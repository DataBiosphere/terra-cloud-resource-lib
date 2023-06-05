package bio.terra.cloudres.google.compute;

import static bio.terra.cloudres.google.compute.testing.NetworkUtils.exceuteCreateNetwork;
import static bio.terra.cloudres.google.compute.testing.NetworkUtils.randomNetworkName;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.*;

import bio.terra.cloudres.google.api.services.common.OperationCow;
import bio.terra.cloudres.google.api.services.common.testing.OperationTestUtils;
import bio.terra.cloudres.google.billing.testing.CloudBillingUtils;
import bio.terra.cloudres.google.cloudresourcemanager.testing.ProjectUtils;
import bio.terra.cloudres.google.serviceusage.testing.ServiceUsageUtils;
import bio.terra.cloudres.testing.IntegrationCredentials;
import bio.terra.cloudres.testing.IntegrationUtils;
import com.google.api.client.googleapis.json.GoogleJsonResponseException;
import com.google.api.services.cloudresourcemanager.v3.model.Project;
import com.google.api.services.compute.model.*;
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
public class CloudComputeCowTest {
  private static final String COMPUTE_SERVICE_ID = "compute.googleapis.com";

  private static Project reusableProject;

  private static CloudComputeCow defaultCompute() throws GeneralSecurityException, IOException {
    return CloudComputeCow.create(
        IntegrationUtils.DEFAULT_CLIENT_CONFIG,
        IntegrationCredentials.getAdminGoogleCredentialsOrDie());
  }

  @BeforeAll
  public static void createReusableProject() throws Exception {
    reusableProject = createPreparedProject();
  }

  /**
   * Creates an instance for the zone. Blocks until the instance is created successfully or fails
   */
  private void createInstance(String projectId, String zone, String instanceName) throws Exception {
    CloudComputeCow cloudComputeCow = defaultCompute();

    List<AttachedDisk> disks =
        List.of(
            new AttachedDisk()
                .setBoot(true)
                .setInitializeParams(
                    new AttachedDiskInitializeParams()
                        .setSourceImage("projects/debian-cloud/global/images/family/debian-9")
                        .setDiskSizeGb(Long.valueOf(10))));

    OperationCow<Operation> createOperation =
        cloudComputeCow
            .zoneOperations()
            .operationCow(
                projectId,
                zone,
                cloudComputeCow
                    .instances()
                    .insert(
                        projectId,
                        zone,
                        new Instance()
                            .setName(instanceName)
                            .setMachineType("zones/us-central1-a/machineTypes/n1-standard-1")
                            .setDisks(disks))
                    .execute());
    OperationTestUtils.pollAndAssertSuccess(
        createOperation, Duration.ofSeconds(30), Duration.ofMinutes(12));
  }

  @Test
  public void createGetListDeleteInstance() throws Exception {
    CloudComputeCow cloudComputeCow = defaultCompute();

    String projectId = reusableProject.getProjectId();
    String zone = "us-central1-a";
    String name = "default-name";

    createInstance(projectId, zone, name);

    Instance retrievedInstance = cloudComputeCow.instances().get(projectId, zone, name).execute();
    assertEquals(name, retrievedInstance.getName());

    InstanceList instanceList = cloudComputeCow.instances().list(projectId, zone).execute();
    assertThat(instanceList.getItems().size(), Matchers.greaterThan(0));
    assertThat(
        instanceList.getItems().stream().map(Instance::getName).collect(Collectors.toList()),
        Matchers.hasItem(name));

    OperationCow<Operation> deleteOperation =
        cloudComputeCow
            .zoneOperations()
            .operationCow(
                projectId,
                zone,
                cloudComputeCow.instances().delete(projectId, zone, name).execute());
    OperationTestUtils.pollAndAssertSuccess(
        deleteOperation, Duration.ofSeconds(30), Duration.ofMinutes(5));

    GoogleJsonResponseException e =
        assertThrows(
            GoogleJsonResponseException.class,
            () -> cloudComputeCow.instances().get(projectId, zone, name).execute());
    assertEquals(404, e.getStatusCode());
  }

  @Test
  public void setInstanceMetadata() throws Exception {
    CloudComputeCow cloudComputeCow = defaultCompute();

    String projectId = reusableProject.getProjectId();
    String zone = "us-central1-a";
    String name = "gce-instance-with-metadata";

    createInstance(projectId, zone, name);

    Instance retrievedInstance = cloudComputeCow.instances().get(projectId, zone, name).execute();
    assertEquals(name, retrievedInstance.getName());

    cloudComputeCow
        .instances()
        .setMetadata(projectId, zone, name, new Metadata().set("foo", "bar").set("count", "3"))
        .execute();

    retrievedInstance = cloudComputeCow.instances().get(projectId, zone, name).execute();
    var metadata = retrievedInstance.getMetadata();
    assertEquals("bar", metadata.get("foo"));
    assertEquals("3", metadata.get("count"));

    cloudComputeCow.instances().delete(projectId, zone, name).execute();
  }

  @Test
  public void setGetTestIamPolicyInstance() throws Exception {
    CloudComputeCow cloudComputeCow = defaultCompute();

    String projectId = reusableProject.getProjectId();
    String zone = "us-central1-a";
    String name = "instance-iam-set-get";

    createInstance(projectId, zone, name);

    String userEmail = IntegrationCredentials.getUserGoogleCredentialsOrDie().getClientEmail();
    Binding binding =
        new Binding()
            .setRole("roles/compute.viewer")
            .setMembers(ImmutableList.of("serviceAccount:" + userEmail));
    Policy policy = cloudComputeCow.instances().getIamPolicy(projectId, zone, name).execute();
    policy.setBindings(ImmutableList.of(binding));

    Policy updatedPolicy =
        cloudComputeCow
            .instances()
            .setIamPolicy(projectId, zone, name, new ZoneSetPolicyRequest().setPolicy(policy))
            .execute();

    assertThat(updatedPolicy.getBindings(), Matchers.hasItem(binding));
    Policy secondRetrieval =
        cloudComputeCow.instances().getIamPolicy(projectId, zone, name).execute();
    assertThat(secondRetrieval.getBindings(), Matchers.hasItem(binding));

    // Test the permissions of the user for which the IAM policy was set.
    CloudComputeCow userInstances =
        CloudComputeCow.create(
            IntegrationUtils.DEFAULT_CLIENT_CONFIG,
            IntegrationCredentials.getUserGoogleCredentialsOrDie());
    // Instance get permission from "roles/compute.viewer".
    String getInstancePermission = "compute.instances.get";
    TestPermissionsResponse iamResponse =
        userInstances
            .instances()
            .testIamPermissions(
                projectId,
                zone,
                name,
                new TestPermissionsRequest()
                    .setPermissions(ImmutableList.of(getInstancePermission)))
            .execute();
    assertThat(iamResponse.getPermissions(), Matchers.contains(getInstancePermission));

    cloudComputeCow.instances().delete(projectId, zone, name).execute();
  }

  @Test
  public void stopStartInstance() throws Exception {
    CloudComputeCow cloudComputeCow = defaultCompute();

    String projectId = reusableProject.getProjectId();
    String zone = "us-central1-a";
    String name = "instance-stop-start";

    createInstance(projectId, zone, name);

    OperationCow<Operation> stopOperation =
        cloudComputeCow
            .zoneOperations()
            .operationCow(
                projectId, zone, cloudComputeCow.instances().stop(projectId, zone, name).execute());
    OperationTestUtils.pollAndAssertSuccess(
        stopOperation, Duration.ofSeconds(10), Duration.ofMinutes(4));
    assertEquals(
        "TERMINATED", cloudComputeCow.instances().get(projectId, zone, name).execute().getStatus());

    OperationCow<Operation> startOperation =
        cloudComputeCow
            .zoneOperations()
            .operationCow(
                projectId,
                zone,
                cloudComputeCow.instances().start(projectId, zone, name).execute());
    OperationTestUtils.pollAndAssertSuccess(
        startOperation, Duration.ofSeconds(10), Duration.ofMinutes(4));
    assertEquals(
        "PROVISIONING",
        cloudComputeCow.instances().get(projectId, zone, name).execute().getStatus());

    cloudComputeCow.instances().delete(projectId, zone, name).execute();
  }

  @Test
  public void createAndGetAndDeleteNetwork() throws Exception {
    CloudComputeCow cloudComputeCow = defaultCompute();

    String projectId = reusableProject.getProjectId();
    String netWorkName = randomNetworkName();
    Network network = new Network().setName(netWorkName).setAutoCreateSubnetworks(false);
    Operation operation = cloudComputeCow.networks().insert(projectId, network).execute();
    OperationTestUtils.pollAndAssertSuccess(
        cloudComputeCow.globalOperations().operationCow(projectId, operation),
        Duration.ofSeconds(5),
        Duration.ofSeconds(100));

    Network createdNetwork = cloudComputeCow.networks().get(projectId, netWorkName).execute();

    assertEquals(netWorkName, createdNetwork.getName());
    assertFalse(createdNetwork.getAutoCreateSubnetworks());

    Operation deleteOperation = cloudComputeCow.networks().delete(projectId, netWorkName).execute();
    OperationTestUtils.pollAndAssertSuccess(
        cloudComputeCow.globalOperations().operationCow(projectId, deleteOperation),
        Duration.ofSeconds(5),
        Duration.ofSeconds(100));
    GoogleJsonResponseException e =
        assertThrows(
            GoogleJsonResponseException.class,
            () -> cloudComputeCow.networks().get(projectId, netWorkName).execute());
    assertEquals(404, e.getStatusCode());
  }

  @Test
  public void createAndGetAndListAndAggregatedListSubnetwork() throws Exception {
    String projectId = reusableProject.getProjectId();
    String region = "us-west1";
    String ipCidrRange = "10.130.0.0/20";
    CloudComputeCow cloudComputeCow = defaultCompute();

    // Create parent network
    Network network = exceuteCreateNetwork(projectId, false);

    String subnetWorkName = randomNetworkName();
    Subnetwork subnetwork =
        new Subnetwork()
            .setName(subnetWorkName)
            .setNetwork(network.getSelfLink())
            .setIpCidrRange(ipCidrRange);
    Operation regionOperation =
        cloudComputeCow.subnetworks().insert(projectId, region, subnetwork).execute();
    OperationTestUtils.pollAndAssertSuccess(
        cloudComputeCow.regionalOperations().operationCow(projectId, region, regionOperation),
        Duration.ofSeconds(5),
        Duration.ofSeconds(100));

    Subnetwork createdSubnet =
        cloudComputeCow.subnetworks().get(projectId, region, subnetWorkName).execute();

    assertEquals(subnetWorkName, createdSubnet.getName());
    assertEquals(network.getSelfLink(), createdSubnet.getNetwork());
    assertEquals(ipCidrRange, createdSubnet.getIpCidrRange());
    assertEquals(regionName(projectId, region), createdSubnet.getRegion());

    SubnetworkList subnetworkList = cloudComputeCow.subnetworks().list(projectId, region).execute();
    assertThat(subnetworkList.getItems().size(), Matchers.greaterThan(0));

    SubnetworkAggregatedList subnetworkAggregatedList =
        cloudComputeCow.subnetworks().aggregatedList(projectId).execute();
    assertThat(subnetworkAggregatedList.getItems().size(), Matchers.greaterThan(0));
  }

  @Test
  public void createGetAndDeleteFirewall() throws Exception {
    String projectId = reusableProject.getProjectId();

    CloudComputeCow cloudComputeCow = defaultCompute();

    // Create parent network
    Network network = exceuteCreateNetwork(projectId, false);

    String firewallName = "allow-internal";
    Firewall.Allowed allowed = new Firewall.Allowed().setIPProtocol("icmp");
    Firewall firewall =
        new Firewall()
            .setName(firewallName)
            .setAllowed(ImmutableList.of(allowed))
            .setNetwork(network.getSelfLink());
    Operation operation = cloudComputeCow.firewalls().insert(projectId, firewall).execute();
    OperationTestUtils.pollAndAssertSuccess(
        cloudComputeCow.globalOperations().operationCow(projectId, operation),
        Duration.ofSeconds(5),
        Duration.ofSeconds(100));

    Firewall createdFirewall = cloudComputeCow.firewalls().get(projectId, firewallName).execute();

    assertEquals(firewallName, createdFirewall.getName());
    assertThat(createdFirewall.getAllowed(), Matchers.contains(allowed));

    Operation deleteOperation =
        cloudComputeCow.firewalls().delete(projectId, firewallName).execute();
    OperationTestUtils.pollAndAssertSuccess(
        cloudComputeCow.globalOperations().operationCow(projectId, deleteOperation),
        Duration.ofSeconds(5),
        Duration.ofSeconds(100));
    GoogleJsonResponseException e =
        assertThrows(
            GoogleJsonResponseException.class,
            () -> cloudComputeCow.firewalls().get(projectId, firewallName).execute());
    assertEquals(404, e.getStatusCode());
  }

  @Test
  public void createAndGetRoute() throws Exception {
    String projectId = reusableProject.getProjectId();

    CloudComputeCow cloudComputeCow = defaultCompute();

    // Create parent network
    Network network = exceuteCreateNetwork(projectId, false);

    String routeName = "private-google-access-route";
    String destRange = "199.36.153.4/30";
    String nextHopGateway = "projects/" + projectId + "/global/gateways/default-internet-gateway";
    Route route =
        new Route()
            .setName(routeName)
            .setDestRange(destRange)
            .setNextHopGateway(nextHopGateway)
            .setNetwork(network.getSelfLink());
    Operation operation = cloudComputeCow.routes().insert(projectId, route).execute();
    OperationTestUtils.pollAndAssertSuccess(
        cloudComputeCow.globalOperations().operationCow(projectId, operation),
        Duration.ofSeconds(5),
        Duration.ofSeconds(100));

    Route createdRoute = cloudComputeCow.routes().get(projectId, routeName).execute();

    assertEquals(routeName, createdRoute.getName());
    assertEquals(destRange, createdRoute.getDestRange());
    assertEquals(
        "https://www.googleapis.com/compute/v1/" + nextHopGateway,
        createdRoute.getNextHopGateway());
  }

  @Test
  public void createAndGetAndDeleteRouter() throws Exception {
    final CloudComputeCow cloudComputeCow = defaultCompute();

    final String projectId = reusableProject.getProjectId();
    final String region = "us-west1";

    // Create parent network
    Network network = exceuteCreateNetwork(projectId, false);

    final String routerName = randomRouterName();
    final String gatewayName = randomGatewayName();
    final RouterNat nat =
        new RouterNat()
            .setName(gatewayName)
            .setSourceSubnetworkIpRangesToNat("ALL_SUBNETWORKS_ALL_IP_RANGES")
            .setNatIpAllocateOption("AUTO_ONLY");
    final Router router =
        new Router()
            .setName(routerName)
            .setRegion(region)
            .setNetwork(networkName(projectId, network.getName()))
            .setNats(ImmutableList.of(nat));
    final Operation insertRouterOperation =
        cloudComputeCow.routers().insert(projectId, region, router).execute();
    OperationTestUtils.pollAndAssertSuccess(
        cloudComputeCow.regionalOperations().operationCow(projectId, region, insertRouterOperation),
        Duration.ofSeconds(5),
        Duration.ofSeconds(100));

    final Router createdRouter =
        cloudComputeCow.routers().get(projectId, region, routerName).execute();

    assertEquals(routerName, createdRouter.getName());
    assertEquals(regionName(projectId, region), createdRouter.getRegion());
    assertEquals(networkName(projectId, network.getName()), createdRouter.getNetwork());

    final List<RouterNat> nats = createdRouter.getNats();
    assertEquals(nats.size(), 1);
    final RouterNat createdNat = nats.get(0);
    assertEquals(gatewayName, createdNat.getName());
    assertEquals("ALL_SUBNETWORKS_ALL_IP_RANGES", createdNat.getSourceSubnetworkIpRangesToNat());
    assertEquals("AUTO_ONLY", createdNat.getNatIpAllocateOption());

    final Operation deleteRouterOperation =
        cloudComputeCow.routers().delete(projectId, region, routerName).execute();
    OperationTestUtils.pollAndAssertSuccess(
        cloudComputeCow.regionalOperations().operationCow(projectId, region, deleteRouterOperation),
        Duration.ofSeconds(5),
        Duration.ofSeconds(100));
    final GoogleJsonResponseException deleteRouterException =
        assertThrows(
            GoogleJsonResponseException.class,
            () -> cloudComputeCow.routers().get(projectId, region, routerName).execute());
    assertEquals(404, deleteRouterException.getStatusCode());

    final Operation deleteNetworkOperation =
        cloudComputeCow.networks().delete(projectId, network.getName()).execute();
    OperationTestUtils.pollAndAssertSuccess(
        cloudComputeCow.globalOperations().operationCow(projectId, deleteNetworkOperation),
        Duration.ofSeconds(5),
        Duration.ofSeconds(100));
    final GoogleJsonResponseException deleteNetworkException =
        assertThrows(
            GoogleJsonResponseException.class,
            () -> cloudComputeCow.networks().get(projectId, network.getName()).execute());
    assertEquals(404, deleteNetworkException.getStatusCode());
  }

  @Test
  public void getZone() throws Exception {
    Zone zone =
        defaultCompute().zones().get(reusableProject.getProjectId(), "us-east1-b").execute();
    assertThat(zone.getRegion(), Matchers.containsString("us-east1"));
  }

  @Test
  public void listZone() throws Exception {
    ZoneList zoneList = defaultCompute().zones().list(reusableProject.getProjectId()).execute();
    assertThat(zoneList.getItems().size(), Matchers.greaterThan(0));
  }

  @Test
  public void instanceCreateSerialize() throws Exception {
    CloudComputeCow cloudComputeCow = defaultCompute();

    assertEquals(
        "{\"projectId\":\"my-project\",\"zone\":\"us-east1-b\","
            + "\"instance\":{\"name\":\"my-id\"}}",
        cloudComputeCow
            .instances()
            .insert("my-project", "us-east1-b", new Instance().setName("my-id"))
            .serialize()
            .toString());
  }

  @Test
  public void instanceGetSerialize() throws Exception {
    CloudComputeCow cloudComputeCow = defaultCompute();

    assertEquals(
        "{\"projectId\":\"my-project\",\"zone\":\"us-east1-b\",\"instance\":\"my-id\"}",
        cloudComputeCow
            .instances()
            .get("my-project", "us-east1-b", "my-id")
            .serialize()
            .toString());
  }

  @Test
  public void instanceDeleteSerialize() throws Exception {
    CloudComputeCow cloudComputeCow = defaultCompute();

    assertEquals(
        "{\"projectId\":\"my-project\",\"zone\":\"us-east1-b\",\"instance\":\"my-id\"}",
        cloudComputeCow
            .instances()
            .delete("my-project", "us-east1-b", "my-id")
            .serialize()
            .toString());
  }

  @Test
  public void instanceListSerialize() throws Exception {
    CloudComputeCow cloudComputeCow = defaultCompute();

    assertEquals(
        "{\"project\":\"my-project\",\"zone\":\"us-east1-b\",\"max_results\":10,"
            + "\"page_token\":\"my-page-token\"}",
        cloudComputeCow
            .instances()
            .list("my-project", "us-east1-b")
            .setMaxResults(Long.valueOf(10))
            .setPageToken("my-page-token")
            .serialize()
            .toString());
  }

  @Test
  public void instanceGetIamPolicySerialize() throws Exception {
    CloudComputeCow cloudComputeCow = defaultCompute();

    assertEquals(
        "{\"project\":\"my-project\",\"zone\":\"us-east1-b\","
            + "\"resource\":\"my-id\",\"options_requested_policy_version\":3}",
        cloudComputeCow
            .instances()
            .getIamPolicy("my-project", "us-east1-b", "my-id")
            .setOptionsRequestedPolicyVersion(3)
            .serialize()
            .toString());
  }

  @Test
  public void instanceSetIamPolicySerialize() throws Exception {
    CloudComputeCow cloudComputeCow = defaultCompute();

    Binding binding =
        new Binding()
            .setRole("roles/compute.viewer")
            .setMembers(ImmutableList.of("userEmail:foo@gmail.com"));
    ZoneSetPolicyRequest request =
        new ZoneSetPolicyRequest().setPolicy(new Policy().setBindings(ImmutableList.of(binding)));
    assertEquals(
        "{\"project\":\"my-project\",\"zone\":\"us-east1-b\","
            + "\"resource\":\"my-id\",\"content\":{\"policy\":{"
            + "\"bindings\":[{\"members\":[\"userEmail:foo@gmail.com\"],"
            + "\"role\":\"roles/compute.viewer\"}]}}}",
        cloudComputeCow
            .instances()
            .setIamPolicy("my-project", "us-east1-b", "my-id", request)
            .serialize()
            .toString());
  }

  @Test
  public void instanceStartSerialize() throws Exception {
    CloudComputeCow cloudComputeCow = defaultCompute();

    assertEquals(
        "{\"projectId\":\"my-project\",\"zone\":\"us-east1-b\",\"instance\":\"my-id\"}",
        cloudComputeCow
            .instances()
            .start("my-project", "us-east1-b", "my-id")
            .serialize()
            .toString());
  }

  @Test
  public void instanceStopSerialize() throws Exception {
    CloudComputeCow cloudComputeCow = defaultCompute();

    assertEquals(
        "{\"projectId\":\"my-project\",\"zone\":\"us-east1-b\",\"instance\":\"my-id\"}",
        cloudComputeCow
            .instances()
            .stop("my-project", "us-east1-b", "my-id")
            .serialize()
            .toString());
  }

  @Test
  public void instanceTestIamPermissionsSerialize() throws Exception {
    CloudComputeCow cloudComputeCow = defaultCompute();

    assertEquals(
        "{\"projectId\":\"my-project\",\"zone\":\"us-east1-b\","
            + "\"resource\":\"my-id\",\"content\":{\"permissions\":[\"myPermission\"]}}",
        cloudComputeCow
            .instances()
            .testIamPermissions(
                "my-project",
                "us-east1-b",
                "my-id",
                new TestPermissionsRequest().setPermissions(ImmutableList.of("myPermission")))
            .serialize()
            .toString());
  }

  @Test
  public void networkInsertSerialize() throws Exception {
    Network network = new Network().setName("network-name");
    CloudComputeCow.Networks.Insert insert =
        defaultCompute().networks().insert("project-id", network);

    assertEquals(
        "{\"project_id\":\"project-id\",\"network\":{\"name\":\"network-name\"}}",
        insert.serialize().toString());
  }

  @Test
  public void networkGetSerialize() throws Exception {
    CloudComputeCow.Networks.Get get =
        defaultCompute().networks().get("project-id", "network-name");

    assertEquals(
        "{\"project_id\":\"project-id\",\"network_name\":\"network-name\"}",
        get.serialize().toString());
  }

  @Test
  public void networkDeleteSerialize() throws Exception {
    CloudComputeCow.Networks.Delete delete =
        defaultCompute().networks().delete("project-id", "network-name");

    assertEquals(
        "{\"project_id\":\"project-id\",\"network_name\":\"network-name\"}",
        delete.serialize().toString());
  }

  @Test
  public void subnetworkInsertSerialize() throws Exception {
    Subnetwork subnetwork = new Subnetwork().setName("subnetwork-name");
    CloudComputeCow.Subnetworks.Insert insert =
        defaultCompute().subnetworks().insert("project-id", "us-west1", subnetwork);

    assertEquals(
        "{\"project_id\":\"project-id\",\"region\":\"us-west1\",\"subnetwork\":{\"name\":\"subnetwork-name\"}}",
        insert.serialize().toString());
  }

  @Test
  public void subnetworkGetSerialize() throws Exception {
    CloudComputeCow.Subnetworks.Get get =
        defaultCompute().subnetworks().get("project-id", "us-west1", "network-name");

    assertEquals(
        "{\"project_id\":\"project-id\",\"region\":\"us-west1\",\"network_name\":\"network-name\"}",
        get.serialize().toString());
  }

  @Test
  public void subnetworkListSerialize() throws Exception {
    CloudComputeCow.Subnetworks.List list =
        defaultCompute()
            .subnetworks()
            .list("project-id", "us-west1")
            .setFilter("my-filter")
            .setMaxResults(42L)
            .setOrderBy("order-by")
            .setPageToken("page-token");

    assertEquals(
        "{\"project_id\":\"project-id\",\"region\":\"us-west1\","
            + "\"filter\":\"my-filter\",\"max_results\":42,\"order_by\":\"order-by\","
            + "\"page_token\":\"page-token\"}",
        list.serialize().toString());
  }

  @Test
  public void subnetworkAggregatedListSerialize() throws Exception {
    CloudComputeCow.Subnetworks.AggregatedList list =
        defaultCompute()
            .subnetworks()
            .aggregatedList("project-id")
            .setFilter("my-filter")
            .setMaxResults(42L)
            .setOrderBy("order-by")
            .setPageToken("page-token");

    assertEquals(
        "{\"project_id\":\"project-id\",\"max_results\":42,\"page_token\":\"page-token\","
            + "\"filter\":\"my-filter\",\"order_by\":\"order-by\"}",
        list.serialize().toString());
  }

  @Test
  public void firewallInsertSerialize() throws Exception {
    Firewall firewall = new Firewall().setName("firewall-name");
    CloudComputeCow.Firewalls.Insert insert =
        defaultCompute().firewalls().insert("project-id", firewall);

    assertEquals(
        "{\"project_id\":\"project-id\",\"firewall\":{\"name\":\"firewall-name\"}}",
        insert.serialize().toString());
  }

  @Test
  public void firewallGetSerialize() throws Exception {
    CloudComputeCow.Firewalls.Get get =
        defaultCompute().firewalls().get("project-id", "firewall-name");

    assertEquals(
        "{\"project_id\":\"project-id\",\"firewall_name\":\"firewall-name\"}",
        get.serialize().toString());
  }

  @Test
  public void firewallDeleteSerialize() throws Exception {
    CloudComputeCow.Firewalls.Delete delete =
        defaultCompute().firewalls().delete("project-id", "firewall-name");

    assertEquals(
        "{\"project_id\":\"project-id\",\"firewall_name\":\"firewall-name\"}",
        delete.serialize().toString());
  }

  @Test
  public void routeInsertSerialize() throws Exception {
    Route route = new Route().setName("route-name");
    CloudComputeCow.Routes.Insert insert = defaultCompute().routes().insert("project-id", route);

    assertEquals(
        "{\"project_id\":\"project-id\",\"route\":{\"name\":\"route-name\"}}",
        insert.serialize().toString());
  }

  @Test
  public void routeGetSerialize() throws Exception {
    CloudComputeCow.Routes.Get get = defaultCompute().routes().get("project-id", "route-name");

    assertEquals(
        "{\"project_id\":\"project-id\",\"route_name\":\"route-name\"}",
        get.serialize().toString());
  }

  @Test
  public void routerInsertSerialize() throws Exception {
    Router router = new Router().setName("router-name");
    CloudComputeCow.Routers.Insert insert =
        defaultCompute().routers().insert("project-id", "us-west1", router);

    assertEquals(
        "{\"project_id\":\"project-id\",\"region\":\"us-west1\",\"router\":{\"name\":\"router-name\"}}",
        insert.serialize().toString());
  }

  @Test
  public void routerGetSerialize() throws Exception {
    CloudComputeCow.Routers.Get get =
        defaultCompute().routers().get("project-id", "us-west1", "router-name");

    assertEquals(
        "{\"project_id\":\"project-id\",\"region\":\"us-west1\",\"router_name\":\"router-name\"}",
        get.serialize().toString());
  }

  @Test
  public void routerDeleteSerialize() throws Exception {
    CloudComputeCow.Routers.Delete delete =
        defaultCompute().routers().delete("project-id", "us-west1", "router-name");

    assertEquals(
        "{\"project_id\":\"project-id\",\"region\":\"us-west1\",\"router_name\":\"router-name\"}",
        delete.serialize().toString());
  }

  @Test
  public void zoneGetSerialize() throws Exception {
    CloudComputeCow.Zones.Get get = defaultCompute().zones().get("project-id", "us-east1-b");
    assertEquals(
        "{\"project_id\":\"project-id\",\"zone\":\"us-east1-b\"}", get.serialize().toString());
  }

  /** Create Project then set billing account, enable compute compute service */
  private static Project createPreparedProject() throws Exception {
    Project project = ProjectUtils.executeCreateProject();
    CloudBillingUtils.setDefaultProjectBilling(project.getProjectId());
    ServiceUsageUtils.enableServices(project.getProjectId(), ImmutableList.of(COMPUTE_SERVICE_ID));
    return project;
  }

  public static String randomRouterName() {
    // Router name ids must start with a lowercase letter followed by up to 62 lowercase letters,
    // numbers, or hyphens, and cannot end with a hyphen.
    return "r" + IntegrationUtils.randomName();
  }

  public static String randomGatewayName() {
    // Gateway name ids must start with a lowercase letter followed by up to 62 lowercase letters,
    // numbers, or hyphens, and cannot end with a hyphen.
    return "g" + IntegrationUtils.randomName();
  }

  /**
   * Create a string matching the region name on {@link Subnetwork#getRegion()} ()}, e.g.
   * https://www.googleapis.com/compute/v1/projects/p-123/regions/us-west1.
   */
  private static String regionName(String projectId, String region) {
    return String.format(
        "https://www.googleapis.com/compute/v1/projects/%s/regions/%s", projectId, region);
  }

  /**
   * Create a string matching the network URI on {@link Router#getNetwork()}, e.g.
   * https://www.googleapis.com/compute/v1/projects/p-123/global/networks/n-456.
   */
  private static String networkName(final String projectId, final String network) {
    return String.format(
        "https://www.googleapis.com/compute/v1/projects/%s/global/networks/%s", projectId, network);
  }
}
