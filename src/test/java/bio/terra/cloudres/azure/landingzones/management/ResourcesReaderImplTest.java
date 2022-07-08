package bio.terra.cloudres.azure.landingzones.management;

import bio.terra.cloudres.azure.landingzones.TestUtils;
import bio.terra.cloudres.azure.landingzones.definition.DefinitionContext;
import bio.terra.cloudres.azure.landingzones.definition.DefinitionVersion;
import bio.terra.cloudres.azure.landingzones.definition.ResourceNameGenerator;
import bio.terra.cloudres.azure.landingzones.definition.factories.LandingZoneDefinitionFactory;
import bio.terra.cloudres.azure.landingzones.definition.factories.LandingZoneDefinitionProvider;
import bio.terra.cloudres.azure.landingzones.definition.factories.LandingZoneDefinitionProviderImpl;
import bio.terra.cloudres.azure.landingzones.definition.factories.TestLandingZoneFactory;
import bio.terra.cloudres.azure.landingzones.deployment.DeployedResource;
import bio.terra.cloudres.azure.landingzones.deployment.DeployedSubnet;
import bio.terra.cloudres.azure.landingzones.deployment.DeployedVNet;
import bio.terra.cloudres.azure.landingzones.deployment.LandingZoneDeployment.DefinitionStages.WithLandingZoneResource;
import bio.terra.cloudres.azure.landingzones.deployment.LandingZoneDeployments;
import bio.terra.cloudres.azure.landingzones.deployment.LandingZoneDeploymentsImpl;
import bio.terra.cloudres.azure.landingzones.deployment.ResourcePurpose;
import bio.terra.cloudres.azure.landingzones.deployment.SubnetResourcePurpose;
import bio.terra.cloudres.azure.resourcemanager.common.TestArmResourcesFactory;
import com.azure.resourcemanager.AzureResourceManager;
import com.azure.resourcemanager.resources.models.ResourceGroup;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import java.util.HashMap;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.hasSize;

@Tag("integration")
class ResourcesReaderImplTest {

  private static AzureResourceManager azureResourceManager;
  private static ResourceGroup resourceGroup;
  private static LandingZoneDefinitionFactory landingZoneFactory;

  private static LandingZoneDefinitionProvider landingZoneDefinitionProvider;

  private static LandingZoneDeployments landingZoneDeployments;

  private static WithLandingZoneResource landingZoneResourceDeployment;

  private static List<DeployedResource> landingZoneResources;
  private static DeployedResource deployedStorage;
  private static DeployedVNet deployedVNet;
  private ResourcesReader resourcesReader;

  @BeforeAll
  static void setUpTestLandingZone() throws InterruptedException {
    azureResourceManager = TestArmResourcesFactory.createArmClient();
    resourceGroup = TestArmResourcesFactory.createTestResourceGroup(azureResourceManager);
    landingZoneDeployments = new LandingZoneDeploymentsImpl();
    String landingZoneId = UUID.randomUUID().toString();
    landingZoneResourceDeployment = landingZoneDeployments.define(UUID.randomUUID().toString());

    landingZoneDefinitionProvider =
        new LandingZoneDefinitionProviderImpl(TestArmResourcesFactory.createArmManagers());
    landingZoneFactory =
        landingZoneDefinitionProvider.createDefinitionFactory(TestLandingZoneFactory.class);
    landingZoneResources =
        landingZoneFactory
            .create(DefinitionVersion.V1)
            .definition(
                new DefinitionContext(
                    landingZoneId,
                    landingZoneResourceDeployment,
                    resourceGroup,
                    new ResourceNameGenerator(landingZoneId),
                    new HashMap<>()))
            .deploy();
    deployedStorage = getDeployedStorage();
    deployedVNet = getDeployedVNet();
    TimeUnit.SECONDS.sleep(5); // give some time for replication of tag data
  }

  @AfterAll
  static void cleanUpArmResources() {
    azureResourceManager.resourceGroups().deleteByName(resourceGroup.name());
  }

  private static DeployedResource getDeployedStorage() {
    return landingZoneResources.stream()
        .filter(c -> c.resourceType().equals("Microsoft.Storage/storageAccounts"))
        .findFirst()
        .get();
  }

  private static DeployedVNet getDeployedVNet() {
    var deployedVNet =
        landingZoneResources.stream()
            .filter(c -> c.resourceType().equals("Microsoft.Network/virtualNetworks"))
            .findFirst()
            .get();

    var vNet = azureResourceManager.networks().getById(deployedVNet.resourceId());

    HashMap<SubnetResourcePurpose, DeployedSubnet> subnetHashMap = new HashMap<>();

    SubnetResourcePurpose.values()
        .forEach(
            p -> {
              var subnetName = vNet.tags().get(p.toString());
              if (subnetName != null) {
                var subnet = vNet.subnets().get(subnetName);
                subnetHashMap.put(p, new DeployedSubnet(subnet.id(), subnet.name()));
              }
            });

    return new DeployedVNet(vNet.id(), subnetHashMap, vNet.regionName());
  }

  @BeforeEach
  void setUp() {
    resourcesReader = new ResourcesReaderImpl(azureResourceManager, resourceGroup);
  }

  @Test
  void listSharedResources_storageResourceIsSharedResource() {

    var resources = resourcesReader.listSharedResources();

    assertThat(resources, hasSize(1));
    assertThat(
        deployedStorage.resourceId(), equalTo(TestUtils.findFirstStorageAccountId(resources)));
  }

  @Test
  void listResourcesByPurpose_storageResourceIsSharedResource() {
    var resources = resourcesReader.listResourcesByPurpose(ResourcePurpose.SHARED_RESOURCE);

    assertThat(resources, hasSize(1));
    assertThat(
        deployedStorage.resourceId(), equalTo(TestUtils.findFirstStorageAccountId(resources)));
  }

  @Test
  void listVNetWithSubnetPurpose_returnsDeployedVNet() {
    var resources =
        resourcesReader.listVNetWithSubnetPurpose(SubnetResourcePurpose.WORKSPACE_COMPUTE_SUBNET);

    assertThat(resources, hasSize(1));
    assertThat(getDeployedVNet().Id(), equalTo(resources.iterator().next().Id()));
  }
}
