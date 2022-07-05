package bio.terra.cloudres.azure.landingzones.definition.factories;

import bio.terra.cloudres.azure.landingzones.definition.DefinitionHeader;
import bio.terra.cloudres.azure.landingzones.definition.DefinitionVersion;
import bio.terra.cloudres.azure.landingzones.definition.LandingZoneDefinable;
import bio.terra.cloudres.azure.landingzones.deployment.LandingZoneDeployment.DefinitionStages.Deployable;
import bio.terra.cloudres.azure.landingzones.deployment.LandingZoneDeployment.DefinitionStages.WithLandingZoneResource;
import bio.terra.cloudres.azure.landingzones.deployment.ResourcePurpose;
import bio.terra.cloudres.azure.landingzones.deployment.ResourceUtils;
import bio.terra.cloudres.azure.landingzones.deployment.SubnetResourcePurpose;
import com.azure.core.credential.TokenCredential;
import com.azure.core.management.profile.AzureProfile;
import com.azure.identity.DefaultAzureCredentialBuilder;
import com.azure.resourcemanager.AzureResourceManager;
import com.azure.resourcemanager.containerservice.models.AgentPoolMode;
import com.azure.resourcemanager.containerservice.models.ContainerServiceVMSizeTypes;
import com.azure.resourcemanager.relay.RelayManager;
import com.azure.resourcemanager.resources.ResourceManager;
import com.azure.resourcemanager.resources.models.ResourceGroup;

import java.util.List;

public class ManagedNetworkWithSharedResourcesFactory implements LandingZoneDefinitionFactory {
    private final String LZ_NAME = "Managed Network with Shared Resources";
    private final String LZ_DESC = "Managed VNet with shared storage and relay namespace ";
    @Override
    public DefinitionHeader header() {
        return new DefinitionHeader(LZ_NAME, LZ_DESC);
    }

    @Override
    public List<DefinitionVersion> availableVersions() {
        return List.of(DefinitionVersion.V1);
    }

    @Override
    public LandingZoneDefinable create(DefinitionVersion version) {
        if (version.equals(DefinitionVersion.V1)){
            return new DefinitionV1();
        }
        throw new RuntimeException("Invalid Version");
    }


    class DefinitionV1 implements LandingZoneDefinable{

        private RelayManager relayManager;

        @Override
        public Deployable definition(WithLandingZoneResource deployment, AzureResourceManager arm, ResourceGroup resourceGroup) {
            var storage = arm.storageAccounts()
                    .define(ResourceUtils.createUniqueAzureResourceName())
                    .withRegion(resourceGroup.region())
                    .withExistingResourceGroup(resourceGroup);

            var vNet = arm.networks()
                    .define(ResourceUtils.createUniqueAzureResourceName())
                    .withRegion(resourceGroup.region())
                    .withExistingResourceGroup(resourceGroup)
                    .withAddressSpace("10.0.0.0/28")
                    .withSubnet("compute", "10.0.0.0/29");

            var relay = relayManager(arm).namespaces()
                    .define(ResourceUtils.createUniqueAzureResourceName(15))
                    .withRegion(resourceGroup.region())
                    .withExistingResourceGroup(resourceGroup.name());

            var aks = arm.kubernetesClusters()
                    .define(ResourceUtils.createUniqueAzureResourceName())
                    .withRegion(resourceGroup.region())
                    .withExistingResourceGroup(resourceGroup)
                    .withDefaultVersion()
                    .withSystemAssignedManagedServiceIdentity()
                    .defineAgentPool(ResourceUtils.createUniqueAzureResourceName(10))
                    .withVirtualMachineSize(ContainerServiceVMSizeTypes.STANDARD_A2_V2)
                    .withAgentPoolVirtualMachineCount(1)
                    .withAgentPoolMode(AgentPoolMode.SYSTEM)
                    .attach()
                    .withDnsPrefix(ResourceUtils.createUniqueAzureResourceName());


            return deployment
                    .withResourceWithPurpose(storage, ResourcePurpose.SHARED_RESOURCE)
                    .withVNetWithPurpose(vNet, "compute", SubnetResourcePurpose.WORKSPACE_COMPUTE_SUBNET)
                    .withResourceWithPurpose(relay, ResourcePurpose.SHARED_RESOURCE)
                    .withResourceWithPurpose(aks, ResourcePurpose.SHARED_RESOURCE);

        }

        private RelayManager relayManager(AzureResourceManager arm){

            ResourceManager manager = arm.genericResources().manager();
            AzureProfile profile = new AzureProfile(arm.tenantId(),manager.subscriptionId(),manager.environment());
            TokenCredential credential = new DefaultAzureCredentialBuilder()
                    .authorityHost(profile.getEnvironment().getActiveDirectoryEndpoint())
                    .build();
            return RelayManager
                    .authenticate(credential, profile);
        }
    }
}
