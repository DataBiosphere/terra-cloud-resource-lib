package bio.terra.cloudres.common.cleanup;

import com.google.auto.value.AutoValue;
import java.time.Duration;

/**
 * Configuration for CRL running in cleanup mode, where it tracks cloud resources being created and
 * cleans them up later.
 *
 * <p>TODO(CA-867): Write a README and link it here explaining cleanup mode.
 */
@AutoValue
public abstract class CleanupConfig {

  /** An id to differentiate this run of CRL in cleanup mode from other runs. Any format allowed. */
  public abstract String cleanupId();

  /** How long created resources should live (at least) before being cleaned up. */
  public abstract Duration timeToLive();

  public static Builder builder() {
    return new AutoValue_CleanupConfig.Builder();
  }

  /** Builder for {@link CleanupConfig}. */
  @AutoValue.Builder
  public abstract static class Builder {
    public abstract Builder setCleanupId(String value);

    public abstract Builder setTimeToLive(Duration value);

    public abstract CleanupConfig build();
  }
}
