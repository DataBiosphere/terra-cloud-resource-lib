package bio.terra.cloudres.azure.resouce.manager;

import com.azure.core.credential.TokenCredential;
import com.azure.identity.ClientSecretCredentialBuilder;
import com.azure.identity.UsernamePasswordCredentialBuilder;

import java.io.IOException;
import java.util.Properties;
import java.util.UUID;

public class SecretCredentials extends Credentials implements ICredentials {
    // A valid and current secret (e.g. application password) for the TDR application
    public String secret;

    SecretCredentials(UUID applicationId, UUID homeTenantId, String secret) {
        super(applicationId, homeTenantId);
        this.secret = secret;
    }

    public static SecretCredentials getFromPropsFile(String fileName) throws IOException {
        PropertyReader reader = new PropertyReader(fileName);
        Properties props = reader.getProperties();

        return new SecretCredentials(UUID.fromString(props.get("applicationId").toString()),
                UUID.fromString(props.get("homeTenantId").toString()),
                props.get("secret").toString());
    }

    @Override
    public TokenCredential getAzureCreds(final UUID tenantId) {
        return new ClientSecretCredentialBuilder()
                .clientId(this.applicationId.toString())
                .clientSecret(this.secret)
                .tenantId(tenantId.toString())
                .build();
    }
}





