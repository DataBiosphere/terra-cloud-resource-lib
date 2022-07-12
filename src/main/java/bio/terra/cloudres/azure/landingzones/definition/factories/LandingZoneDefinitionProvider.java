package bio.terra.cloudres.azure.landingzones.definition.factories;

import bio.terra.cloudres.azure.landingzones.definition.FactoryInfo;
import java.util.Set;

/** High-level API for listing and creating Landing Zones Definition factories. */
public interface LandingZoneDefinitionProvider {
  Set<FactoryInfo> factories();

  <T extends LandingZoneDefinitionFactory> LandingZoneDefinitionFactory createDefinitionFactory(
      Class<T> factory);
}
