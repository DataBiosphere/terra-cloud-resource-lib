package bio.terra.cloudres.azure.resourcemanager.compute;

import bio.terra.cloudres.azure.resourcemanager.resources.Defaults;
import bio.terra.cloudres.common.ClientConfig;
import com.azure.core.credential.TokenCredential;
import com.azure.core.management.profile.AzureProfile;
import com.azure.resourcemanager.compute.ComputeManager;

public class ComputeManagerCow {
  private final ClientConfig clientConfig;
  private final ComputeManager computeManager;

  private ComputeManagerCow(ClientConfig clientConfig, ComputeManager computeManager) {
    this.clientConfig = clientConfig;
    this.computeManager = computeManager;
  }

  public static ComputeManagerCow create(
      ClientConfig clientConfig, TokenCredential credential, AzureProfile profile) {
    return new ComputeManagerCow(
        clientConfig,
        ComputeManager.configure()
            .withLogOptions(Defaults.logOptions())
            .authenticate(credential, profile));
  }

  public ComputeManager computeManager() {
    return computeManager;
  }
}
