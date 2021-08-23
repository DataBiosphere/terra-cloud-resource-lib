package bio.terra.cloudres.azure.resouce.manager;

import com.azure.core.credential.TokenCredential;
import com.azure.identity.ClientSecretCredentialBuilder;

import java.util.UUID;

/** Authenticates an application against user Azure tenants using a secret */
public class SecretCredentials extends Credentials {
    // A valid and current secret (e.g. application password) for the application
    private final String secret;

    public SecretCredentials(UUID applicationId, UUID homeTenantId, String secret) {
        super(applicationId, homeTenantId);
        this.secret = secret;
    }

    @Override
    public TokenCredential getAzureCreds() {
        return new ClientSecretCredentialBuilder()
                .clientId(getApplicationId().toString())
                .clientSecret(this.secret)
                .tenantId(getHomeTenantId().toString())
                .build();
    }
}
