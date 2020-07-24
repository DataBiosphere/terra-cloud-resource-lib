package bio.terra.cloudres.common.cleanup;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.*;

import bio.terra.cloudres.common.ClientConfig;
import bio.terra.cloudres.testing.IntegrationCredentials;
import bio.terra.janitor.ApiClient;
import bio.terra.janitor.model.CloudResourceUid;
import bio.terra.janitor.model.CreateResourceRequestBody;
import bio.terra.janitor.model.GoogleBucketUid;
import com.google.auth.oauth2.ServiceAccountCredentials;
import java.time.Duration;
import java.util.List;
import org.hamcrest.Matchers;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

@Tag("unit")
public class CleanupRecorderTest {
  private static final String JANITOR_PATH = "http://1.1.1.0";
  private static final String CLIENT_NAME = "crl-test";
  private static final String CLEANUP_ID = "CleanupRecorderTest";
  private static final int TTL_MIN = 1;
  private static final ServiceAccountCredentials CREDENTIALS =
      IntegrationCredentials.getAdminGoogleCredentialsOrDie();

  private static final CleanupConfig CLEANUP_CONFIG =
      CleanupConfig.builder()
          .setCleanupId(CLEANUP_ID)
          .setTimeToLive(Duration.ofMinutes(TTL_MIN))
          .setCredentials(CREDENTIALS)
          .setJanitorBasePath(JANITOR_PATH)
          .build();
  private static final ClientConfig CLIENT_CONFIG =
      ClientConfig.Builder.newBuilder()
          .setClient(CLIENT_NAME)
          .setCleanupConfig(CLEANUP_CONFIG)
          .build();

  private static final CloudResourceUid RESOURCE_1 =
      new CloudResourceUid().googleBucketUid(new GoogleBucketUid().bucketName("1"));
  private static final CloudResourceUid RESOURCE_2 =
      new CloudResourceUid().googleBucketUid(new GoogleBucketUid().bucketName("2"));
  private static final CloudResourceUid RESOURCE_3 =
      new CloudResourceUid().googleBucketUid(new GoogleBucketUid().bucketName("3"));

  private ApiClient spyApiClient = spy(new ApiClient());

  @BeforeEach
  private void setup() throws Exception {
    // An ugly 10 parameters method.
    doReturn(null)
        .when(spyApiClient)
        .invokeAPI(
            anyString(), anyString(), any(), any(), any(), any(), any(), any(), any(), any());
    CleanupRecorder.provideApiClient(spyApiClient);
  }

  @Test
  public void recordsForTestingOnlyAfterStart() {
    CleanupRecorder.record(RESOURCE_1, CLIENT_CONFIG);

    List<CloudResourceUid> record = CleanupRecorder.startNewRecordForTesting();
    CleanupRecorder.record(RESOURCE_2, CLIENT_CONFIG);
    CleanupRecorder.record(RESOURCE_3, CLIENT_CONFIG);

    assertThat(record, Matchers.contains(RESOURCE_2, RESOURCE_3));
  }

  @Test
  public void recordsForTestingOnlyWithCleanupConfig() {
    List<CloudResourceUid> record = CleanupRecorder.startNewRecordForTesting();
    CleanupRecorder.record(RESOURCE_1, CLIENT_CONFIG);
    CleanupRecorder.record(
        RESOURCE_2, ClientConfig.Builder.newBuilder().setClient("crl-test").build());

    assertThat(record, Matchers.contains(RESOURCE_1));
  }

  @Test
  public void recordsForTestingAreDistinct() {
    List<CloudResourceUid> record1 = CleanupRecorder.startNewRecordForTesting();
    CleanupRecorder.record(RESOURCE_1, CLIENT_CONFIG);
    List<CloudResourceUid> record2 = CleanupRecorder.startNewRecordForTesting();
    CleanupRecorder.record(RESOURCE_2, CLIENT_CONFIG);

    assertThat(record1, Matchers.contains(RESOURCE_1));
    assertThat(record2, Matchers.contains(RESOURCE_2));
  }

  @Test
  public void recordWithJanitorApiCInvoked() {
    CleanupRecorder.record(RESOURCE_1, CLIENT_CONFIG);
    CleanupRecorder.record(RESOURCE_2, CLIENT_CONFIG);
    CleanupRecorder.record(RESOURCE_3, CLIENT_CONFIG);

    // All record are captured.
    assertApiClientSet(3);
  }

  @Test
  public void createJanitorResource() {
    assertEquals(
        new CreateResourceRequestBody()
            .resourceUid(RESOURCE_1)
            .timeToLiveInMinutes(TTL_MIN)
            .putLabelsItem("client", CLIENT_NAME)
            .putLabelsItem("cleanupId", CLEANUP_ID),
        CleanupRecorder.createJanitorResource(RESOURCE_1, CLIENT_CONFIG));
  }

  private void assertApiClientSet(int times) {
    verify(spyApiClient, times(times)).setBasePath(JANITOR_PATH);
    verify(spyApiClient, times(times)).setAccessToken(anyString());
  }
}
