package bio.terra.cloudres.azure.landingzones.definition;

import com.azure.resourcemanager.AzureResourceManager;
import com.azure.resourcemanager.relay.RelayManager;
/** Record with the ARM clients required for deployments */
public record ArmManagers(AzureResourceManager azureResourceManager, RelayManager relayManager) {
}
