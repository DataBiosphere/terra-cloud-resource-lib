package bio.terra.cloudres.common;

import static com.google.common.base.Preconditions.checkNotNull;

/** Config class to manage Google resources. */
public class ClientConfig {
  private final String clientName;

  private ClientConfig(String clientName) {
    checkNotNull(clientName, "client name must be set");

    this.clientName = clientName;
  }

  /**
   * Gets the client name from the config.
   *
   * @return the client name
   */
  public String getClientName() {
    return clientName;
  }

  public static class Builder {
    private String client;

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

    public ClientConfig build() {
      return new ClientConfig(this.client);
    }
  }
}
