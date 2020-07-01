package bio.terra.cloudres.common.cleanup;

import static org.junit.Assert.assertTrue;

import bio.terra.cloudres.resources.CloudResourceUid;
import bio.terra.cloudres.resources.GoogleBucketUid;
import java.time.Duration;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

@Tag("unit")
public class InMemoryCleanupRecorderTest {
  private static final CloudResourceUid RESOURCE_UID = new GoogleBucketUid().bucketName("foo");
  private static final CleanupConfig CLEANUP_CONFIG =
      CleanupConfig.builder().setCleanupId("test").setTimeToLive(Duration.ofHours(2)).build();

  @Test
  public void resourcePassedThrough() {
    InMemoryCleanupRecorder inner = new InMemoryCleanupRecorder(new NullCleanupRecorder());
    InMemoryCleanupRecorder outer = new InMemoryCleanupRecorder(inner);

    outer.record(RESOURCE_UID, CLEANUP_CONFIG);

    assertTrue(outer.hasRecord(RESOURCE_UID));
    assertTrue(inner.hasRecord(RESOURCE_UID));
  }
}
