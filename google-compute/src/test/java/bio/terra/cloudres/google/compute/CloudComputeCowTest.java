package bio.terra.cloudres.google.compute;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.*;

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
  public void createAndGetAndListSubnetwork() throws Exception {
    String projectId = reusableProject.getProjectId();
    String region = "us-west1";
    String ipCidrRange = "10.130.0.0/20";
    CloudComputeCow cloudComputeCow = defaultCompute();

    // Create parent network
    String netWorkName = randomNetworkName();
    Network network = new Network().setName(netWorkName).setAutoCreateSubnetworks(false);
    Operation operation = cloudComputeCow.networks().insert(projectId, network).execute();
    OperationTestUtils.pollAndAssertSuccess(
        cloudComputeCow.globalOperations().operationCow(projectId, operation),
        Duration.ofSeconds(5),
        Duration.ofSeconds(100));
    network = cloudComputeCow.networks().get(projectId, netWorkName).execute();

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
  }

  @Test
  public void createGetAndDeleteFirewall() throws Exception {
    String projectId = reusableProject.getProjectId();

    CloudComputeCow cloudComputeCow = defaultCompute();

    String firewallName = "allow-internal";
    Firewall.Allowed allowed = new Firewall.Allowed().setIPProtocol("icmp");
    Firewall firewall = new Firewall().setName(firewallName).setAllowed(ImmutableList.of(allowed));
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

    String routeName = "private-google-access-route";
    String destRange = "199.36.153.4/30";
    String nextHopGateway = "projects/" + projectId + "/global/gateways/default-internet-gateway";
    Route route =
        new Route().setName(routeName).setDestRange(destRange).setNextHopGateway(nextHopGateway);
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
  public void getZone() throws Exception {
    Zone zone =
        defaultCompute().zones().get(reusableProject.getProjectId(), "us-east1-b").execute();
    assertThat(zone.getRegion(), Matchers.containsString("us-east1"));
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

  public static String randomNetworkName() {
    // Network name ids must start with a letter and be no more than 30 characters long.
    return "n" + IntegrationUtils.randomName().substring(0, 29);
  }

  /**
   * Create a string matching the region name on {@link Subnetwork#getRegion()} ()}, e.g.
   * https://www.googleapis.com/compute/v1/projects/p-123/regions/us-west1.
   */
  private static String regionName(String projectId, String region) {
    return String.format(
        "https://www.googleapis.com/compute/v1/projects/%s/regions/%s", projectId, region);
  }
}
