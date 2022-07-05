package bio.terra.cloudres.azure.landingzones.deployment;

import com.azure.resourcemanager.network.models.Network;
import com.azure.resourcemanager.relay.models.RelayNamespace;
import com.azure.resourcemanager.relay.models.RelayNamespace.DefinitionStages.WithCreate;
import com.azure.resourcemanager.resources.fluentcore.arm.models.Resource;
import com.azure.resourcemanager.resources.fluentcore.arm.models.Resource.DefinitionWithTags;
import com.azure.resourcemanager.resources.fluentcore.model.Creatable;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class LandingZoneDeploymentImpl implements
        LandingZoneDeployment,
        LandingZoneDeployment.FluentDefinition {

    private final ResourcesTagMapWrapper resourcesTagMapWrapper;

    protected LandingZoneDeploymentImpl(ResourcesTagMapWrapper resourcesTagMapWrapper) {
        this.resourcesTagMapWrapper = resourcesTagMapWrapper;
    }

    @Override
    public List<DeployedResource> deploy() {

        return Stream.concat(deployResources().stream(),
                deployRelayResources().stream()).collect(Collectors.toList());
    }

    private List<DeployedResource> deployRelayResources(){
        Map<WithCreate,Map<String,String>> resourcesTagsMap = resourcesTagMapWrapper.getRelayResourcesTagsMap();
        return resourcesTagsMap
                .entrySet()
                .stream()
                .map(this::deployRelayResource)
                .collect(Collectors.toList());

    }

    private List<DeployedResource> deployResources(){
        Map<Creatable<?>,Map<String,String>> resourcesTagsMap = resourcesTagMapWrapper.getResourcesTagsMap();

        return resourcesTagsMap
                .entrySet()
                .stream()
                .map(this::deployResource)
                .collect(Collectors.toList());
    }

    private DeployedResource deployResource(Map.Entry<Creatable<?>,Map<String,String>> resourceEntry) {

        //if not null, this means the resource is expected to be tagged.
        if (resourceEntry.getValue() != null) {
            //this cast should be safe as all put methods that set tags require an implementation of DefinitionWithTags
            DefinitionWithTags<?> resourceWithTags = (DefinitionWithTags<?>) resourceEntry.getKey();
            resourceWithTags.withTags(resourceEntry.getValue());
            return deployCreatableResource((Creatable<?>) resourceWithTags);
        }

        return deployCreatableResource(resourceEntry.getKey());
    }
    private DeployedResource deployRelayResource(Map.Entry<WithCreate,Map<String,String>> resourceEntry) {

        WithCreate relayResource = resourceEntry.getKey();
        if (resourceEntry.getValue() != null) {
            relayResource.withTags(resourceEntry.getValue());
        }

        RelayNamespace namespace = relayResource.create();

        return  new DeployedResource(
                namespace.id(),
                namespace.type(),
                namespace.tags(),
                namespace.regionName()
        );
    }

    private DeployedResource deployCreatableResource(Creatable<?> resource){
        Resource result = (Resource) resource.create();

        return new DeployedResource(
                result.id(),
                result.type(),
                result.tags(),
                result.regionName());
    }


    @Override
    public <T extends Creatable<?> & DefinitionWithTags<?>> DefinitionStages.Deployable withResourceWithPurpose(T resource, ResourcePurpose purpose) {
        resourcesTagMapWrapper.putWithPurpose(resource, purpose);
        return this;
    }

    @Override
    public <T extends Creatable<?>> DefinitionStages.Deployable withResource(T resource) {
        resourcesTagMapWrapper.putResource(resource);
        return this;
    }

    @Override
    public DefinitionStages.Deployable withVNetWithPurpose(Network.DefinitionStages.WithCreateAndSubnet virtualNetwork, String subnetName, SubnetResourcePurpose purpose) {
        resourcesTagMapWrapper.putWithVNetWithPurpose(virtualNetwork, subnetName, purpose);
        return this;
    }

    @Override
    public DefinitionStages.Deployable withResourceWithPurpose(WithCreate relay, ResourcePurpose purpose) {
        resourcesTagMapWrapper.putWithPurpose(relay, purpose);
        return this;
    }
}
