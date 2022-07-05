package bio.terra.cloudres.azure.landingzones.definition;

import bio.terra.cloudres.azure.landingzones.deployment.LandingZoneDeployment;
import bio.terra.cloudres.azure.landingzones.deployment.LandingZoneDeployment.DefinitionStages.WithLandingZoneResource;
import com.azure.resourcemanager.AzureResourceManager;
import com.azure.resourcemanager.resources.models.ResourceGroup;

public interface LandingZoneDefinable {
    LandingZoneDeployment.DefinitionStages.Deployable definition(WithLandingZoneResource deployment, AzureResourceManager azureResourceManager, ResourceGroup resourceGroup);
}
