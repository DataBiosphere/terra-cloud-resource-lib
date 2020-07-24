package bio.terra.cloudres.common.cleanup;

import bio.terra.cloudres.common.ClientConfig;
import bio.terra.cloudres.common.JanitorException;
import bio.terra.janitor.ApiClient;
import bio.terra.janitor.ApiException;
import bio.terra.janitor.controller.JanitorApi;
import bio.terra.janitor.model.CloudResourceUid;
import bio.terra.janitor.model.CreateResourceRequestBody;
import com.google.auth.oauth2.GoogleCredentials;
import com.google.common.annotations.VisibleForTesting;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/** An interface for recording created cloud resources for cleanup. */
public class CleanupRecorder {
  private CleanupRecorder() {}

  /** Scopes required by Janitor. */
  private static final List<String> SCOPES = Arrays.asList("openid", "email", "profile");

  private static TestRecord testRecord = new TestRecord();
  private static ApiClient client = new ApiClient();

  public static void record(CloudResourceUid resource, ClientConfig clientConfig) {
    if (!clientConfig.getCleanupConfig().isPresent()) {
      return;
    }

    CleanupConfig cleanupConfig = clientConfig.getCleanupConfig().get();
    GoogleCredentials googleCredentials =
        clientConfig.getCleanupConfig().get().credentials().createScoped(SCOPES);

    try {
      googleCredentials.refreshIfExpired();
    } catch (IOException e) {
      throw new RuntimeException(
          "Failed to refresh the access token used by Janitor during cleanup.", e);
    }
    client.setAccessToken(googleCredentials.getAccessToken().getTokenValue());
    client.setBasePath(cleanupConfig.janitorBasePath());
    try {
      new JanitorApi(client).createResource(createJanitorResource(resource, clientConfig));
    } catch (ApiException e) {
      throw new JanitorException("Failed to create tracked resource in Janitor", e);
    }
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

  /** Provides an {@link ApiClient}. */
  public static void provideApiClient(ApiClient apiClient) {
    client = apiClient;
  }

  /**
   * Construct {@link CreateResourceRequestBody} which will used to call Janitor Service to track
   * the resource.
   */
  @VisibleForTesting
  static CreateResourceRequestBody createJanitorResource(
      CloudResourceUid resource, ClientConfig clientConfig) {
    CleanupConfig cleanupConfig = clientConfig.getCleanupConfig().get();
    return new CreateResourceRequestBody()
        .resourceUid(resource)
        .timeToLiveInMinutes((int) cleanupConfig.timeToLive().toMinutes())
        .putLabelsItem("client", clientConfig.getClientName())
        .putLabelsItem("cleanupId", cleanupConfig.cleanupId());
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
