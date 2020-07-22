package bio.terra.cloudres.common.cleanup;

import bio.terra.janitor.model.CloudResourceUid;
import com.google.common.annotations.VisibleForTesting;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/** An interface for recording created cloud resources for cleanup. */
public class CleanupRecorder {
  private CleanupRecorder() {}

  private static TestRecord testRecord = new TestRecord();

  public static void record(CloudResourceUid resource, Optional<CleanupConfig> cleanupConfig) {
    if (!cleanupConfig.isPresent()) {
      return;
    }
    testRecord.add(resource);
    // TODO(CA-874): Implement the actual recording.
  }

  /**
   * Returns a list that will be added to by future calls of {@link #record} until a new record is
   * set. Only to be used for testing. This grows unbounded in memory.
   */
  @VisibleForTesting
  public static List<CloudResourceUid> startNewRecordForTesting() {
    return testRecord.startNewRecord();
  }

  /** Helper class for recording resources in memory for testing. */
  private static class TestRecord {
    private List<CloudResourceUid> resources;
    private boolean recording = false;

    public void add(CloudResourceUid resource) {
      if (!recording) {
        return;
      }
      resources.add(resource);
    }

    public List<CloudResourceUid> startNewRecord() {
      recording = true;
      resources = new ArrayList<>();
      return resources;
    }
  }
}
