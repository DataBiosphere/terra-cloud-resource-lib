package bio.terra.cloudres.azure.resouce.manager;

import com.azure.core.credential.TokenCredential;

import java.util.UUID;

/** Information for authenticating an application against user Azure tenants */
public abstract class Credentials {
    // The unique UUID of the application trying to authenticate
    private final UUID applicationId;
    // The UUID of the tenant to which the application belongs (e.g. the Terra tenant)
    private final UUID homeTenantId;

    protected Credentials(final UUID applicationId, final UUID homeTenantId) {
        this.applicationId = applicationId;
        this.homeTenantId = homeTenantId;
    }

    public final UUID getApplicationId() {
        return applicationId;
    }

    public final UUID getHomeTenantId() {
        return homeTenantId;
    }

    abstract TokenCredential getAzureCreds();
}
