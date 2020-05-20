package bio.terra.cloudres.google.common;

import static com.google.common.base.Preconditions.checkNotNull;

import com.google.api.gax.retrying.RetrySettings;

/**
 * Config clas to manage Google resources.
 *
 * <p>It contains:
 *
 * <ul>
 *   <li>client(required): The client which is using CRL
 * </ul>
 */
public class GoogleClientConfig {
  private final String client;

  // The default retry settings if not present.
  private static final RetrySettings DEFAULT_RETRY_SETTINGS = RetrySettings.newBuilder().build();

  private GoogleClientConfig(String client) {
    checkNotNull(client, "client name must be set");

    this.client = client;
  }

  /** Gets the client name from the config. */
  public String getClient() {
    return client;
  }

  public static class Builder {
    private Builder() {}

    private String client;

    public Builder setClient(String client) {
      this.client = client;
      return this;
    }

    /** Builder for {@link GoogleClientConfig}. */
    public static Builder newBuilder() {
      return new Builder();
    }

    public GoogleClientConfig build() {
      return new GoogleClientConfig(this.client);
    }
  }
}
