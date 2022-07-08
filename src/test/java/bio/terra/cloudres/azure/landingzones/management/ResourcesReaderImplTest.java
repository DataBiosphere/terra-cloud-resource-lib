package bio.terra.cloudres.azure.landingzones.management;

import bio.terra.cloudres.azure.landingzones.definition.DefinitionVersion;
import bio.terra.cloudres.azure.landingzones.definition.factories.LandingZoneDefinitionFactory;
import bio.terra.cloudres.azure.landingzones.definition.factories.LandingZoneDefinitionProvider;
import bio.terra.cloudres.azure.landingzones.definition.factories.LandingZoneDefinitionProviderImpl;
import bio.terra.cloudres.azure.landingzones.definition.factories.TestLandingZoneFactory;
import bio.terra.cloudres.azure.landingzones.deployment.*;
import bio.terra.cloudres.azure.landingzones.deployment.LandingZoneDeployment.DefinitionStages.WithLandingZoneResource;
import bio.terra.cloudres.azure.resourcemanager.common.TestArmResourcesFactory;
import com.azure.resourcemanager.AzureResourceManager;
import com.azure.resourcemanager.resources.models.ResourceGroup;
import org.junit.jupiter.api.*;

import java.util.HashMap;
import java.util.List;
import java.util.UUID;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.hasToString;

@Tag("integration")
class ResourcesReaderImplTest {

    private static AzureResourceManager azureResourceManager;
    private static ResourceGroup resourceGroup;
    private static LandingZoneDefinitionFactory landingZoneFactory;

    private static LandingZoneDefinitionProvider landingZoneDefinitionProvider;

    private static LandingZoneDeployments landingZoneDeployments;

    private static WithLandingZoneResource landingZoneResourceDeployment;

    private static List<DeployedResource> landingZoneResources;
    private ResourcesReader resourcesReader;

    @BeforeAll
    static void setUpTestLandingZone() {
        azureResourceManager = TestArmResourcesFactory.createArmClient();
        resourceGroup = TestArmResourcesFactory.createTestResourceGroup(azureResourceManager);
        landingZoneDeployments = new LandingZoneDeploymentsImpl();
        landingZoneResourceDeployment = landingZoneDeployments.define(UUID.randomUUID().toString());

        landingZoneDefinitionProvider = new LandingZoneDefinitionProviderImpl(
                TestArmResourcesFactory.createArmManagers());
        landingZoneFactory = landingZoneDefinitionProvider.createDefinitionFactory(TestLandingZoneFactory.class);

        landingZoneResources = landingZoneFactory.create(DefinitionVersion.V1)
                .definition(landingZoneResourceDeployment, resourceGroup)
                .deploy();
    }

    @AfterAll
    static void cleanUpArmResources() {
        azureResourceManager.resourceGroups().deleteByName(resourceGroup.name());
    }

    @BeforeEach
    void setUp() {
        resourcesReader = new ResourcesReaderImpl(azureResourceManager, resourceGroup);
    }

    @Test
    void listSharedResources_storageResourceIsSharedResource() {
        var resources = resourcesReader.listSharedResources();

        assertThat(resources, contains(hasToString(getDeployedStorage().toString())));
    }

    @Test
    void listResourcesByPurpose_storageResourceIsSharedResource() {
        var resources = resourcesReader.listResourcesByPurpose(ResourcePurpose.SHARED_RESOURCE);

        assertThat(resources, contains(hasToString(getDeployedStorage().toString())));
    }

    @Test
    void listVNetWithSubnetPurpose() {
        var resources = resourcesReader
                .listVNetWithSubnetPurpose(SubnetResourcePurpose.WORKSPACE_COMPUTE_SUBNET);

        assertThat(resources, contains(hasToString(getDeployedVNet().toString())));

    }

    private DeployedResource getDeployedStorage() {
        return landingZoneResources
                .stream()
                .filter(c -> c.resourceType().equals("Microsoft.Storage/storageAccounts"))
                .findFirst()
                .get();

    }

    private DeployedVNet getDeployedVNet() {
        var deployedVNet = landingZoneResources
                .stream()
                .filter(c -> c.resourceType().equals("Microsoft.Network/virtualNetworks"))
                .findFirst()
                .get();

        var vNet = azureResourceManager.networks().getById(deployedVNet.resourceId());

        HashMap<SubnetResourcePurpose, DeployedSubnet> subnetHashMap = new HashMap<>();

        SubnetResourcePurpose
                .values()
                .forEach(p -> {
                    var subnetName = vNet.tags().get(p.toString());
                    if (subnetName != null) {
                        var subnet = vNet.subnets().get(subnetName);
                        subnetHashMap.put(p, new DeployedSubnet(subnet.id(), subnet.name()));
                    }
                });

        return new DeployedVNet(vNet.id(), subnetHashMap, vNet.regionName());

    }
}