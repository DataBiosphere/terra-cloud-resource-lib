package bio.terra.cloudres.common.cleanup;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.mockito.Mockito.*;

import bio.terra.cloudres.common.ClientConfig;
import bio.terra.cloudres.testing.IntegrationCredentials;
import bio.terra.janitor.model.CloudResourceUid;
import bio.terra.janitor.model.CreateResourceRequestBody;
import bio.terra.janitor.model.GoogleBucketUid;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jdk8.Jdk8Module;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.google.api.core.ApiFutures;
import com.google.auth.oauth2.ServiceAccountCredentials;
import com.google.cloud.pubsub.v1.Publisher;
import com.google.pubsub.v1.PubsubMessage;
import java.time.Clock;
import java.time.Duration;
import java.time.OffsetDateTime;
import java.time.ZoneId;
import java.util.List;
import java.util.stream.Collectors;
import org.hamcrest.Matchers;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;

@Tag("integration")
public class CleanupRecorderTest {
  @Mock private final Publisher mockPublisher = mock(Publisher.class);

  private final ArgumentCaptor<PubsubMessage> messageArgumentCaptor =
      ArgumentCaptor.forClass(PubsubMessage.class);

  private static final String CLIENT_NAME = "crl-test";
  private static final String CLEANUP_ID = "CleanupRecorderTest";
  private static final String TOPIC_NAME = "test-topic";
  private static final String PROJECT_ID = "test-project";
  private static final int TTL_MIN = 1;
  private static final OffsetDateTime CREATION = OffsetDateTime.now(ZoneId.systemDefault());
  private static final OffsetDateTime EXPIRATION = CREATION.plusMinutes(TTL_MIN);
  private static final ServiceAccountCredentials CREDENTIALS =
      IntegrationCredentials.getAdminGoogleCredentialsOrDie();

  private static final CleanupConfig CLEANUP_CONFIG =
      CleanupConfig.builder()
          .setCleanupId(CLEANUP_ID)
          .setTimeToLive(Duration.ofMinutes(TTL_MIN))
          .setCredentials(CREDENTIALS)
          .setJanitorProjectId(PROJECT_ID)
          .setJanitorTopicName(TOPIC_NAME)
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

  private static final CreateResourceRequestBody MESSAGE_BODY =
      new CreateResourceRequestBody()
          .creation(CREATION)
          .expiration(EXPIRATION)
          .putLabelsItem("client", CLIENT_NAME)
          .putLabelsItem("cleanupId", CLEANUP_ID);

  private ObjectMapper objectMapper =
      new ObjectMapper()
          .registerModule(new Jdk8Module())
          .registerModule(new JavaTimeModule())
          .configure(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS, false);

  @BeforeEach
  private void setup() throws Exception {
    CleanupRecorder.providePublisher(mockPublisher);
    CleanupRecorder.provideClock(Clock.fixed(CREATION.toInstant(), ZoneId.systemDefault()));
    when(mockPublisher.publish(any(PubsubMessage.class)))
        .thenReturn(ApiFutures.immediateFuture("123"));
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
  public void recordWithJanitorApiCInvoked() throws Exception {
    CleanupRecorder.record(RESOURCE_1, CLIENT_CONFIG);
    CleanupRecorder.record(RESOURCE_2, CLIENT_CONFIG);
    CleanupRecorder.record(RESOURCE_3, CLIENT_CONFIG);

    verify(mockPublisher, times(3)).publish(messageArgumentCaptor.capture());

    assertThat(
        messageArgumentCaptor.getAllValues().stream()
            .map(m -> m.getData().toStringUtf8())
            .collect(Collectors.toList()),
        Matchers.containsInAnyOrder(
            objectMapper.writeValueAsString(MESSAGE_BODY.resourceUid(RESOURCE_1)),
            objectMapper.writeValueAsString(MESSAGE_BODY.resourceUid(RESOURCE_2)),
            objectMapper.writeValueAsString(MESSAGE_BODY.resourceUid(RESOURCE_3))));
  }
}
