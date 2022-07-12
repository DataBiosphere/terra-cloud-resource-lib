package bio.terra.cloudres.azure.landingzones.definition;

import bio.terra.cloudres.azure.landingzones.deployment.LandingZoneDeployment.DefinitionStages.Deployable;

/** Enables the definition of a Landing Zone */
public interface LandingZoneDefinable {
  Deployable definition(DefinitionContext definitionContext);
}
