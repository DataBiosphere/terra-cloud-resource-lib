package bio.terra.cloudres.azure.landingzones.definition.factories;

import bio.terra.cloudres.azure.landingzones.definition.ArmManagers;
import com.azure.core.util.logging.ClientLogger;
import com.azure.resourcemanager.AzureResourceManager;
import com.azure.resourcemanager.batch.BatchManager;
import com.azure.resourcemanager.postgresql.PostgreSqlManager;
import com.azure.resourcemanager.relay.RelayManager;

/** Implementation of {@link LandingZoneDefinitionProvider} */
public class LandingZoneDefinitionProviderImpl implements LandingZoneDefinitionProvider {

  private final ClientLogger logger = new ClientLogger(LandingZoneDefinitionProviderImpl.class);
  private final ArmManagers armManagers;

  public LandingZoneDefinitionProviderImpl(ArmManagers armManagers) {
    this.armManagers = armManagers;
  }

  @Override
  public <T extends LandingZoneDefinitionFactory>
      LandingZoneDefinitionFactory createDefinitionFactory(Class<T> factory) {
    return createNewFactoryInstance(factory);
  }

  private <T extends LandingZoneDefinitionFactory> T createNewFactoryInstance(
      Class<T> factoryClass) {
    try {
      return factoryClass
          .getDeclaredConstructor(
              AzureResourceManager.class,
              RelayManager.class,
              BatchManager.class,
              PostgreSqlManager.class)
          .newInstance(
              armManagers.azureResourceManager(),
              armManagers.relayManager(),
              armManagers.batchManager(),
              armManagers.postgreSqlManager());
    } catch (Exception e) {
      throw logger.logExceptionAsError(new RuntimeException(e));
    }
    //       BatchManager batchManager,
    //      PostgreSqlManager postgreSqlManager
  }
}
