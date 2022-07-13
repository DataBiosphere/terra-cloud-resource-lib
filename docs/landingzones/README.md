# Landing Zones

## Overview

A Landing Zone is a set of cloud resources that serve as the underlying infrastructure where workspaces or other Terra
applications can
be deployed. The resources in a Landing Zone define and implement constraints, provide cross-cutting features, or can be
shared. These resources have a different lifecycle than resources in workspaces.

## Implementing a Landing Zone

### Landing Zone Definition Factories and Landing Zone Definitions.

Landing zones are implemented using the factory pattern; the factory creates *Landing Zone Definitions* (LZDs).

Landing Zone Definitions are where resources and their purpose are defined.

A Landing Zone Definition factory is an implementation of:

```java
public interface LandingZoneDefinitionFactory {
    DefinitionHeader header();

    List<DefinitionVersion> availableVersions();

    LandingZoneDefinable create(DefinitionVersion version);
}
```

The library includes an abstract class that expects the Azure Resource Manager (ARM)
clients: `ArmClientsDefinitionFactory`. Factories should extend this class and must be implemented in `factories`
package.

```java
package bio.terra.cloudres.azure.landingzones.definition.factories;

public class FooLZFactory extends ArmClientsDefinitionFactory {

    @Override
    public DefinitionHeader header() {
        return new DefinitionHeader("Foo LZ", "Description of Foo LZ");
    }

    @Override
    public List<DefinitionVersion> availableVersions() {
        return List.of(DefinitionVersion.V1);
    }

    @Override
    public LandingZoneDefinable create(DefinitionVersion version) {
        if (version.equals(DefinitionVersion.V1)) {
            return new FooLZDefinitionV1(azureResourceManager, relayManager);
        }
        throw new RuntimeException("Invalid Version");
    }
}
```

An inner class in the factory class is a good convention for implementing a Landing Zone Definition.

```java
public class FooLZFactory extends ArmClientsDefinitionFactory {
    ...

    class FooLZDefinitionV1 extends LandingZoneDefinition {

        protected FooLZDefinitionV1(
                AzureResourceManager azureResourceManager, RelayManager relayManager) {
            super(azureResourceManager, relayManager);
        }

        @Override
        public Deployable definition(DefinitionContext definitionContext) {
            var storage =
                    azureResourceManager
                            .storageAccounts()
                            .define(definitionContext.resourceNameGenerator().nextName(20))
                            .withRegion(Region.US_EAST2)
                            .withExistingResourceGroup(definitionContext.resourceGroup());

            var vNet =
                    azureResourceManager
                            .networks()
                            .define(definitionContext.resourceNameGenerator().nextName(20))
                            .withRegion(Region.US_EAST2)
                            .withExistingResourceGroup(definitionContext.resourceGroup())
                            .withAddressSpace("10.0.0.0/28")
                            .withSubnet("compute", "10.0.0.0/29")
                            .withSubnet("storage", "10.0.0.8/29");

            return definitionContext
                    .deployment()
                    .withResourceWithPurpose(storage, ResourcePurpose.SHARED_RESOURCE)
                    .withVNetWithPurpose(vNet, "compute", SubnetResourcePurpose.WORKSPACE_COMPUTE_SUBNET)
                    .withVNetWithPurpose(vNet, "storage", SubnetResourcePurpose.WORKSPACE_STORAGE_SUBNET);
        }
    }
```

Resources are defined using the standard Azure Java SDK but with the following constraints to consider:

- The purpose of the resources must be indicated in the deployment.
- Resources not included in the deployment won't be created.
- The `create()` method in the resource definition must not be called.
- The resource definition must have the required configuration for a creation before it can be added to the deployment.

### Naming Resources and Idempotency

You can use the resource name generator in the deployment context to guarantee that names are consistent in retry
attempts.

The resource name generator creates a name from a hash of the landing zone id and internal sequence number.
As long as the landing zone id is globally unique, the resulting name will be the same across retry attempts with a very
low probability of a naming collision.

> The Azure Resource Manager APIs can be retried if a transient error occurs - the API is idempotent. However, The
> request must be the same as the failed one to avoid duplicating resources in the deployment. The deployment could
> create
> duplicate resources if the resource's name is auto-generated and changes in every request.

An instance of the resource name generator is included in the deployment context.

```java
 var storage=azureResourceManager
        .storageAccounts()
        .define(definitionContext.resourceNameGenerator().nextName(20))
        .withRegion(Region.US_EAST2)
        .withExistingResourceGroup(definitionContext.resourceGroup());

```

### Handling Prerequisites

The library deploys resources in a non-deterministic order. Therefore, it is not possible to assume any specific order.
For cases when a resource must be created before other resources, you can create prerequisite deployment inside your
definition.

```java

class FooLZDefinitionV1 extends LandingZoneDefinition {

    protected FooLZDefinitionV1(
            AzureResourceManager azureResourceManager, RelayManager relayManager) {
        super(azureResourceManager, relayManager);
    }

    @Override
    public Deployable definition(DefinitionContext definitionContext) {

        var vNet =
                azureResourceManager
                        .networks()
                        .define(definitionContext.resourceNameGenerator().nextName(20))
                        .withRegion(Region.US_EAST2)
                        .withExistingResourceGroup(definitionContext.resourceGroup())
                        .withAddressSpace("10.0.0.0/28")
                        .withSubnet("subnet1", "10.0.0.0/29")

        var prerequisites =
                deployment
                        .definePrerequisites()
                        .withVNetWithPurpose(vNet, "subnet1", SubnetResourcePurpose.WORKSPACE_STORAGE_SUBNET)
                        .deploy();

        var storage =
                azureResourceManager
                        .storageAccounts()
                        .define(definitionContext.resourceNameGenerator().nextName(20))
                        .withRegion(Region.US_EAST2)
                        .withExistingResourceGroup(definitionContext.resourceGroup());

        return definitionContext
                .deployment()
                .withResourceWithPurpose(storage, ResourcePurpose.SHARED_RESOURCE);
    }
```

## Landing Zone Manager

The Landing Zone Manager is the high-level component that lists the available Landing Zone Definition factories, deploys
Landing Zone Definitions and lists resources per purpose.

The Landing Zone Manager requires a `TokenCredential`, `AzureProfile` and `resourceGroupName`.

```java
landingZoneManager=
        LandingZoneManager.createLandingZoneManager(
        credential,
        azureProfile,
        resourceGroupName);
```

### Deploying a Landing Zone Definition

You can deploy Landing Zone Definition using the manager.

```java
    List<DeployedResource> resources=
        landingZoneManager.deployLandingZone(
        landingZoneId,
        FooLZDefinitionV1.class,
        DefinitionVersion.V1);

```

The manager has an asynchronous API for deployments. You can implement retry capabilities using standard reactive retry
policies.

```java
    Flux<DeployedResource> resources=
        landingZoneManager
        .deployLandingZoneAsync(landingZoneId,FooLZDefinitionV1.class,DefinitionVersion.V1)
        .retryWhen(Retry.max(1));

```

### Reading Landing Zone Resources

You can list resources by purpose using the Landing Zone Manager:

```java
List<DeployedResource> resources=landingZoneManager.reader().listResourcesByPurpose(ResourcePurpose.SHARED_RESOURCE);

```

Virtual Networks can be listed by subnet purpose:

```java
    List<DeployedVNet> vNets=
        landingZoneManager.reader().listVNetWithSubnetPurpose(SubnetResourcePurpose.WORKSPACE_COMPUTE_SUBNET);

```