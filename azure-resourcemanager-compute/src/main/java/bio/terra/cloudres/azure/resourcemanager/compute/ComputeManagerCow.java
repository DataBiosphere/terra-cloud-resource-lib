package bio.terra.cloudres.azure.resourcemanager.compute;

import bio.terra.cloudres.azure.resourcemanager.common.ApplicationSecretCredentials;
import bio.terra.cloudres.common.ClientConfig;
import bio.terra.cloudres.common.OperationAnnotator;
import com.azure.core.management.profile.AzureProfile;
import com.azure.resourcemanager.compute.ComputeManager;
import com.google.common.annotations.VisibleForTesting;
import com.google.gson.JsonObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * A Cloud Object Wrapper(COW) for Azure Compute Manager client library: {@link ComputeManager}
 *
 * <p>TODO: integrate with Janitor
 */
public class ComputeManagerCow {
  private final Logger logger = LoggerFactory.getLogger(ComputeManagerCow.class);

  private final ClientConfig clientConfig;
  private final ComputeManager client;
  private final OperationAnnotator operationAnnotator;

  private ComputeManagerCow(ClientConfig clientConfig, ComputeManager client) {
    this.clientConfig = clientConfig;
    this.client = client;
    this.operationAnnotator = new OperationAnnotator(clientConfig, logger);
  }

  /**
   * Creates a ComputeManagerCow.
   *
   * @param clientConfig CRL configuration
   * @param profile Azure profile
   * @param credentials Azure application credentials
   * @return ComputeManagerCow
   */
  public static ComputeManagerCow create(
      ClientConfig clientConfig, AzureProfile profile, ApplicationSecretCredentials credentials) {
    return new ComputeManagerCow(
        clientConfig, ComputeManager.authenticate(credentials.getTokenCredential(), profile));
  }

  public void deleteVirtualMachine(String resourceGroup, String vmName) {
    operationAnnotator.executeCowOperation(
        ComputeManagerOperation.AZURE_RESOURCE_MANAGER_DELETE_VIRTUAL_MACHINE,
        () -> {
          client.virtualMachines().deleteByResourceGroup(resourceGroup, vmName);
          return null;
        },
        () -> serializeDeployment(resourceGroup, vmName));
  }

  // TODO add other compute methods as needed

  @VisibleForTesting
  static JsonObject serializeDeployment(String resourceGroup, String vmName) {
    JsonObject result = new JsonObject();
    result.addProperty("resourceGroup", resourceGroup);
    result.addProperty("vmName", vmName);
    return result;
  }
}
