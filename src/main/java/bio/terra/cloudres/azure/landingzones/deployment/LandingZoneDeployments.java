package bio.terra.cloudres.azure.landingzones.deployment;

import bio.terra.cloudres.azure.landingzones.deployment.LandingZoneDeployment.DefinitionStages.WithLandingZoneResource;

/** Factory entry point for the creation deployments with a fluent interface. */
public interface LandingZoneDeployments {
  WithLandingZoneResource define(String landingZoneId);
}
