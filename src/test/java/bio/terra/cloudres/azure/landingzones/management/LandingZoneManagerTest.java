package bio.terra.cloudres.azure.landingzones.management;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.hasItem;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;

import bio.terra.cloudres.azure.landingzones.TestUtils;
import bio.terra.cloudres.azure.landingzones.definition.DefinitionVersion;
import bio.terra.cloudres.azure.landingzones.definition.FactoryDefinitionInfo;
import bio.terra.cloudres.azure.landingzones.definition.factories.TestLandingZoneFactory;
import bio.terra.cloudres.azure.landingzones.deployment.DeployedResource;
import bio.terra.cloudres.azure.resourcemanager.common.AzureIntegrationUtils;
import bio.terra.cloudres.azure.resourcemanager.common.TestArmResourcesFactory;
import com.azure.core.util.logging.ClientLogger;
import com.azure.resourcemanager.AzureResourceManager;
import com.azure.resourcemanager.resources.models.ResourceGroup;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import reactor.core.publisher.Flux;
import reactor.util.retry.Retry;

@Tag("integration")
class LandingZoneManagerTest {

  private static AzureResourceManager azureResourceManager;
  private final ClientLogger logger = new ClientLogger(LandingZoneManagerTest.class);
  private ResourceGroup resourceGroup;
  private LandingZoneManager landingZoneManager;

  @BeforeAll
  static void setUpTestResourceGroup() {
    azureResourceManager = TestArmResourcesFactory.createArmClient();
  }

  @BeforeEach
  void setUp() {
    resourceGroup = TestArmResourcesFactory.createTestResourceGroup(azureResourceManager);
    landingZoneManager =
        LandingZoneManager.createLandingZoneManager(
            AzureIntegrationUtils.getAdminAzureCredentialsOrDie(),
            AzureIntegrationUtils.TERRA_DEV_AZURE_PROFILE,
            resourceGroup.name());
  }

  @AfterEach
  void cleanUp() {
    azureResourceManager.resourceGroups().deleteByName(resourceGroup.name());
  }

  @Test
  void deployLandingZone_deploysTestLandingZoneDefinition() {
    List<DeployedResource> resources =
        landingZoneManager.deployLandingZone(
            UUID.randomUUID().toString(),
            TestLandingZoneFactory.class.getName(),
            DefinitionVersion.V1,
            null);

    // the test landing zone creates two resources: storage account and vnet.
    assertThat(resources, hasSize(2));
    assertThat(TestUtils.findFirstStorageAccountId(resources), is(notNullValue()));
    assertThat(TestUtils.findFirstVNetId(resources), is(notNullValue()));
  }

  @Test
  void deployLandingZone_duplicateDeploymentWithRetry_deploysSuccessfullyOnlyOneInstance()
      throws InterruptedException {
    String landingZone = UUID.randomUUID().toString();
    Flux<DeployedResource> first =
        landingZoneManager
            .deployLandingZoneAsync(
                landingZone, TestLandingZoneFactory.class.getName(), DefinitionVersion.V1, null)
            .retryWhen(Retry.max(1));

    Flux<DeployedResource> second =
        landingZoneManager
            .deployLandingZoneAsync(
                landingZone, TestLandingZoneFactory.class.getName(), DefinitionVersion.V1, null)
            .retryWhen(Retry.max(1));

    var results = Flux.merge(first, second).collectList().block();

    // There should be 4 items in the list. 2 for each deployment.
    assertThat(results, hasSize(4));

    // however there should be only 2 distinct resources.
    var distinct = results.stream().distinct().collect(Collectors.toList());
    assertThat(distinct, hasSize(2));
    assertThat(TestUtils.findFirstStorageAccountId(distinct), is(notNullValue()));
    assertThat(TestUtils.findFirstVNetId(distinct), is(notNullValue()));

    assertThatExpectedResourcesExistsInResourceGroup(distinct);
  }

  private void assertThatExpectedResourcesExistsInResourceGroup(List<DeployedResource> result)
      throws InterruptedException {

    var resourcesInGroup =
        azureResourceManager.genericResources().listByResourceGroup(resourceGroup.name()).stream()
            .collect(Collectors.toList());

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
    var factories = LandingZoneManager.listDefinitionFactories();
    FactoryDefinitionInfo testFactory =
        new FactoryDefinitionInfo(
            TestLandingZoneFactory.LZ_NAME,
            TestLandingZoneFactory.LZ_DESC,
            TestLandingZoneFactory.class.getName(),
            List.of(DefinitionVersion.V1));

    assertThat(factories, hasItem(testFactory));
  }
}
