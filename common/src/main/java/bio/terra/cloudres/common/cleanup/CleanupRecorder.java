package bio.terra.cloudres.common.cleanup;

import bio.terra.cloudres.resources.CloudResourceUid;
import java.util.Optional;

/** An interface for recording created cloud resources for cleanup. */
public interface CleanupRecorder {
  void record(CloudResourceUid resource, CleanupConfig cleanupConfig);

  default void record(CloudResourceUid resource, Optional<CleanupConfig> cleanupConfig) {
    cleanupConfig.ifPresent(cleanup -> this.record(resource, cleanup));
  }
}
