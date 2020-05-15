package bio.terra.cloudres.google.common;

import com.google.api.gax.retrying.RetrySettings;

import static com.google.common.base.Preconditions.checkNotNull;

/**
 * Config class to use Terra CRL to manage Google resources.
 *
 * <p> It contains:
 * <ul>
 *     <li> client(required): The client which is using CRL
 * </ul>
 */
public class GoogleClientConfig {
    private final String client;

    // The default retry settings if not present.
    private static final RetrySettings DEFAULT_RETRY_SETTINGS = RetrySettings.newBuilder().build();

    private GoogleClientConfig(GoogleClientConfig.Builder builder) {
        checkNotNull(builder.client, "client name must be set");

        this.client = builder.client;
    }

    /**
     * Gets the client name from the config.
     */
    public String getClient() {
        return client;
    }

    public static class Builder {
        private Builder() {
        }

        private String client;

        private Builder self() {
            return this;
        }

        /**
         * Sets client name.
         *
         * @return the builder
         */
        public Builder setClient(String client) {
            this.client = client;
            return self();
        }

        public static Builder newBuilder() {
            return new Builder();
        }

        public GoogleClientConfig build() {
            return new GoogleClientConfig(this);
        }
    }
}
