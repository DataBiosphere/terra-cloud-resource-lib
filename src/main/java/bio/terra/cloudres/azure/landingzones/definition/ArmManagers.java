package bio.terra.cloudres.azure.landingzones.definition;

import com.azure.resourcemanager.AzureResourceManager;
import com.azure.resourcemanager.relay.RelayManager;

public record ArmManagers(AzureResourceManager azureResourceManager, RelayManager relayManager) {
}
