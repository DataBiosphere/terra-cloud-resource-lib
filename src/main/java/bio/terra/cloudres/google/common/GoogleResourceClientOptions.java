package bio.terra.cloudres.google.common;

import com.google.api.gax.retrying.RetrySettings;
import com.google.auth.Credentials;

import static com.google.common.base.Preconditions.checkNotNull;

/** Helper class to hold all configs to use Terra CRL to manage Google resources. */
public class GoogleResourceClientOptions {
    private final Credentials credentials;
    private final RetrySettings retrySettings;
    private final String client;

    // The default retry settings if not present.
    private static final RetrySettings DEFAULT_RETRY_SETTINGS = RetrySettings.newBuilder().build();

    private GoogleResourceClientOptions(GoogleResourceClientOptions.Builder builder) {
        checkNotNull(builder.credentials, "credential must be set");
        checkNotNull(builder.client, "client name must be set");

        this.credentials = builder.credentials;
        this.client = builder.client;

        // Use the default one if not present.
        this.retrySettings = builder.retrySettings == null ? DEFAULT_RETRY_SETTINGS: builder.retrySettings;
    }

    /** Gets the credential from the config. */
    public Credentials getCredential() {
        checkNotNull(credentials);
        return credentials;
    }

    /** Gets the client name from the config. */
    public String getClient() {
        return client;
    }

    /** Gets the RetrySettings from the config. */
    public RetrySettings getRetrySettings() {
        return retrySettings;
    }

    public static class Builder {
        private Builder() {}
        private Credentials credentials;

        private String client;
        private RetrySettings retrySettings;

        private Builder self() {
            return this;
        }

        /**
         * Sets configuration parameters for request retries.
         *
         * @return the builder
         */
        public Builder setRetrySettings(RetrySettings retrySettings) {
            this.retrySettings = retrySettings;
            return self();
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

        /**
         * Sets credential.
         *
         * @return the builder
         */
        public Builder setCredential(Credentials credentials) {
            this.credentials = credentials;
            return self();
        }

        public static Builder newBuilder() {
            return new Builder();
        }

        public GoogleResourceClientOptions build() {
            return new GoogleResourceClientOptions(this);
        }
    }
}
