package bio.terra.cloudres.azure.resourcemanager.common;

import static bio.terra.cloudres.azure.resourcemanager.common.Defaults.CLOUD_RESOURCE_REQUEST_DATA_KEY;

import bio.terra.cloudres.common.ClientConfig;
import bio.terra.cloudres.common.cleanup.CleanupRecorder;
import com.azure.core.http.policy.HttpRequestLogger;
import com.azure.core.http.policy.HttpRequestLoggingContext;
import com.azure.core.util.Context;
import com.azure.core.util.logging.ClientLogger;
import reactor.core.publisher.Mono;

/** Intercepts Azure HTTP requests to record cloud resource creations for cleanup. */
public class AzureRequestLogger implements HttpRequestLogger {
  private final ClientConfig clientConfig;

  AzureRequestLogger(ClientConfig clientConfig) {
    this.clientConfig = clientConfig;
  }

  @Override
  public Mono<Void> logRequest(ClientLogger logger, HttpRequestLoggingContext loggingOptions) {
    final Context context = loggingOptions.getContext();
    if (context != null) {
      context
          .getData(CLOUD_RESOURCE_REQUEST_DATA_KEY)
          .ifPresent(
              data -> {
                AbstractRequestData requestData = (AbstractRequestData) data;
                requestData
                    .resourceUidCreation()
                    .ifPresent(
                        resourceUid ->
                            CleanupRecorder.record(
                                resourceUid,
                                requestData.resourceCreationMetadata().orElse(null),
                                clientConfig));
              });
    }
    return Mono.empty();
  }
}
