package bio.terra.cloudres.azure.resouce.manager;

import java.io.IOException;
import java.util.UUID;

import com.azure.core.credential.TokenCredential;

public interface ICredentials {
    /**
     * Given a user tenant Id, return Azure credentials
     *
     * @param tenantId The ID of a user tenant
     * @return A credential object that can be used to interact with Azure apis
     */
     TokenCredential getAzureCreds(final UUID tenantId);

    UUID getHomeTenantId();
}
