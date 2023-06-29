package bio.terra.cloudres.google.compute.testing;

import bio.terra.cloudres.google.api.services.common.testing.OperationTestUtils;
import bio.terra.cloudres.google.compute.CloudComputeCow;
import bio.terra.cloudres.testing.IntegrationCredentials;
import bio.terra.cloudres.testing.IntegrationUtils;
import com.google.api.services.compute.model.Firewall;
import com.google.api.services.compute.model.Firewall.Allowed;
import com.google.api.services.compute.model.Network;
import com.google.api.services.compute.model.Operation;
import java.time.Duration;
import java.util.List;

/** Testing utilities for networks. */
public class NetworkUtils {
  private static CloudComputeCow cloudComputeCow;

  public static CloudComputeCow getCloudComputeCow() throws Exception {
    if (cloudComputeCow == null) {
      cloudComputeCow =
          CloudComputeCow.create(
              IntegrationUtils.DEFAULT_CLIENT_CONFIG,
              IntegrationCredentials.getAdminGoogleCredentialsOrDie());
    }
    return cloudComputeCow;
  }

  /** Creates a new Network in a given GCP project for testing. */
  public static Network exceuteCreateNetwork(String projectId, boolean autoCreateSubnetworks)
      throws Exception {
    String netWorkName = randomNetworkName();
    Network network =
        new Network().setName(netWorkName).setAutoCreateSubnetworks(autoCreateSubnetworks);
    Operation networkOperation =
        getCloudComputeCow().networks().insert(projectId, network).execute();
    OperationTestUtils.pollAndAssertSuccess(
        getCloudComputeCow().globalOperations().operationCow(projectId, networkOperation),
        Duration.ofSeconds(5),
        Duration.ofSeconds(100));
    return getCloudComputeCow().networks().get(projectId, netWorkName).execute();
  }

  /**
   * Creates dataproc ingress firewall rule in a given network for Google Cloud Dataproc. See
   * https://cloud.google.com/dataproc/docs/concepts/configuring-clusters/network#firewall_rule_requirement.
   */
  public static void createDataprocIngressRule(String projectId, String networkName)
      throws Exception {
    String firewallName = "allow-dataproc-ingress";
    String networkUrl =
        String.format(
            "https://www.googleapis.com/compute/v1/projects/%s/global/networks/%s",
            projectId, networkName);

    Firewall firewall =
        new Firewall()
            .setName(firewallName)
            .setNetwork(networkUrl)
            .setDirection("INGRESS")
            .setSourceRanges(
                List.of("10.128.0.0/9")) // Source range for internal dataproc VPC traffic
            .setAllowed(List.of(new Allowed().setIPProtocol("all"))); // Allow all protocols

    Operation firewallOperation =
        getCloudComputeCow().firewalls().insert(projectId, firewall).execute();
    OperationTestUtils.pollAndAssertSuccess(
        getCloudComputeCow().globalOperations().operationCow(projectId, firewallOperation),
        Duration.ofSeconds(5),
        Duration.ofSeconds(100));
  }

  public static String randomNetworkName() {
    // Network name ids must start with a letter and be no more than 30 characters long.
    return "n" + IntegrationUtils.randomName().substring(0, 29);
  }
}
