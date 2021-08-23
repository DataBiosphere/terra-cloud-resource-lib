package bio.terra.cloudres.azure.resouce.manager;

import com.azure.core.credential.TokenCredential;
import com.azure.identity.UsernamePasswordCredentialBuilder;

import java.util.UUID;

/** Authenticates an application against user Azure tenants using a username/password */
public class UsernamePasswordCredentials extends Credentials {
    private final String username;
    private final String password;

    public UsernamePasswordCredentials(UUID applicationId, UUID homeTenantId, String username, String password) {
        super(applicationId, homeTenantId);
        this.username = username;
        this.password = password;
    }

    @Override
    public TokenCredential getAzureCreds() {
        return new UsernamePasswordCredentialBuilder()
                .clientId(getApplicationId().toString())
                .tenantId(getHomeTenantId().toString())
                .username(this.username)
                .password(this.password)
                .build();
    }
}
