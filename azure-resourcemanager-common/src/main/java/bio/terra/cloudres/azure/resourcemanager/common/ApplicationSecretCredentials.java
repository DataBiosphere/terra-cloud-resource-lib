package bio.terra.cloudres.azure.resourcemanager.common;

import com.azure.core.credential.TokenCredential;
import com.azure.identity.ClientSecretCredentialBuilder;
import java.util.UUID;

/** Credentials for an Azure application in the home (Terra) tenant. */
public class ApplicationSecretCredentials {
  // The unique UUID of the application trying to authenticate
  private final UUID applicationId;
  // The UUID of the tenant to which the application belongs (e.g. the Terra tenant)
  private final UUID homeTenantId;
  // A valid and current secret (e.g. application password) for the application
  private final String secret;

  public ApplicationSecretCredentials(UUID applicationId, UUID homeTenantId, String secret) {
    this.applicationId = applicationId;
    this.homeTenantId = homeTenantId;
    this.secret = secret;
  }

  public TokenCredential getTokenCredential() {
    return new ClientSecretCredentialBuilder()
        .clientId(this.applicationId.toString())
        .clientSecret(this.secret)
        .tenantId(this.homeTenantId.toString())
        .build();
  }
}
