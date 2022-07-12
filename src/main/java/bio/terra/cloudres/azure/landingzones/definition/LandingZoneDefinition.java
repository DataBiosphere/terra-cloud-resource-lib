package bio.terra.cloudres.azure.landingzones.definition;

import com.azure.resourcemanager.AzureResourceManager;
import com.azure.resourcemanager.relay.RelayManager;

/**
 * Base class for a Landing Zone Definition that contains the ARM clients to define the resource to
 * deploy
 */
public abstract class LandingZoneDefinition implements LandingZoneDefinable {

  protected final RelayManager relayManager;
  protected final AzureResourceManager azureResourceManager;

  protected LandingZoneDefinition(
      AzureResourceManager azureResourceManager, RelayManager relayManager) {
    this.relayManager = relayManager;
    this.azureResourceManager = azureResourceManager;
  }
}
