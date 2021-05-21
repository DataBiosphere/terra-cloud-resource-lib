package bio.terra.cloudres.common.cleanup;

import bio.terra.cloudres.common.ClientConfig;
import bio.terra.cloudres.common.JanitorException;
import bio.terra.janitor.model.CloudResourceUid;
import bio.terra.janitor.model.CreateResourceRequestBody;
import bio.terra.janitor.model.ResourceMetadata;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jdk8.Jdk8Module;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.google.api.core.ApiFuture;
import com.google.api.gax.core.FixedCredentialsProvider;
import com.google.cloud.pubsub.v1.Publisher;
import com.google.common.annotations.VisibleForTesting;
import com.google.protobuf.ByteString;
import com.google.pubsub.v1.PubsubMessage;
import com.google.pubsub.v1.TopicName;
import java.io.IOException;
import java.time.Clock;
import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutionException;
import javax.annotation.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/** An interface for recording created cloud resources for cleanup. */
public class CleanupRecorder {
  private CleanupRecorder() {}

  private static final Logger logger = LoggerFactory.getLogger(CleanupRecorder.class);

  private static TestRecord testRecord = new TestRecord();
  private static Publisher publisher = null;
  private static Clock clock = Clock.systemUTC();

  public static void record(CloudResourceUid resource, ClientConfig clientConfig) {
    record(resource, null, clientConfig);
  }

  public static void record(
      CloudResourceUid resource, @Nullable ResourceMetadata metadata, ClientConfig clientConfig) {
    if (!clientConfig.getCleanupConfig().isPresent()) {
      return;
    }

    publish(resource, metadata, clientConfig);
    testRecord.add(resource);
  }

  /**
   * Returns a list that will be added to by future calls of {@link #record} until a new record is
   * set. Only to be used for testing. This grows unbounded in memory.
   */
  @VisibleForTesting
  public static List<CloudResourceUid> startNewRecordForTesting() {
    return testRecord.startNewRecord();
  }

  /** Provides an {@link Publisher}. */
  @VisibleForTesting
  static void providePublisher(Publisher newPublisher) {
    publisher = newPublisher;
  }

  /** Provides an {@link Clock} for easy testing. */
  @VisibleForTesting
  static void provideClock(Clock newClock) {
    clock = newClock;
  }

  private static void publish(
      CloudResourceUid resource, @Nullable ResourceMetadata metadata, ClientConfig clientConfig) {
    CleanupConfig cleanupConfig = clientConfig.getCleanupConfig().get();
    if (publisher == null) {
      // Provide a new publisher if not present.
      TopicName topicName =
          TopicName.of(cleanupConfig.janitorProjectId(), cleanupConfig.janitorTopicName());
      try {
        providePublisher(
            Publisher.newBuilder(topicName)
                .setCredentialsProvider(
                    FixedCredentialsProvider.create(cleanupConfig.credentials()))
                .build());
      } catch (IOException e) {
        throw new JanitorException("Failed to initialize Janitor pubsub publisher.", e);
      }
    }

    ObjectMapper objectMapper =
        new ObjectMapper()
            .registerModule(new Jdk8Module())
            .registerModule(new JavaTimeModule())
            .configure(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS, false);

    OffsetDateTime now = OffsetDateTime.now(clock);
    CreateResourceRequestBody body =
        new CreateResourceRequestBody()
            .resourceUid(resource)
            .resourceMetadata(metadata)
            .creation(now)
            .expiration(now.plus(cleanupConfig.timeToLive()))
            .putLabelsItem("client", clientConfig.getClientName())
            .putLabelsItem("cleanupId", cleanupConfig.cleanupId());

    ByteString data;
    try {
      data = ByteString.copyFromUtf8(objectMapper.writeValueAsString(body));
    } catch (IOException e) {
      throw new JanitorException(
          String.format("Failed to serialize CreateResourceRequestBody: [%s]", body), e);
    }

    ApiFuture<String> messageIdFuture =
        publisher.publish(PubsubMessage.newBuilder().setData(data).build());
    try {
      String messageId = messageIdFuture.get();
      logger.debug("Publish message to Janitor track resource " + messageId);
    } catch (InterruptedException | ExecutionException e) {
      throw new JanitorException(
          String.format("Failed to publish message: [%s] ", data.toString()), e);
    }
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
