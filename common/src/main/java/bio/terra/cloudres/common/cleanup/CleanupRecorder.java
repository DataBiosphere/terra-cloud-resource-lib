package bio.terra.cloudres.common.cleanup;

import bio.terra.cloudres.common.ClientConfig;
import bio.terra.janitor.model.CloudResourceUid;
import bio.terra.janitor.model.CreateResourceRequestBody;

/** An interface for recording created cloud resources for cleanup. */
public class CleanupRecorder {
  private final JanitorService janitorService;
  private final ClientConfig clientConfig;

  public CleanupRecorder(ClientConfig clientConfig) {
    this.clientConfig = clientConfig;
    janitorService =
        clientConfig.getCleanupConfig().isPresent()
            ? new JanitorService(
                clientConfig.getCleanupConfig().get().accessToken(),
                clientConfig.getCleanupConfig().get().janitorBasePath())
            : null;
  }

  public void record(CloudResourceUid resource) {
    if (!clientConfig.getCleanupConfig().isPresent()) {
      return;
    }
    createJanitorResource(resource);
  }

  /** Calls Janitor's create resource endpoint to track the resource. */
  private void createJanitorResource(CloudResourceUid resource) {
    CleanupConfig cleanupConfig = clientConfig.getCleanupConfig().get();
    CreateResourceRequestBody body =
        new CreateResourceRequestBody()
            .resourceUid(resource)
            .timeToLiveInMinutes((int) cleanupConfig.timeToLive().toMinutes())
            .putLabelsItem("client", clientConfig.getClientName())
            .putLabelsItem("cleanupId", cleanupConfig.cleanupId());

    janitorService.createTrackedResource(body);
  }
}
