package bio.terra.cloudres.azure.resourcemanager.common;

import bio.terra.cloudres.azure.landingzones.definition.ArmManagers;
import bio.terra.cloudres.azure.landingzones.deployment.DeployedResource;
import com.azure.core.credential.TokenCredential;
import com.azure.core.management.profile.AzureProfile;
import com.azure.resourcemanager.resources.models.ResourceGroup;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;

import java.util.List;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.MatcherAssert.assertThat;

/** wires up ARM clients and creates a test resource group. */
public class LandingZoneTestFixture {
  protected static ArmManagers armManagers;
  protected static ResourceGroup resourceGroup;

  protected static TokenCredential tokenCredential;
  protected static AzureProfile azureProfile;

  @BeforeAll
  static void setUpBeforeAll() {
    armManagers =
        new ArmManagers(
            TestArmResourcesFactory.createArmClient(),
            TestArmResourcesFactory.createRelayArmClient());
    resourceGroup =
        TestArmResourcesFactory.createTestResourceGroup(armManagers.azureResourceManager());
    tokenCredential = AzureIntegrationUtils.getAdminAzureCredentialsOrDie();
    azureProfile = AzureIntegrationUtils.TERRA_DEV_AZURE_PROFILE;
  }

  @AfterAll
  static void cleanUpAfterAll() {
    armManagers.azureResourceManager().resourceGroups().deleteByName(resourceGroup.name());
  }

  protected void assertAllResourcesExists(List<DeployedResource> resources) {
    for (DeployedResource resource : resources) {
      assertThat(
          armManagers
              .azureResourceManager()
              .genericResources()
              .checkExistenceById(resource.resourceId()),
          equalTo(true));
    }
  }
}
