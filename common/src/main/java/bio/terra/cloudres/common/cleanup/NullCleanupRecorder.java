package bio.terra.cloudres.common.cleanup;

import bio.terra.cloudres.resources.CloudResourceUid;

/** Null object for {@link CleanupRecorder} to do nothing. */
public class NullCleanupRecorder implements CleanupRecorder {
  @Override
  public void record(CloudResourceUid resource, CleanupConfig cleanupConfig) {
    // do nothing.
  }
}
