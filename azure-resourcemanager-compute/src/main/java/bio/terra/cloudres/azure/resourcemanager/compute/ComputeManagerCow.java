package bio.terra.cloudres.azure.resourcemanager.compute;

import bio.terra.cloudres.azure.resourcemanager.resources.Defaults;
import bio.terra.cloudres.common.ClientConfig;
import com.azure.core.credential.TokenCredential;
import com.azure.core.management.profile.AzureProfile;
import com.azure.resourcemanager.compute.ComputeManager;

/** A Cloud Object Wrapper (COW) for Azure {@link ComputeManager} library. */
public class ComputeManagerCow {
  private final ClientConfig clientConfig;
  private final ComputeManager computeManager;

  private ComputeManagerCow(ClientConfig clientConfig, ComputeManager computeManager) {
    this.clientConfig = clientConfig;
    this.computeManager = computeManager;
  }

  /**
   * Creates a {@link ComputeManagerCow}.
   *
   * @param clientConfig client configuration
   * @param credential Azure credential
   * @param profile Azure profile for the client
   * @return {@link ComputeManagerCow} instance.
   */
  public static ComputeManagerCow create(
      ClientConfig clientConfig, TokenCredential credential, AzureProfile profile) {
    return new ComputeManagerCow(
        clientConfig,
        ComputeManager.configure()
            .withLogOptions(Defaults.logOptions(clientConfig))
            .authenticate(credential, profile));
  }

  /** Returns the underlying {@link ComputeManager} object for making API calls. */
  public ComputeManager computeManager() {
    return computeManager;
  }
}
