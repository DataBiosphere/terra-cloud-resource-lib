package bio.terra.cloudres.azure.resouce.manager;

import com.azure.core.credential.TokenCredential;
import com.azure.core.management.AzureEnvironment;
import com.azure.core.management.profile.AzureProfile;
import com.azure.identity.ClientSecretCredentialBuilder;
import com.azure.resourcemanager.AzureResourceManager;

import java.io.IOException;
import java.util.Properties;
import java.util.UUID;

public class AzureResourceConfiguration {
    private Credentials credentials;

    public AzureResourceConfiguration(Credentials credentials) {
        this.credentials = credentials;
    }

    public Credentials getCredentials() {
        return credentials;
    }

    /**
     * Given a user tenant Id, return Azure credentials
     *
     * @param tenantId The ID of a user tenant
     * @return A credential object that can be used to interact with Azure apis
     */
    public TokenCredential getAppToken(final UUID tenantId) {
        return new ClientSecretCredentialBuilder()
                .clientId(credentials.applicationId.toString())
                .clientSecret(credentials.secret)
                .tenantId(tenantId.toString())
                .build();
    }

    /**
     * Get Azure credentials authenticated against the home tenant. Use this authentication method
     * when accessing resources within a deployed managed application
     *
     * @return A credential object that can be used to interact with Azure apis
     */
    public TokenCredential getAppToken() {
        return getAppToken(credentials.getHomeTenantId());
    }

    /**
     * Get a resource manager client to a user's tenant. This object can then be used to
     * create/destroy resources
     *
     * @param tenantId The ID of the user's tenant
     * @param subscriptionId The ID of the subscription that will be charged for the resources created
     *     with this client
     * @return An authenticated {@link AzureResourceManager} client
     */
    public AzureResourceManager getClient(final UUID tenantId, final UUID subscriptionId) {
        final AzureProfile profile =
                new AzureProfile(tenantId.toString(), subscriptionId.toString(), AzureEnvironment.AZURE);
        return AzureResourceManager.authenticate(getAppToken(tenantId), profile)
                .withSubscription(subscriptionId.toString());
    }

    /**
     * Get a resource manager client to a user's subscription but through TDR's tenant.
     *
     * @param subscriptionId The ID of the subscription that will be charged for the resources created
     *     with this client
     * @return An authenticated {@link AzureResourceManager} client
     */
    public AzureResourceManager getClient(final UUID subscriptionId) {
        return getClient(credentials.getHomeTenantId(), subscriptionId);
    }
}


