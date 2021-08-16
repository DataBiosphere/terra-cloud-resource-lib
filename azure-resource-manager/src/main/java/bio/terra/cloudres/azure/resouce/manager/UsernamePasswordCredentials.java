package bio.terra.cloudres.azure.resouce.manager;

import com.azure.core.credential.TokenCredential;
import com.azure.identity.UsernamePasswordCredentialBuilder;

import java.io.IOException;
import java.util.Properties;
import java.util.UUID;

public class UsernamePasswordCredentials extends Credentials implements ICredentials {
    public String username;
    public String password;

    UsernamePasswordCredentials(UUID applicationId, UUID homeTenantId, String username, String password) {
        super(applicationId, homeTenantId);
        this.username = username;
        this.password = password;
    }


    public static UsernamePasswordCredentials getFromPropsFile(String fileName) throws IOException {
        PropertyReader reader = new PropertyReader(fileName);
        Properties props = reader.getProperties();

        return new UsernamePasswordCredentials(UUID.fromString(props.get("applicationId").toString()),
                UUID.fromString(props.get("homeTenantId").toString()),
                props.get("username").toString(),
                props.get("password").toString());
    }

    @Override
    public TokenCredential getAzureCreds(UUID tenantId) {
        return new UsernamePasswordCredentialBuilder()
                .clientId(this.applicationId.toString())
                .tenantId(tenantId.toString())
                .username(this.username)
                .password(this.password)
                .build();
    }
}
