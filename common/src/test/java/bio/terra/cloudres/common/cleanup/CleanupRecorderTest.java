package bio.terra.cloudres.common.cleanup;

import bio.terra.cloudres.common.ClientConfig;
import bio.terra.janitor.model.CloudResourceUid;
import bio.terra.janitor.model.GoogleBucketUid;
import org.hamcrest.Matchers;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import java.time.Duration;
import java.util.List;

import static org.hamcrest.MatcherAssert.assertThat;

@Tag("unit")
public class CleanupRecorderTest {
  private static final ClientConfig CLIENT_CONFIG = ClientConfig.Builder.newBuilder()
                .setClient("clientName")
          .setCleanupConfig(CleanupConfig.builder()
                  .setCleanupId("CleanupRecorderTest")
                  .setTimeToLive(Duration.ofMinutes(1))
                  .build())
          .build();

  private static final CloudResourceUid RESOURCE_1 =
      new CloudResourceUid().googleBucketUid(new GoogleBucketUid().bucketName("1"));
  private static final CloudResourceUid RESOURCE_2 =
      new CloudResourceUid().googleBucketUid(new GoogleBucketUid().bucketName("2"));
  private static final CloudResourceUid RESOURCE_3 =
      new CloudResourceUid().googleBucketUid(new GoogleBucketUid().bucketName("3"));

  private CleanupRecorder recorder = new CleanupRecorder(CLIENT_CONFIG);
  private MockJanitorService mockJanitorService;

  @BeforeClass
  public void setUp() {
    mockJanitorService = new MockJanitorService();
    mockJanitorService.setup();
  }

  @AfterClass
  public void tearDown() {
    mockJanitorService.stop();
  }

  @Test
  public void recordsForTestingOnlyAfterStart() {
    recorder.record(RESOURCE_1);

    List<CloudResourceUid> record = CleanupRecorder.startNewRecordForTesting();
    recorder.record(RESOURCE_2);
    recorder.record(RESOURCE_3);

    assertThat(record, Matchers.contains(RESOURCE_2, RESOURCE_3));
  }

//  @Test
//  public void recordsForTestingOnlyWithCleanupConfig() {
//    List<CloudResourceUid> record = CleanupRecorder.startNewRecordForTesting();
//    CleanupRecorder.record(RESOURCE_1, Optional.of(CLEANUP_CONFIG));
//    CleanupRecorder.record(RESOURCE_2, Optional.empty());
//
//    assertThat(record, Matchers.contains(RESOURCE_1));
//  }
//
//  @Test
//  public void recordsForTestingAreDistinct() {
//    List<CloudResourceUid> record1 = CleanupRecorder.startNewRecordForTesting();
//    CleanupRecorder.record(RESOURCE_1, Optional.of(CLEANUP_CONFIG));
//    List<CloudResourceUid> record2 = CleanupRecorder.startNewRecordForTesting();
//    CleanupRecorder.record(RESOURCE_2, Optional.of(CLEANUP_CONFIG));
//
//    assertThat(record1, Matchers.contains(RESOURCE_1));
//    assertThat(record2, Matchers.contains(RESOURCE_2));
//  }
}
