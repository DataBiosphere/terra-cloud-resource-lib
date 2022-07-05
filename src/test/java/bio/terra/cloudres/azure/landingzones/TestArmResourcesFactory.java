package bio.terra.cloudres.azure.landingzones;

import com.azure.core.credential.TokenCredential;
import com.azure.core.management.AzureEnvironment;
import com.azure.core.management.Region;
import com.azure.core.management.profile.AzureProfile;
import com.azure.identity.DefaultAzureCredentialBuilder;
import com.azure.resourcemanager.AzureResourceManager;
import com.azure.resourcemanager.resources.models.ResourceGroup;
import org.junit.jupiter.api.Tag;

import java.util.Locale;
import java.util.UUID;

import static java.lang.System.getenv;

public class TestArmResourcesFactory {

    private static final String TEST_SUBSCRIPTION_ID_ENV_VAR = "TEST_SUBSCRIPTION_ID";
    private static final String TEST_SUBSCRIPTION_ID = getenv(TEST_SUBSCRIPTION_ID_ENV_VAR);
    private static final String TEST_TENANT_ID_ENV_VAR = "TEST_TENANT_ID";
    private static final String TEST_TENANT_ID = getenv(TEST_TENANT_ID_ENV_VAR);

    public static AzureResourceManager createArmClient(){
        AzureProfile profile = new AzureProfile(TEST_TENANT_ID, TEST_SUBSCRIPTION_ID,AzureEnvironment.AZURE);
        TokenCredential credential = new DefaultAzureCredentialBuilder()
                //.authorityHost(profile.getEnvironment().getActiveDirectoryEndpoint())
                .build();
        return   AzureResourceManager
                .authenticate(credential,profile)
                .withSubscription(TEST_SUBSCRIPTION_ID);

    }

    public static ResourceGroup createTestResourceGroup(AzureResourceManager azureResourceManager){
        String resourceGroupId = UUID.randomUUID().toString();
        return azureResourceManager.resourceGroups().define("test-"+resourceGroupId)
                .withRegion(Region.US_EAST2)
                .create();
    }

    public static String createUniqueAzureResourceName(){
        return UUID.randomUUID().toString()
                .toLowerCase(Locale.ROOT)
                .replace("-","")
                .substring(0,23);
    }
}
