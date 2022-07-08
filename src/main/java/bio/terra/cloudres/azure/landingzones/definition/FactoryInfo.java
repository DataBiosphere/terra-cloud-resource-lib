package bio.terra.cloudres.azure.landingzones.definition;

import bio.terra.cloudres.azure.landingzones.definition.factories.LandingZoneDefinitionFactory;

import java.util.List;
/** Contains the class instance and the list of available versions of
 * a {@link bio.terra.cloudres.azure.landingzones.definition.factories.LandingZoneDefinitionFactory} */
public record FactoryInfo(Class<? extends LandingZoneDefinitionFactory> factoryClass,
                          List<DefinitionVersion> versions) {
}
