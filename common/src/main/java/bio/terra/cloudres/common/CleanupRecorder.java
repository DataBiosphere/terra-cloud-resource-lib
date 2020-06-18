package bio.terra.cloudres.common;

import bio.terra.cloudres.resources.CloudResourceUid;

/** An interface for recording created cloud resources for cleanup. */
interface CleanupRecorder {
  void record(CloudResourceUid resource);
}
