package bio.terra.cloudres.azure.landingzones.definition.factories;

import bio.terra.cloudres.azure.landingzones.definition.FactoryDefinitionInfo;
import java.util.List;

public interface LandingZoneDefinitionFactoryListProvider {
  public List<FactoryDefinitionInfo> listFactories();

  public List<Class<? extends LandingZoneDefinitionFactory>> listFactoriesClasses();
}
