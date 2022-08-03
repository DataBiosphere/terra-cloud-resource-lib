package bio.terra.cloudres.azure.landingzones.definition.factories;

import bio.terra.cloudres.azure.landingzones.definition.ArmManagers;
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
import java.util.List;

import static bio.terra.cloudres.azure.landingzones.definition.ResourceNameGenerator.MAX_STORAGE_ACCOUNT_NAME_LENGTH;
import static bio.terra.cloudres.azure.landingzones.definition.ResourceNameGenerator.MAX_VNET_NAME_LENGTH;

public class TestLandingZoneFactory extends ArmClientsDefinitionFactory {
  public static final String LZ_NAME = "LZ_NAME";
  public static final String LZ_DESC = "LZ_DESC";

  TestLandingZoneFactory() {}

  public TestLandingZoneFactory(ArmManagers armManagers) {
    super(armManagers);
  }

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
    return new TestLandingZone(armManagers);
  }

  class TestLandingZone extends LandingZoneDefinition {

    protected TestLandingZone(ArmManagers armManagers) {
      super(armManagers);
    }

    @Override
    public Deployable definition(DefinitionContext definitionContext) {
      AzureResourceManager azureResourceManager = armManagers.azureResourceManager();

      var storage =
          azureResourceManager
              .storageAccounts()
              .define(
                  definitionContext
                      .resourceNameGenerator()
                      .nextName(MAX_STORAGE_ACCOUNT_NAME_LENGTH))
              .withRegion(Region.US_EAST2)
              .withExistingResourceGroup(definitionContext.resourceGroup());

      var vNet =
          azureResourceManager
              .networks()
              .define(definitionContext.resourceNameGenerator().nextName(MAX_VNET_NAME_LENGTH))
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
