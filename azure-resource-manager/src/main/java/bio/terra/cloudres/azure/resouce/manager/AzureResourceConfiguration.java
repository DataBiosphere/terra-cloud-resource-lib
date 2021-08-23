package bio.terra.cloudres.azure.resouce.manager;

import com.azure.core.management.AzureEnvironment;
import com.azure.core.management.profile.AzureProfile;
import com.azure.resourcemanager.AzureResourceManager;

import java.util.UUID;

/**
 * Configuration for working with Azure Resource Manager (ARM).
 */
public class AzureResourceConfiguration {
    private final Credentials credentials;

    public AzureResourceConfiguration(Credentials credentials) {
        this.credentials = credentials;
    }

    /**
     * Get a resource manager client to a user's tenant. This object can then be used to
     * create/destroy resources.
     *
     * @param tenantId The ID of the user's tenant
     * @param subscriptionId The ID of the subscription that will be charged for the resources created
     *     with this client
     * @return An authenticated {@link AzureResourceManager} client
     */
    AzureResourceManager getClient(final UUID tenantId, final UUID subscriptionId) {
        final AzureProfile profile =
                new AzureProfile(tenantId.toString(), subscriptionId.toString(), AzureEnvironment.AZURE);
        return AzureResourceManager.authenticate(credentials.getAzureCreds(), profile)
                .withSubscription(subscriptionId.toString());
    }

    /**
     * Get a resource manager client to a user's subscription but through the Terra tenant.
     *
     * @param subscriptionId The ID of the subscription that will be charged for the resources created
     *     with this client
     * @return An authenticated {@link AzureResourceManager} client
     */
    AzureResourceManager getClient(final UUID subscriptionId) {
        return getClient(credentials.getHomeTenantId(), subscriptionId);
    }
}


