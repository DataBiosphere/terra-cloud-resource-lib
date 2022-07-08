package bio.terra.cloudres.azure.landingzones.definition.factories;

import com.azure.resourcemanager.AzureResourceManager;
import com.azure.resourcemanager.relay.RelayManager;

public abstract class ArmClientsDefinitionFactory implements LandingZoneDefinitionFactory {
    protected final AzureResourceManager azureResourceManager;
    protected final RelayManager relayManager;

    protected ArmClientsDefinitionFactory(AzureResourceManager azureResourceManager, RelayManager relayManager) {
        this.azureResourceManager = azureResourceManager;
        this.relayManager = relayManager;
    }
}
