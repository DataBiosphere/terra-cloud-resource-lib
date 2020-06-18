package bio.terra.cloudres.common;

import static com.google.common.base.Preconditions.checkNotNull;

import bio.terra.cloudres.common.cleanup.CleanupConfig;
import bio.terra.cloudres.common.cleanup.CleanupRecorder;
import bio.terra.cloudres.common.cleanup.NullCleanupRecorder;
import bio.terra.cloudres.resources.CloudResourceUid;
import java.util.Optional;

/** Configuration class to manage CRL behavior. */
public class ClientConfig {
  private final String clientName;
  private final Optional<CleanupConfig> cleanupConfig;
  private final CleanupRecorder cleanupRecorder;

  private ClientConfig(
      String clientName, Optional<CleanupConfig> cleanupConfig, CleanupRecorder recorder) {
    cleanupRecorder = recorder;
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

  /**
   * The {@link CleanupRecorder} to use to record resources for clenaup, if running in cleanup mode.
   */
  public CleanupRecorder getCleanupRecorder() {
    return cleanupRecorder;
  }

  /** Record a cloud resource to be created for cleanup, if running in cleanup mode. */
  public void recordForCleanup(CloudResourceUid resource) {
    if (!getCleanupConfig().isPresent()) {
      return;
    }
    getCleanupRecorder().record(resource, getCleanupConfig().get());
  }

  public static class Builder {
    private String client;
    private Optional<CleanupConfig> cleanupConfig = Optional.empty();
    private CleanupRecorder cleanupRecorder = new NullCleanupRecorder();

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

    public Builder setCleanupRecorder(CleanupRecorder recorder) {
      this.cleanupRecorder = recorder;
      return this;
    }

    public ClientConfig build() {
      return new ClientConfig(this.client, cleanupConfig, cleanupRecorder);
    }
  }
}
