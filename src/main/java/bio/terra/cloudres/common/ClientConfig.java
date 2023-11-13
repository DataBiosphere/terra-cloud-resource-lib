package bio.terra.cloudres.common;

import static com.google.common.base.Preconditions.checkNotNull;

import bio.terra.cloudres.common.cleanup.CleanupConfig;
import io.opentelemetry.api.OpenTelemetry;

import java.util.Optional;

/** Configuration class to manage CRL behavior. */
public class ClientConfig {
  private final String clientName;
  private final Optional<CleanupConfig> cleanupConfig;
  private final OpenTelemetry openTelemetry;

  private ClientConfig(String clientName, Optional<CleanupConfig> cleanupConfig, OpenTelemetry openTelemetry) {
    checkNotNull(clientName, "client name must be set");

    this.clientName = clientName;
    this.cleanupConfig = cleanupConfig;
    this.openTelemetry = openTelemetry;
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

  public OpenTelemetry getOpenTelemetry() {
    return openTelemetry;
  }

  public static class Builder {
    private String client;
    private Optional<CleanupConfig> cleanupConfig = Optional.empty();
    private OpenTelemetry openTelemetry = OpenTelemetry.noop();

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

    public Builder setOpenTelemetry(OpenTelemetry openTelemetry) {
      this.openTelemetry = openTelemetry;
      return this;
    }

    public ClientConfig build() {
      return new ClientConfig(this.client, cleanupConfig, openTelemetry);
    }
  }
}
