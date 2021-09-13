package bio.terra.cloudres.azure.resourcemanager.resources;

import static bio.terra.cloudres.azure.resourcemanager.resources.Defaults.CLOUD_RESOURCE_REQUEST_DATA_KEY;

import bio.terra.cloudres.common.ClientConfig;
import bio.terra.cloudres.common.cleanup.CleanupRecorder;
import com.azure.core.http.policy.HttpRequestLogger;
import com.azure.core.http.policy.HttpRequestLoggingContext;
import com.azure.core.util.Context;
import com.azure.core.util.logging.ClientLogger;
import reactor.core.publisher.Mono;

public class AzureRequestLogger implements HttpRequestLogger {
  private final ClientConfig clientConfig;

  public AzureRequestLogger(ClientConfig clientConfig) {
    this.clientConfig = clientConfig;
  }

  @Override
  public Mono<Void> logRequest(ClientLogger logger, HttpRequestLoggingContext loggingOptions) {
    final Context context = loggingOptions.getContext();
    if (context != null) {
      context
          .getData(CLOUD_RESOURCE_REQUEST_DATA_KEY)
          .ifPresent(
              requestData ->
                  ((AzureRequestData) requestData)
                      .getCloudResourceUid()
                      .ifPresent(
                          resourceUid -> CleanupRecorder.record(resourceUid, null, clientConfig)));
    }
    return Mono.empty();
  }
}
