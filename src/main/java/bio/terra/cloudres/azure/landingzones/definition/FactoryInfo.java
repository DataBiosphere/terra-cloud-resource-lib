package bio.terra.cloudres.azure.landingzones.definition;

import bio.terra.cloudres.azure.landingzones.definition.factories.LandingZoneDefinitionFactory;

import java.util.List;

public record FactoryInfo(Class<? extends LandingZoneDefinitionFactory> factoryClass,
                          List<DefinitionVersion> versions) {
}
