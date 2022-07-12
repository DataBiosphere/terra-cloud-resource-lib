package bio.terra.cloudres.azure.landingzones.deployment;

import bio.terra.cloudres.azure.landingzones.deployment.LandingZoneDeployment.DefinitionStages.Deployable;
import bio.terra.cloudres.azure.landingzones.deployment.LandingZoneDeployment.DefinitionStages.WithLandingZoneResource;
import com.azure.resourcemanager.network.models.Network;
import com.azure.resourcemanager.relay.models.RelayNamespace.DefinitionStages.WithCreate;
import com.azure.resourcemanager.resources.fluentcore.arm.models.Resource;
import com.azure.resourcemanager.resources.fluentcore.arm.models.Resource.DefinitionWithTags;
import com.azure.resourcemanager.resources.fluentcore.model.Creatable;
import java.util.List;
import java.util.Map;
import org.reactivestreams.Publisher;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

/** Implementation of a landing zone deployment */
public class LandingZoneDeploymentImpl
    implements LandingZoneDeployment, LandingZoneDeployment.FluentDefinition {

  private final ResourcesTagMapWrapper resourcesTagMapWrapper;

  protected LandingZoneDeploymentImpl(ResourcesTagMapWrapper resourcesTagMapWrapper) {
    this.resourcesTagMapWrapper = resourcesTagMapWrapper;
  }

  @Override
  public List<DeployedResource> deploy() {

    return deployAsync().collectList().block();
  }

  @Override
  public Flux<DeployedResource> deployAsync() {
    return Flux.merge(deployResourcesAsync(), deployRelayResourcesAsync());
  }

  private Flux<DeployedResource> deployRelayResourcesAsync() {
    Map<WithCreate, Map<String, String>> resourcesTagsMap =
        resourcesTagMapWrapper.getRelayResourcesTagsMap();
    return Flux.fromIterable(resourcesTagsMap.entrySet()).flatMap(this::deployRelayResourceAsync);
  }

  private Publisher<? extends DeployedResource> deployRelayResourceAsync(
      Map.Entry<WithCreate, Map<String, String>> resourceEntry) {

    WithCreate relayResource = resourceEntry.getKey();
    if (resourceEntry.getValue() != null) {
      relayResource.withTags(resourceEntry.getValue());
    }

    return Mono.just(relayResource.create())
        .map(n -> new DeployedResource(n.id(), n.type(), n.tags(), n.regionName()));
  }

  private Flux<DeployedResource> deployResourcesAsync() {
    Map<Creatable<?>, Map<String, String>> resourcesTagsMap =
        resourcesTagMapWrapper.getResourcesTagsMap();

    return Flux.fromIterable(resourcesTagsMap.entrySet())
        .flatMap(r -> deployResourceAsync(r.getKey(), r.getValue()));
  }

  private Publisher<? extends DeployedResource> deployResourceAsync(
      Creatable<?> resource, Map<String, String> tags) {

    // if not null, this means the resource is expected to be tagged.
    if (tags != null) {
      // this cast should be safe as all put methods that set tags require an implementation of
      // DefinitionWithTags
      DefinitionWithTags<?> resourceWithTags = (DefinitionWithTags<?>) resource;
      resourceWithTags.withTags(tags);
      return deployCreatableResourceAsync((Creatable<?>) resourceWithTags);
    }

    return deployCreatableResourceAsync(resource);
  }

  private Mono<DeployedResource> deployCreatableResourceAsync(Creatable<?> resource) {
    return resource
        .createAsync()
        .map(
            r -> {
              Resource result = (Resource) r;
              return new DeployedResource(
                  result.id(), result.type(), result.tags(), result.regionName());
            });
  }

  @Override
  public <T extends Creatable<?> & DefinitionWithTags<?>> Deployable withResourceWithPurpose(
      T resource, ResourcePurpose purpose) {
    resourcesTagMapWrapper.putWithPurpose(resource, purpose);
    return this;
  }

  @Override
  public <T extends Creatable<?>> Deployable withResource(T resource) {
    resourcesTagMapWrapper.putResource(resource);
    return this;
  }

  @Override
  public Deployable withVNetWithPurpose(
      Network.DefinitionStages.WithCreateAndSubnet virtualNetwork,
      String subnetName,
      SubnetResourcePurpose purpose) {
    resourcesTagMapWrapper.putWithVNetWithPurpose(virtualNetwork, subnetName, purpose);
    return this;
  }

  @Override
  public Deployable withResourceWithPurpose(WithCreate relay, ResourcePurpose purpose) {
    resourcesTagMapWrapper.putWithPurpose(relay, purpose);
    return this;
  }

  @Override
  public WithLandingZoneResource definePrerequisites() {
    return new LandingZoneDeploymentImpl(
        new ResourcesTagMapWrapper(resourcesTagMapWrapper.getLandingZoneId()));
  }
}
