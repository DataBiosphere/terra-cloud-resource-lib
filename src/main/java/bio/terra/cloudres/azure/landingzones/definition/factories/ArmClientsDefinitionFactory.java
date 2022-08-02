package bio.terra.cloudres.azure.landingzones.definition.factories;

import com.azure.resourcemanager.AzureResourceManager;
import com.azure.resourcemanager.batch.BatchManager;
import com.azure.resourcemanager.postgresql.PostgreSqlManager;
import com.azure.resourcemanager.relay.RelayManager;

/** Base class that provides Arm clients for Landing Zone Definition factories. */
public abstract class ArmClientsDefinitionFactory implements LandingZoneDefinitionFactory {
  protected final AzureResourceManager azureResourceManager;
  protected final RelayManager relayManager;
  protected final BatchManager batchManager;
  protected final PostgreSqlManager postgreSqlManager;

  protected ArmClientsDefinitionFactory(
      AzureResourceManager azureResourceManager,
      RelayManager relayManager,
      BatchManager batchManager,
      PostgreSqlManager postgreSqlManager) {
    this.azureResourceManager = azureResourceManager;
    this.relayManager = relayManager;
    this.batchManager = batchManager;
    this.postgreSqlManager = postgreSqlManager;
  }

  protected ArmClientsDefinitionFactory() {
    this.azureResourceManager = null;
    this.relayManager = null;
    this.batchManager = null;
    this.postgreSqlManager = null;
  }
}
