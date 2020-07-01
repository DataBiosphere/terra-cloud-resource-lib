package bio.terra.cloudres.common.cleanup;

import static org.hamcrest.MatcherAssert.assertThat;

import bio.terra.cloudres.resources.CloudResourceUid;
import bio.terra.cloudres.resources.GoogleBucketUid;
import java.time.Duration;
import java.util.List;
import java.util.Optional;
import org.hamcrest.Matchers;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

@Tag("unit")
public class CleanupRecorderTest {
  private static final CleanupConfig CLEANUP_CONFIG =
      CleanupConfig.builder()
          .setCleanupId("CleanupRecorderTest")
          .setTimeToLive(Duration.ofMinutes(1))
          .build();

  private static final CloudResourceUid RESOURCE_1 = new GoogleBucketUid().bucketName("1");
  private static final CloudResourceUid RESOURCE_2 = new GoogleBucketUid().bucketName("2");
  private static final CloudResourceUid RESOURCE_3 = new GoogleBucketUid().bucketName("3");

  @Test
  public void recordsForTestingOnlyAfterStart() {
    CleanupRecorder.record(RESOURCE_1, Optional.of(CLEANUP_CONFIG));

    List<CloudResourceUid> record = CleanupRecorder.startNewRecordForTesting();
    CleanupRecorder.record(RESOURCE_2, Optional.of(CLEANUP_CONFIG));
    CleanupRecorder.record(RESOURCE_3, Optional.of(CLEANUP_CONFIG));

    assertThat(record, Matchers.contains(RESOURCE_2, RESOURCE_3));
  }

  @Test
  public void recordsForTestingOnlyWithCleanupConfig() {
    List<CloudResourceUid> record = CleanupRecorder.startNewRecordForTesting();
    CleanupRecorder.record(RESOURCE_1, Optional.of(CLEANUP_CONFIG));
    CleanupRecorder.record(RESOURCE_2, Optional.empty());

    assertThat(record, Matchers.contains(RESOURCE_1));
  }

  @Test
  public void recordsForTestingAreDistinct() {
    List<CloudResourceUid> record1 = CleanupRecorder.startNewRecordForTesting();
    CleanupRecorder.record(RESOURCE_1, Optional.of(CLEANUP_CONFIG));
    List<CloudResourceUid> record2 = CleanupRecorder.startNewRecordForTesting();
    CleanupRecorder.record(RESOURCE_2, Optional.of(CLEANUP_CONFIG));

    assertThat(record1, Matchers.contains(RESOURCE_1));
    assertThat(record2, Matchers.contains(RESOURCE_2));
  }
}
