package bio.terra.cloudres.azure.landingzones.management;

import bio.terra.cloudres.azure.landingzones.TestUtils;
import bio.terra.cloudres.azure.landingzones.definition.DefinitionVersion;
import bio.terra.cloudres.azure.landingzones.definition.FactoryInfo;
import bio.terra.cloudres.azure.landingzones.definition.factories.TestLandingZoneFactory;
import bio.terra.cloudres.azure.landingzones.deployment.DeployedResource;
import bio.terra.cloudres.azure.resourcemanager.common.AzureIntegrationUtils;
import bio.terra.cloudres.azure.resourcemanager.common.TestArmResourcesFactory;
import com.azure.resourcemanager.AzureResourceManager;
import com.azure.resourcemanager.resources.models.GenericResource;
import com.azure.resourcemanager.resources.models.ResourceGroup;
import org.junit.jupiter.api.*;
import reactor.core.publisher.Flux;
import reactor.util.retry.Retry;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;

@Tag("integration")
class LandingZoneManagerTest {

  private static AzureResourceManager azureResourceManager;
  private static ResourceGroup resourceGroup;
  private LandingZoneManager landingZoneManager;

  @BeforeAll
  static void setUpTestResourceGroup() {
    azureResourceManager = TestArmResourcesFactory.createArmClient();
    resourceGroup = TestArmResourcesFactory.createTestResourceGroup(azureResourceManager);
  }

  @AfterAll
  static void cleanUpArmResources() {
    azureResourceManager.resourceGroups().deleteByName(resourceGroup.name());
  }

  @BeforeEach
  void setUp() {
    landingZoneManager =
        LandingZoneManager.createLandingZoneManager(
            AzureIntegrationUtils.getAdminAzureCredentialsOrDie(),
            AzureIntegrationUtils.TERRA_DEV_AZURE_PROFILE,
            resourceGroup.name());
  }

  @Test
  void deployLandingZone_deploysTestLandingZoneDefinition() {
    List<DeployedResource> resources =
        landingZoneManager.deployLandingZone(
            UUID.randomUUID().toString(), TestLandingZoneFactory.class, DefinitionVersion.V1);

    // the test landing zone creates two resources: storage account and vnet.
    assertThat(resources, hasSize(2));
    assertThat(TestUtils.findFirstStorageAccountId(resources), is(notNullValue()));
    assertThat(TestUtils.findFirstVNetId(resources), is(notNullValue()));
  }

  @Test
  void deployLandingZone_duplicateDeploymentWithRetry_deploysSuccessfullyOnlyOneInstance() {
    String landingZone = UUID.randomUUID().toString();
    Flux<DeployedResource> first =
        landingZoneManager
            .deployLandingZoneAsync(landingZone, TestLandingZoneFactory.class, DefinitionVersion.V1)
            .retryWhen(Retry.max(1));

    Flux<DeployedResource> second =
        landingZoneManager
            .deployLandingZoneAsync(landingZone, TestLandingZoneFactory.class, DefinitionVersion.V1)
            .retryWhen(Retry.max(1));

    var results = Flux.merge(first, second).toStream().collect(Collectors.toList());

    // There should be 4 items in the list. 2 for each deployment.
    assertThat(results, hasSize(4));

    // however there should be only 2 distinct resources.
    var distinct = results.stream().distinct().collect(Collectors.toList());
    assertThat(distinct, hasSize(2));
    assertThat(TestUtils.findFirstStorageAccountId(distinct), is(notNullValue()));
    assertThat(TestUtils.findFirstVNetId(distinct), is(notNullValue()));


    assertThatExpectedResourcesExistsInResourceGroup(distinct);
  }

  private void assertThatExpectedResourcesExistsInResourceGroup(List<DeployedResource> result) {

    var resourcesInGroup =
            azureResourceManager.genericResources().listByResourceGroup(resourceGroup.name()).stream()
                    .collect(Collectors.toList());

    //there should be two resources in the group.
    assertThat(resourcesInGroup, hasSize(2));
    assertThat(
        resourcesInGroup.stream()
            .filter(r -> r.id().equals(TestUtils.findFirstStorageAccountId(result)))
            .findFirst()
            .get(),
        is(notNullValue()));
    assertThat(
        resourcesInGroup.stream()
            .filter(r -> r.id().equals(TestUtils.findFirstVNetId(result)))
            .findFirst()
            .get(),
        is(notNullValue()));
  }

  @Test
  void listDefinitionFactories_testFactoryIsListed() {
    var factories = landingZoneManager.listDefinitionFactories();

    FactoryInfo testFactory = new FactoryInfo(TestLandingZoneFactory.class, List.of(DefinitionVersion.V1));

    assertThat(factories,hasItem(testFactory));
  }

  @Test
  void reader() {}

  @Test
  void deployments() {}

  @Test
  void provider() {}
}
