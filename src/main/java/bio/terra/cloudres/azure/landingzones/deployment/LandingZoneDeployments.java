package bio.terra.cloudres.azure.landingzones.deployment;

import bio.terra.cloudres.azure.landingzones.deployment.LandingZoneDeployment.DefinitionStages.WithLandingZoneResource;

public interface LandingZoneDeployments {
    WithLandingZoneResource define(String landingZoneId);
}
