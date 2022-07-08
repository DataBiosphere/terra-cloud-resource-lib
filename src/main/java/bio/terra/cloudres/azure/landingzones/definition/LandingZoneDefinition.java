package bio.terra.cloudres.azure.landingzones.definition;

import com.azure.resourcemanager.AzureResourceManager;
import com.azure.resourcemanager.relay.RelayManager;

public abstract class LandingZoneDefinition implements LandingZoneDefinable {

    protected final RelayManager relayManager;
    protected final AzureResourceManager azureResourceManager;

    protected LandingZoneDefinition(AzureResourceManager azureResourceManager,
                                    RelayManager relayManager) {
        this.relayManager = relayManager;
        this.azureResourceManager = azureResourceManager;
    }

}
