package bio.terra.cloudres.common.cleanup;

import static bio.terra.cloudres.testing.MockJanitorService.SERVICE_BASE_PATH;
import static bio.terra.cloudres.testing.MockJanitorService.getDefaultAccessToken;
import static org.hamcrest.MatcherAssert.assertThat;

import bio.terra.cloudres.common.ClientConfig;
import bio.terra.cloudres.testing.MockJanitorService;
import bio.terra.janitor.model.CloudResourceUid;
import bio.terra.janitor.model.GoogleBucketUid;
import java.time.Duration;
import org.hamcrest.Matchers;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

@Tag("unit")
public class CleanupRecorderTest {
  private static final ClientConfig CLIENT_CONFIG =
      ClientConfig.Builder.newBuilder()
          .setClient("clientName")
          .setCleanupConfig(
              CleanupConfig.builder()
                  .setCleanupId("CleanupRecorderTest")
                  .setTimeToLive(Duration.ofMinutes(1))
                  .setAccessToken(getDefaultAccessToken())
                  .setJanitorBasePath(SERVICE_BASE_PATH)
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

  @BeforeEach
  public void setUp() {
    mockJanitorService = new MockJanitorService();
    mockJanitorService.setup();
  }

  @AfterEach
  public void tearDown() {
    mockJanitorService.stop();
  }

  @Test
  public void recordsAll() throws Exception {
    recorder.record(RESOURCE_1);
    recorder.record(RESOURCE_2);
    recorder.record(RESOURCE_3);

    assertThat(
        mockJanitorService.getRecordedResources(),
        Matchers.contains(RESOURCE_1, RESOURCE_2, RESOURCE_3));
  }

  @Test
  public void recordsOnlyWithCleanupConfig() throws Exception {
    CleanupRecorder noopRecorder =
        new CleanupRecorder(ClientConfig.Builder.newBuilder().setClient("123").build());
    recorder.record(RESOURCE_1);
    noopRecorder.record(RESOURCE_2);

    assertThat(mockJanitorService.getRecordedResources(), Matchers.contains(RESOURCE_1));
  }
}
