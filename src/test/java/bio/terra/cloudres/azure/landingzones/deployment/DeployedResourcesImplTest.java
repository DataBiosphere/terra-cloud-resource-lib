package bio.terra.cloudres.azure.landingzones.deployment;

import bio.terra.cloudres.azure.landingzones.TestArmResourcesFactory;
import com.azure.resourcemanager.AzureResourceManager;
import com.azure.resourcemanager.network.models.PrivateLinkSubResourceName;
import com.azure.resourcemanager.resources.models.ResourceGroup;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.hasSize;

@Tag("integration")
class DeployedResourcesImplTest {

    private static AzureResourceManager azureResourceManager;
    private static ResourceGroup resourceGroup;
    private static String storageAccountName;
    private static String vNetName;
    private LandingZoneDeploymentsImpl landingZoneResources;

    private String landingZoneId;

    @BeforeEach
    void setUp(){
        landingZoneId = UUID.randomUUID().toString();
        landingZoneResources =  new LandingZoneDeploymentsImpl();
        storageAccountName = TestArmResourcesFactory.createUniqueAzureResourceName();
        vNetName = TestArmResourcesFactory.createUniqueAzureResourceName();

    }

    @BeforeAll
    static void setUpClients(){
        azureResourceManager = TestArmResourcesFactory.createArmClient();
        resourceGroup = TestArmResourcesFactory.createTestResourceGroup(azureResourceManager);
    }

    @AfterAll
    static void cleanUp(){
        azureResourceManager.resourceGroups().deleteByName(resourceGroup.name());
    }


    @Test
    void deploy_sharedStorageAccount() {
       var storage =   azureResourceManager.storageAccounts()
               .define(storageAccountName)
               .withRegion(resourceGroup.regionName())
               .withExistingResourceGroup(resourceGroup);

        var resources = landingZoneResources.define(landingZoneId)
                      .withResourceWithPurpose(storage, ResourcePurpose.SHARED_RESOURCE)
                .deploy();

        assertThatSharedStorageAccountIsCreated(resources);
    }

    private void assertThatSharedStorageAccountIsCreated(List<DeployedResource> resources) {
        assertThat(resources, hasSize(1));
        DeployedResource resource = resources.iterator().next();

        var createdStorage = azureResourceManager.storageAccounts().getById(resource.resourceId());
        assertThat(createdStorage.id(), equalTo(resource.resourceId()));
        assertThat(resource.region(), equalTo(resourceGroup.regionName()));
        assertThat(resource.tags().get(LandingZoneTagKeys.LANDING_ZONE_ID.toString()),
                equalTo(landingZoneId));
        assertThat(resource.tags().get(LandingZoneTagKeys.LANDING_ZONE_PURPOSE.toString()),
                equalTo(ResourcePurpose.SHARED_RESOURCE.toString()));
    }

    @Test
    void deploy_sharedVNetWithMultipleSubNets() {
        var vNet =   azureResourceManager.networks()
                .define(vNetName)
                .withRegion(resourceGroup.regionName())
                .withExistingResourceGroup(resourceGroup)
                .withAddressSpace("10.0.0.0/28")
                .withSubnet("compute", "10.0.0.0/29")
                .withSubnet("storage", "10.0.0.8/29");


        var resources = landingZoneResources.define(landingZoneId)
                .withVNetWithPurpose(vNet,"compute", SubnetResourcePurpose.WORKSPACE_COMPUTE_SUBNET)
                .withVNetWithPurpose(vNet,"storage", SubnetResourcePurpose.WORKSPACE_STORAGE_SUBNET)
                .deploy();

        assertThatVNetWithSubnetsIsCreated(resources);
    }

    private void assertThatVNetWithSubnetsIsCreated(List<DeployedResource> resources) {
        assertThat(resources, hasSize(1));
        DeployedResource resource = resources.iterator().next();
        var createdVNet = azureResourceManager.networks().getById(resource.resourceId());
        assertThat(createdVNet.id(), equalTo(resource.resourceId()));
        assertThat(createdVNet.tags().get(LandingZoneTagKeys.LANDING_ZONE_ID.toString()), equalTo(landingZoneId));
        assertThat(createdVNet.tags().get(SubnetResourcePurpose.WORKSPACE_COMPUTE_SUBNET.toString()), equalTo("compute"));
        assertThat(createdVNet.tags().get(SubnetResourcePurpose.WORKSPACE_STORAGE_SUBNET.toString()), equalTo("storage"));
    }

    @Test
    void deploy_privateStorageInVNet() {

        var vNet =   azureResourceManager.networks()
                .define(vNetName)
                .withRegion(resourceGroup.regionName())
                .withExistingResourceGroup(resourceGroup)
                .withAddressSpace("10.0.0.0/28")
                .withSubnet("subnet1", "10.0.0.0/29");


        var storage = azureResourceManager.storageAccounts()
                .define(storageAccountName)
                .withRegion(resourceGroup.regionName())
                .withExistingResourceGroup(resourceGroup);

        var networkResources = landingZoneResources.define(landingZoneId)
                .withVNetWithPurpose(vNet,"subnet1",SubnetResourcePurpose.WORKSPACE_STORAGE_SUBNET)
                .withResourceWithPurpose(storage, ResourcePurpose.SHARED_RESOURCE)
                .deploy();

        var vNetId =
                networkResources.stream().filter(r->r.resourceType().equals("Microsoft.Network/virtualNetworks"))
                        .findFirst().get().resourceId();
        var storageId =
                networkResources.stream().filter(r->r.resourceType().equals("Microsoft.Storage/storageAccounts"))
                        .findFirst().get().resourceId();
        var deployedVNet = azureResourceManager.networks().getById(vNetId);

        var privateEndpoint = azureResourceManager.privateEndpoints().define(
                TestArmResourcesFactory.createUniqueAzureResourceName())
                .withRegion(resourceGroup.regionName())
                .withExistingResourceGroup(resourceGroup)
                .withSubnetId(deployedVNet.subnets().get("subnet1").id())
                .definePrivateLinkServiceConnection(TestArmResourcesFactory.createUniqueAzureResourceName())
                .withResourceId(storageId)
                .withSubResource(PrivateLinkSubResourceName.STORAGE_BLOB)
                .attach();

        var privateEndpointResource = landingZoneResources
                .define(landingZoneId)
                .withResource(privateEndpoint)
                        .deploy();

        assertThatStorageWithPrivateEndpointInVNetIsCreated(networkResources, vNetId, storageId, privateEndpointResource);


    }

    private void assertThatStorageWithPrivateEndpointInVNetIsCreated(List<DeployedResource> networkResources, String vNetId, String storageId, List<DeployedResource> privateEndpointResource) {
        var privateEndpointId = privateEndpointResource.iterator().next().resourceId();
        assertThat(networkResources, hasSize(2));
        assertThat(privateEndpointResource, hasSize(1));
        var deployedNetwork = azureResourceManager.networks().getById(vNetId);
        var deployedStorage = azureResourceManager.storageAccounts().getById(storageId);
        var deployedPrivateEndpoint = azureResourceManager.privateEndpoints()
                .getById(privateEndpointId);

        assertThat(deployedNetwork.id(), equalTo(vNetId));
        assertThat(deployedStorage.id(), equalTo(storageId));
        assertThat(deployedPrivateEndpoint.id(), equalTo(privateEndpointId));
    }
}