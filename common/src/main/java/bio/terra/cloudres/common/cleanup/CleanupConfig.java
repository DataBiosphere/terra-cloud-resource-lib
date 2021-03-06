package bio.terra.cloudres.common.cleanup;

import com.google.auth.oauth2.GoogleCredentials;
import com.google.auto.value.AutoValue;
import java.time.Duration;

/**
 * Configuration for CRL running in cleanup mode, where it tracks cloud resources being created and
 * cleans them up later.
 *
 * <p>See the README on Cleanup Mode.
 */
@AutoValue
public abstract class CleanupConfig {

  /**
   * An id to differentiate this run of CRL in cleanup mode from other runs. Any format allowed.
   *
   * <p>Each integration test run could be given a different cleanup id to allow resources from each
   * run to be distinguished from each other.
   */
  public abstract String cleanupId();

  /** How long created resources should live (at least) before being cleaned up. */
  public abstract Duration timeToLive();

  /**
   * The Google's {@link GoogleCredentials} for accessing Janitor service. CRL refresh/generate
   * access token using this credential.
   *
   * <p>Current Janitor only supports Google Credentials, and the most common usage would be a
   * {@link com.google.auth.oauth2.ServiceAccountCredentials}
   */
  public abstract GoogleCredentials credentials();

  /** The Janitor pub/sub topic name */
  public abstract String janitorTopicName();

  /** The Janitor pub/sub project id */
  public abstract String janitorProjectId();

  public static Builder builder() {
    return new AutoValue_CleanupConfig.Builder();
  }

  /** Builder for {@link CleanupConfig}. */
  @AutoValue.Builder
  public abstract static class Builder {
    public abstract Builder setCleanupId(String value);

    public abstract Builder setTimeToLive(Duration value);

    public abstract Builder setCredentials(GoogleCredentials value);

    public abstract Builder setJanitorTopicName(String value);

    public abstract Builder setJanitorProjectId(String value);

    public abstract CleanupConfig build();
  }
}
