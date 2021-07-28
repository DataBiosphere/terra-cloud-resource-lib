import com.azure.core.credential.TokenCredential;
import com.azure.core.management.AzureEnvironment;
import com.azure.core.management.profile.AzureProfile;
import com.azure.identity.DefaultAzureCredential;
import com.azure.identity.DefaultAzureCredentialBuilder;
import com.microsoft.azure.management.resources.ResourceManagementClient;
import com.microsoft.azure.management.resources.models.DeploymentExtended;
import com.microsoft.azure.utility.ComputeHelper;
import com.microsoft.azure.utility.ResourceContext;
import com.microsoft.azure.utility.ResourceHelper;
import com.microsoft.windowsazure.management.configuration.ManagementConfiguration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class AzureResourceManagerClient implements AutoCloseable {
    private final Logger logger = LoggerFactory.getLogger(AzureResourceManagerClient.class);

    DefaultAzureCredential defaultCredential = new DefaultAzureCredentialBuilder().build();


    final AzureProfile profile = new AzureProfile(AzureEnvironment.AZURE);
    final TokenCredential credential = new DefaultAzureCredentialBuilder()
            .authorityHost(profile.getEnvironment().getActiveDirectoryEndpoint())
            .build();

    AzureResourceManager azureResourceManager = AzureResourceManager
            .configure()
            .withLogLevel(HttpLogDetailLevel.BASIC)
            .authenticate(credential, profile)
            .withDefaultSubscription();

    @Override
    public void close() {
        //TODO
    }
}
