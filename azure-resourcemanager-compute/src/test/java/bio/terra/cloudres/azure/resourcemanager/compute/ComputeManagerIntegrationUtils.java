package bio.terra.cloudres.azure.resourcemanager.compute;

import bio.terra.cloudres.azure.resourcemanager.resources.AzureIntegrationUtils;
import bio.terra.cloudres.testing.IntegrationUtils;

public class ComputeManagerIntegrationUtils {
  static ComputeManagerCow defaultComputeManagerCow() {
    return ComputeManagerCow.create(
        IntegrationUtils.DEFAULT_CLIENT_CONFIG,
        AzureIntegrationUtils.getAdminAzureCredentialsOrDie(),
        AzureIntegrationUtils.getUserAzureProfileOrDie());
  }

  // TODO: perhaps create this dynamically in the future
  static String getReusableResourceGroup() {
    return "mrg-rtitle-1-previe-20210819100327";
  }
}
