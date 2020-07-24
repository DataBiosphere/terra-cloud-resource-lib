package bio.terra.cloudres.common.cleanup;

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
   * The client access token when calling Janitor, e.g. when using Google SA account credentials, it
   * would be {@code serviceAccountCredentials.refreshAccessToken().getTokenValue()}
   */
  public abstract String accessToken();

  /** The janitor server base path, e.g. 127.0.0.1. */
  public abstract String janitorBasePath();

  public static Builder builder() {
    return new AutoValue_CleanupConfig.Builder();
  }

  /** Builder for {@link CleanupConfig}. */
  @AutoValue.Builder
  public abstract static class Builder {
    public abstract Builder setCleanupId(String value);

    public abstract Builder setTimeToLive(Duration value);

    public abstract Builder setAccessToken(String value);

    public abstract Builder setJanitorBasePath(String value);

    public abstract CleanupConfig build();
  }
}
