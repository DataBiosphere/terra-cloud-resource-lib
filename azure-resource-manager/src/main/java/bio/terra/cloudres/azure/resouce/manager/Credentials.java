package bio.terra.cloudres.azure.resouce.manager;

import java.util.UUID;

/** Information for authenticating the TDR service against user Azure tenants */
public abstract class Credentials {
    // The unique UUID of the TDR application
    public UUID applicationId;
    // The UUID of the tenant to which the application belongs
    public UUID homeTenantId;

    Credentials(UUID applicationId, UUID homeTenantId) {
        this.applicationId = applicationId;
        this.homeTenantId = homeTenantId;
    }

    public UUID getApplicationId() {
        return applicationId;
    }

    public void setApplicationId(UUID applicationId) {
        this.applicationId = applicationId;
    }

    public UUID getHomeTenantId() {
        return homeTenantId;
    }

    public void setHomeTenantId(UUID homeTenantId) {
        this.homeTenantId = homeTenantId;
    }
}
