package bio.terra.cloudres.azure.landingzones.definition.factories;

import bio.terra.cloudres.azure.landingzones.definition.DefinitionHeader;
import bio.terra.cloudres.azure.landingzones.definition.DefinitionVersion;
import bio.terra.cloudres.azure.landingzones.definition.LandingZoneDefinable;

import java.util.List;

public interface LandingZoneDefinitionFactory {
    DefinitionHeader header();
    List<DefinitionVersion> availableVersions();
    LandingZoneDefinable create(DefinitionVersion version);
}
