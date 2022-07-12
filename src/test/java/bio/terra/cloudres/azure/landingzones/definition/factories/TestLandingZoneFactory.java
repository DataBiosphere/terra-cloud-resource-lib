package bio.terra.cloudres.azure.landingzones.definition.factories;

import bio.terra.cloudres.azure.landingzones.definition.DefinitionContext;
import bio.terra.cloudres.azure.landingzones.definition.DefinitionHeader;
import bio.terra.cloudres.azure.landingzones.definition.DefinitionVersion;
import bio.terra.cloudres.azure.landingzones.definition.LandingZoneDefinable;
import bio.terra.cloudres.azure.landingzones.definition.LandingZoneDefinition;
import bio.terra.cloudres.azure.landingzones.deployment.LandingZoneDeployment.DefinitionStages.Deployable;
import bio.terra.cloudres.azure.landingzones.deployment.ResourcePurpose;
import bio.terra.cloudres.azure.landingzones.deployment.SubnetResourcePurpose;
import com.azure.core.management.Region;
import com.azure.resourcemanager.AzureResourceManager;
import com.azure.resourcemanager.relay.RelayManager;
import java.util.List;

public class TestLandingZoneFactory extends ArmClientsDefinitionFactory {
  protected TestLandingZoneFactory(
      AzureResourceManager azureResourceManager, RelayManager relayManager) {
    super(azureResourceManager, relayManager);
  }

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
    return new TestLandingZone(azureResourceManager, relayManager);
  }

  class TestLandingZone extends LandingZoneDefinition {

    protected TestLandingZone(
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
}
