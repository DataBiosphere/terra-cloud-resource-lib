package bio.terra.cloudres.common.cleanup;

import bio.terra.cloudres.resources.CloudResourceUid;

/** An interface for recording created cloud resources for cleanup. */
public interface CleanupRecorder {
  void record(CloudResourceUid resource, CleanupConfig cleanupConfig);
}
