package bio.terra.cloudres.azure.resourcemanager.common;

import bio.terra.cloudres.azure.landingzones.definition.ArmManagers;
import bio.terra.cloudres.azure.resourcemanager.common.AzureIntegrationUtils;
import com.azure.core.credential.TokenCredential;
import com.azure.core.management.AzureEnvironment;
import com.azure.core.management.Region;
import com.azure.core.management.profile.AzureProfile;
import com.azure.identity.DefaultAzureCredentialBuilder;
import com.azure.resourcemanager.AzureResourceManager;
import com.azure.resourcemanager.relay.RelayManager;
import com.azure.resourcemanager.resources.models.ResourceGroup;
import org.junit.jupiter.api.Tag;

import java.util.Locale;
import java.util.UUID;

import static java.lang.System.getenv;

public class TestArmResourcesFactory {

    public static AzureResourceManager createArmClient(){
        AzureProfile profile = AzureIntegrationUtils.TERRA_DEV_AZURE_PROFILE;
        return   AzureResourceManager
                .authenticate(
                        AzureIntegrationUtils.getAdminAzureCredentialsOrDie()
                        ,profile)
                .withSubscription(profile.getSubscriptionId());

    }

    public static ArmManagers createArmManagers(){
        return new ArmManagers(createArmClient(), createRelayArmClient());
    }

    public static RelayManager createRelayArmClient(){
        return RelayManager
                .authenticate(
                        AzureIntegrationUtils.getAdminAzureCredentialsOrDie(),
                        AzureIntegrationUtils.TERRA_DEV_AZURE_PROFILE);
    }

    public static ResourceGroup createTestResourceGroup(AzureResourceManager azureResourceManager){
        String resourceGroupId = UUID.randomUUID().toString();
        return azureResourceManager.resourceGroups().define("test-"+resourceGroupId)
                .withRegion(Region.US_WEST2)
                .create();
    }

    public static String createUniqueAzureResourceName(){
        return UUID.randomUUID().toString()
                .toLowerCase(Locale.ROOT)
                .replace("-","")
                .substring(0,23);
    }
}
