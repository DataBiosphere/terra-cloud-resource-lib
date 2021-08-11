package bio.terra.cloudres.azure.resouce.manager;

import com.azure.core.credential.TokenCredential;
import com.azure.identity.UsernamePasswordCredentialBuilder;

import java.io.IOException;
import java.util.Properties;
import java.util.UUID;

public class UserNamePasswordCredentials extends Credentials implements ICredentials {
    public String username;
    public String password;

    UserNamePasswordCredentials(UUID applicationId, UUID homeTenantId, String username, String password) {
        super(applicationId, homeTenantId);
        this.username = username;
        this.password = password;
    }


    public static UserNamePasswordCredentials getFromPropsFile(String fileName) throws IOException {
        PropertyReader reader = new PropertyReader(fileName);
        Properties props = reader.getProperties();

        return new UserNamePasswordCredentials(UUID.fromString(props.get("applicationId").toString()),
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
