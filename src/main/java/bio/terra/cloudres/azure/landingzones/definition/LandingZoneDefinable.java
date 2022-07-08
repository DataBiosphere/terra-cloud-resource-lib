package bio.terra.cloudres.azure.landingzones.definition;

import bio.terra.cloudres.azure.landingzones.deployment.LandingZoneDeployment.DefinitionStages.Deployable;
import bio.terra.cloudres.azure.landingzones.deployment.LandingZoneDeployment.DefinitionStages.WithLandingZoneResource;
import com.azure.resourcemanager.resources.models.ResourceGroup;

public interface LandingZoneDefinable {
    Deployable definition(WithLandingZoneResource deployment, ResourceGroup resourceGroup);
}
