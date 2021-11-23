package bio.terra.cloudres.google.compute.testing;

import bio.terra.cloudres.google.api.services.common.testing.OperationTestUtils;
import bio.terra.cloudres.google.compute.CloudComputeCow;
import bio.terra.cloudres.testing.IntegrationCredentials;
import bio.terra.cloudres.testing.IntegrationUtils;
import com.google.api.services.compute.model.Network;
import com.google.api.services.compute.model.Operation;
import java.time.Duration;

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

  public static String randomNetworkName() {
    // Network name ids must start with a letter and be no more than 30 characters long.
    return "n" + IntegrationUtils.randomName().substring(0, 29);
  }
}
