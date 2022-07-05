package bio.terra.cloudres.azure.landingzones.definition.factories;

import bio.terra.cloudres.azure.landingzones.definition.FactoryInfo;

import java.util.Set;

public interface LandingZoneDefinitionProvider {
    Set<FactoryInfo> factories();
    <T extends LandingZoneDefinitionFactory> LandingZoneDefinitionFactory createDefinitionFactory(Class<T> factory);
}
