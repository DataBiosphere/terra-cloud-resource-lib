package bio.terra.cloudres.azure.landingzones.definition.factories;

import bio.terra.cloudres.azure.landingzones.TestArmResourcesFactory;
import bio.terra.cloudres.azure.landingzones.definition.DefinitionHeader;
import bio.terra.cloudres.azure.landingzones.definition.DefinitionVersion;
import bio.terra.cloudres.azure.landingzones.definition.LandingZoneDefinable;
import bio.terra.cloudres.azure.landingzones.deployment.LandingZoneDeployment.DefinitionStages.Deployable;
import bio.terra.cloudres.azure.landingzones.deployment.LandingZoneDeployment.DefinitionStages.WithLandingZoneResource;
import bio.terra.cloudres.azure.landingzones.deployment.ResourcePurpose;
import bio.terra.cloudres.azure.landingzones.deployment.SubnetResourcePurpose;
import com.azure.core.management.Region;
import com.azure.resourcemanager.AzureResourceManager;
import com.azure.resourcemanager.resources.models.ResourceGroup;

import java.util.List;

public class TestLandingZoneFactory implements LandingZoneDefinitionFactory {
    @Override
    public DefinitionHeader header() {
        return new DefinitionHeader("Test LZ", "Description of Test LZ");
    }

    @Override
    public List<DefinitionVersion> availableVersions() {
        return List.of(DefinitionVersion.V1);
    }

    @Override
    public LandingZoneDefinable create(DefinitionVersion version) {
        return new TestLandingZone();
    }

    class TestLandingZone implements LandingZoneDefinable {

        @Override
        public Deployable definition(WithLandingZoneResource deployment, AzureResourceManager azureResourceManager, ResourceGroup resourceGroup) {
            var storage = azureResourceManager.storageAccounts().define(TestArmResourcesFactory.createUniqueAzureResourceName())
                    .withRegion(Region.US_EAST2)
                    .withExistingResourceGroup(resourceGroup);

            var vNet = azureResourceManager.networks().define(TestArmResourcesFactory.createUniqueAzureResourceName())
                    .withRegion(Region.US_EAST2)
                    .withExistingResourceGroup(resourceGroup)
                    .withAddressSpace("10.0.0.0/28")
                    .withSubnet("compute", "10.0.0.0/29")
                    .withSubnet("storage", "10.0.0.8/29");

            return deployment.withResourceWithPurpose(storage, ResourcePurpose.SHARED_RESOURCE)
                    .withVNetWithPurpose(vNet, "compute", SubnetResourcePurpose.WORKSPACE_COMPUTE_SUBNET)
                    .withVNetWithPurpose(vNet, "storage", SubnetResourcePurpose.WORKSPACE_STORAGE_SUBNET);
        }
    }

}
