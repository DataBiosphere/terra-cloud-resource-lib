package bio.terra.cloudres.azure.resouce.manager;

import java.io.IOException;
import java.util.Properties;
import java.util.UUID;

/** Information for authenticating the TDR service against user Azure tenants */
public class Credentials {
    // The unique UUID of the TDR application
    public UUID applicationId;
    // A valid and current secret (e.g. application password) for the TDR application
    public String secret;
    // The UUID of the tenant to which the application belongs
    public UUID homeTenantId;

    private Credentials(UUID applicationId, String secret,  UUID homeTenantId) {
        this.applicationId = applicationId;
        this.secret = secret;
        this.homeTenantId = homeTenantId;
    }

    public UUID getApplicationId() {
        return applicationId;
    }

    public void setApplicationId(UUID applicationId) {
        this.applicationId = applicationId;
    }

    public String getSecret() {
        return secret;
    }

    public void setSecret(String secret) {
        this.secret = secret;
    }

    public UUID getHomeTenantId() {
        return homeTenantId;
    }

    public void setHomeTenantId(UUID homeTenantId) {
        this.homeTenantId = homeTenantId;
    }

    public static Credentials getFromPropsFile(String fileName)  throws IOException {
        PropertyReader reader = new PropertyReader(fileName);
        Properties props = reader.getProperties();
//            secret=stuff
//            applicationId=stuff
//            homeTenantId

        return new Credentials(UUID.fromString(props.get("applicationId").toString()),
            props.get("secret").toString(),
            UUID.fromString(props.get("homeTenantId").toString())
        );
    }
}
