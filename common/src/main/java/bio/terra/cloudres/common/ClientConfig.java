package bio.terra.cloudres.common;

import static com.google.common.base.Preconditions.checkNotNull;

import bio.terra.cloudres.common.cleanup.CleanupConfig;
import bio.terra.cloudres.common.cleanup.CleanupRecorderLocator;
import bio.terra.cloudres.resources.CloudResourceUid;
import java.util.Optional;

/** Configuration class to manage CRL behavior. */
public class ClientConfig {
  private final String clientName;
  private final Optional<CleanupConfig> cleanupConfig;

  private ClientConfig(String clientName, Optional<CleanupConfig> cleanupConfig) {
    checkNotNull(clientName, "client name must be set");

    this.clientName = clientName;
    this.cleanupConfig = cleanupConfig;
  }

  /** The name of the client running CRL, e.g. the name of the service. */
  public String getClientName() {
    return clientName;
  }

  /**
   * The {@link CleanupConfig} of how created resources should be cleaned up, or empty if not
   * running in cleanup mode.
   */
  public Optional<CleanupConfig> getCleanupConfig() {
    return cleanupConfig;
  }

  /** Record a cloud resource to be created for cleanup, if running in cleanup mode. */
  public void recordForCleanup(CloudResourceUid resource) {
    getCleanupConfig().ifPresent(cleanup -> CleanupRecorderLocator.get().record(resource, cleanup));
  }

  public static class Builder {
    private String client;
    private Optional<CleanupConfig> cleanupConfig = Optional.empty();

    private Builder() {}

    /** Builder for {@link ClientConfig}. */
    public static Builder newBuilder() {
      return new Builder();
    }

    /** required, sets the client which is using CRL */
    public Builder setClient(String client) {
      this.client = client;
      return this;
    }

    public Builder setCleanupConfig(CleanupConfig cleanupConfig) {
      this.cleanupConfig = Optional.of(cleanupConfig);
      return this;
    }

    public ClientConfig build() {
      return new ClientConfig(this.client, cleanupConfig);
    }
  }
}
