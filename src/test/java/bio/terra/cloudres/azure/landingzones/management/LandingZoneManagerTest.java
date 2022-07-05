package bio.terra.cloudres.azure.landingzones.management;

import bio.terra.cloudres.azure.landingzones.TestArmResourcesFactory;
import bio.terra.cloudres.azure.landingzones.definition.DefinitionVersion;
import bio.terra.cloudres.azure.landingzones.definition.factories.ManagedNetworkWithSharedResourcesFactory;
import bio.terra.cloudres.azure.landingzones.deployment.DeployedResource;
import com.azure.resourcemanager.AzureResourceManager;
import com.azure.resourcemanager.resources.models.ResourceGroup;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.hasSize;

@Tag("integration")
class LandingZoneManagerTest {

    private static AzureResourceManager azureResourceManager;
    private static ResourceGroup resourceGroup;


    @BeforeAll
    static void setUpTestResourceGroup() {
        azureResourceManager = TestArmResourcesFactory.createArmClient();
        resourceGroup = TestArmResourcesFactory.createTestResourceGroup(azureResourceManager);
    }

    private LandingZoneManager landingZoneManager;

    @BeforeEach
    void setUp() {
        landingZoneManager = LandingZoneManager.createLandingZoneManager(azureResourceManager, resourceGroup);
    }

    @AfterAll
    static void cleanUpArmResources() {
       azureResourceManager.resourceGroups().deleteByName(resourceGroup.name());
    }


    @Test
    void createLandingZoneManager_createsManagedVNetWithSharedResources() {
        List<DeployedResource> resources = landingZoneManager.deployLandingZone(
                UUID.randomUUID().toString(),
                ManagedNetworkWithSharedResourcesFactory.class,
                DefinitionVersion.V1);

        assertThat(resources, hasSize(4));
    }

    @Test
    void listDefinitionFactories() {
    }

    @Test
    void reader() {
    }

    @Test
    void deployments() {
    }

    @Test
    void provider() {
    }
}