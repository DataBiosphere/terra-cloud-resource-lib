package bio.terra.cloudres.azure.landingzones.definition;

import com.azure.resourcemanager.AzureResourceManager;
import com.azure.resourcemanager.batch.BatchManager;
import com.azure.resourcemanager.postgresql.PostgreSqlManager;
import com.azure.resourcemanager.relay.RelayManager;

/**
 * Base class for a Landing Zone Definition that contains the ARM clients to define the resource to
 * deploy
 */
public abstract class LandingZoneDefinition implements LandingZoneDefinable {

  protected final AzureResourceManager azureResourceManager;
  protected final RelayManager relayManager;
  protected final BatchManager batchManager;
  protected final PostgreSqlManager postgreSqlManager;

  protected LandingZoneDefinition(
      AzureResourceManager azureResourceManager,
      RelayManager relayManager,
      BatchManager batchManager,
      PostgreSqlManager postgreSqlManager) {
    this.azureResourceManager = azureResourceManager;
    this.relayManager = relayManager;
    this.batchManager = batchManager;
    this.postgreSqlManager = postgreSqlManager;
  }
}
