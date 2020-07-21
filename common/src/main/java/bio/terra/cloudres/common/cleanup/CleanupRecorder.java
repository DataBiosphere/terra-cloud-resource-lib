package bio.terra.cloudres.common.cleanup;

import bio.terra.cloudres.JanitorService;
import bio.terra.cloudres.common.ClientConfig;
import bio.terra.janitor.controller.JanitorApi;
import bio.terra.janitor.model.CloudResourceUid;
import bio.terra.janitor.model.CreateResourceRequestBody;
import com.google.common.annotations.VisibleForTesting;

import java.util.ArrayList;
import java.util.List;

/** An interface for recording created cloud resources for cleanup. */
public class CleanupRecorder {
  private final JanitorService janitorService;
  private final ClientConfig clientConfig;

  public CleanupRecorder(ClientConfig clientConfig) {
    this.clientConfig = clientConfig;
    janitorService = clientConfig.getCleanupConfig().isPresent() ? new JanitorService(clientConfig.getCleanupConfig().get().accessToken(), clientConfig.getCleanupConfig().get().janitorBasePath()) : null;
  }

  private static TestRecord testRecord = new TestRecord();

  public void record(CloudResourceUid resource) {
    if (!clientConfig.getCleanupConfig().isPresent()) {
      return;
    }
    createJanitorResource(resource);
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

  /** Calls Janitor's create resource endpoint to track the resource. */
  private void createJanitorResource(CloudResourceUid resource) {
    CleanupConfig cleanupConfig = clientConfig.getCleanupConfig().get();
    CreateResourceRequestBody body = new CreateResourceRequestBody().resourceUid(resource).timeToLiveInMinutes((int)cleanupConfig.timeToLive().toMinutes()).putLabelsItem("client", clientConfig.getClientName()).putLabelsItem("cleanupId", cleanupConfig.cleanupId());
    janitorService.createTrackedResource(body);

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
