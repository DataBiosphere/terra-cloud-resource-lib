package bio.terra.cloudres.common;

import static org.hamcrest.MatcherAssert.assertThat;

import bio.terra.cloudres.common.cleanup.CleanupConfig;
import bio.terra.cloudres.common.cleanup.InMemoryCleanupRecorder;
import bio.terra.cloudres.resources.GoogleBucketUid;
import bio.terra.cloudres.resources.GoogleProjectUid;
import java.time.Duration;
import org.hamcrest.Matchers;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

@Tag("unit")
public class ClientConfigTest {

  private static final CleanupConfig CLEANUP_CONFIG =
      CleanupConfig.builder().setCleanupId("bar").setTimeToLive(Duration.ofDays(1)).build();

  private static final GoogleBucketUid BUCKET_1 = new GoogleBucketUid().bucketName("bucket1");
  private static final GoogleBucketUid BUCKET_2 = new GoogleBucketUid().bucketName("bucket2");
  private static final GoogleProjectUid PROJECT_1 = new GoogleProjectUid().projectId("project1");

  @Test
  public void recordForCleanup_recordsWithCleanupConfig() {
    InMemoryCleanupRecorder recorder = new InMemoryCleanupRecorder();
    ClientConfig clientConfig =
        ClientConfig.Builder.newBuilder()
            .setClient("foo")
            .setCleanupConfig(CLEANUP_CONFIG)
            .setCleanupRecorder(recorder)
            .build();

    clientConfig.recordForCleanup(BUCKET_1);
    clientConfig.recordForCleanup(BUCKET_2);
    clientConfig.recordForCleanup(PROJECT_1);
    clientConfig.recordForCleanup(PROJECT_1);

    assertThat(recorder.getRecords(BUCKET_1), Matchers.contains(CLEANUP_CONFIG));
    assertThat(recorder.getRecords(BUCKET_2), Matchers.contains(CLEANUP_CONFIG));
    assertThat(recorder.getRecords(PROJECT_1), Matchers.contains(CLEANUP_CONFIG, CLEANUP_CONFIG));
  }

  @Test
  public void recordForCleanup_noRecordWithoutCleanupConfig() {
    InMemoryCleanupRecorder recorder = new InMemoryCleanupRecorder();
    ClientConfig clientConfig =
        ClientConfig.Builder.newBuilder().setClient("foo").setCleanupRecorder(recorder).build();

    clientConfig.recordForCleanup(BUCKET_1);
    clientConfig.recordForCleanup(BUCKET_2);

    assertThat(recorder.getRecords(BUCKET_1), Matchers.empty());
    assertThat(recorder.getRecords(BUCKET_2), Matchers.empty());
  }

  @Test
  public void recordForCleanup_noRecorderIsOk() {
    // Even though no CleanupRecorder is set on ClientConfig, we can still use the recordForCleanup
    // method.
    ClientConfig clientConfig =
        ClientConfig.Builder.newBuilder().setClient("foo").setCleanupConfig(CLEANUP_CONFIG).build();
    clientConfig.recordForCleanup(BUCKET_1);
  }
}
