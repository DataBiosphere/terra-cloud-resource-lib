package bio.terra.cloudres.common.cleanup;

import bio.terra.cloudres.common.ClientConfig;
import bio.terra.cloudres.common.JanitorException;
import bio.terra.janitor.model.CloudResourceUid;
import bio.terra.janitor.model.CreateResourceRequestBody;
import com.google.api.core.ApiFuture;
import com.google.api.gax.core.FixedCredentialsProvider;
import com.google.cloud.pubsub.v1.Publisher;
import com.google.common.annotations.VisibleForTesting;
import com.google.gson.Gson;
import com.google.protobuf.ByteString;
import com.google.pubsub.v1.PubsubMessage;
import com.google.pubsub.v1.TopicName;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutionException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/** An interface for recording created cloud resources for cleanup. */
public class CleanupRecorder {
  private CleanupRecorder() {}

  private static final Logger logger = LoggerFactory.getLogger(CleanupRecorder.class);

  private static TestRecord testRecord = new TestRecord();
  private static Publisher publisher = null;

  public static void record(CloudResourceUid resource, ClientConfig clientConfig) {
    if (!clientConfig.getCleanupConfig().isPresent()) {
      return;
    }

    publish(resource, clientConfig);
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
  public static void providePublisher(Publisher newPublisher) {
    publisher = newPublisher;
  }

  @VisibleForTesting
  static void publish(CloudResourceUid resource, ClientConfig clientConfig) {
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
        throw new JanitorException("Failed to initialize publisher Janitor message", e);
      }
    }

    ByteString data =
        ByteString.copyFromUtf8(
            new Gson()
                .toJson(
                    new CreateResourceRequestBody()
                        .resourceUid(resource)
                        .timeToLiveInMinutes((int) cleanupConfig.timeToLive().toMinutes())
                        .putLabelsItem("client", clientConfig.getClientName())
                        .putLabelsItem("cleanupId", cleanupConfig.cleanupId())));

    ApiFuture<String> messageIdFuture =
        publisher.publish(PubsubMessage.newBuilder().setData(data).build());
    try {
      String messageId = messageIdFuture.get();
      logger.info("Publish message to Janitor track resource " + messageId);
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
