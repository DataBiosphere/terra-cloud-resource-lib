package bio.terra.cloudres.azure.landingzones.deployment;

import com.azure.resourcemanager.network.models.Network;
import com.azure.resourcemanager.relay.models.RelayNamespace;
import com.azure.resourcemanager.resources.fluentcore.arm.models.Resource.DefinitionWithTags;
import com.azure.resourcemanager.resources.fluentcore.model.Creatable;
import java.util.List;
import reactor.core.publisher.Flux;

/** Fluent API to define resource purpose and deployment in a landing zone. */
public interface LandingZoneDeployment {

  interface FluentDefinition
      extends DefinitionStages.WithLandingZoneResource, DefinitionStages.Deployable {}

  interface DefinitionStages {
    interface Definable {
      WithLandingZoneResource define(String landingZoneId);
    }

    interface WithLandingZoneResource {
      <T extends Creatable<?> & DefinitionWithTags<?>> Deployable withResourceWithPurpose(
          T resource, ResourcePurpose purpose);

      <T extends Creatable<?>> Deployable withResource(T resource);

      Deployable withVNetWithPurpose(
          Network.DefinitionStages.WithCreateAndSubnet virtualNetwork,
          String subnetName,
          SubnetResourcePurpose purpose);

      Deployable withResourceWithPurpose(
          RelayNamespace.DefinitionStages.WithCreate relay, ResourcePurpose sharedResource);

      WithLandingZoneResource definePrerequisites();
    }

    interface Deployable extends WithLandingZoneResource {
      List<DeployedResource> deploy();

      Flux<DeployedResource> deployAsync();
    }
  }
}
