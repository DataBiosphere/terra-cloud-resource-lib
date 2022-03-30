package bio.terra.cloudres.azure.resourcemanager.common;

import static bio.terra.cloudres.azure.resourcemanager.common.Defaults.CLOUD_RESOURCE_REQUEST_DATA_KEY;

import bio.terra.cloudres.common.ClientConfig;
import bio.terra.cloudres.common.cleanup.CleanupRecorder;
import com.azure.core.http.policy.HttpRequestLogger;
import com.azure.core.http.policy.HttpRequestLoggingContext;
import com.azure.core.util.Context;
import com.azure.core.util.logging.ClientLogger;
import java.util.Optional;
import reactor.core.publisher.Mono;

/**
 * Intercepts Azure cloud resource creations to record them for cleanup.
 *
 * <p>Implemented as a {@link HttpRequestLogger} to record created resources as soon as the request
 * is made. This is a no-op if the HTTP request is not a cloud resource creation.
 *
 * 
 */
public class AzureResourceCleanupRecorder implements HttpRequestLogger {
  private final ClientConfig clientConfig;

  public AzureResourceCleanupRecorder(ClientConfig clientConfig) {
    this.clientConfig = clientConfig;
  }

  @Override
  public Mono<Void> logRequest(ClientLogger logger, HttpRequestLoggingContext loggingOptions) {
    final Context context = loggingOptions.getContext();

    Optional.ofNullable(context)
        .flatMap(c -> c.getData(CLOUD_RESOURCE_REQUEST_DATA_KEY))
        .ifPresent(
            data -> {
              ResourceManagerRequestData requestData = (ResourceManagerRequestData) data;
              requestData
                  .resourceUidCreation()
                  .ifPresent(
                      resourceUid ->
                          CleanupRecorder.record(
                              resourceUid,
                              requestData.resourceCreationMetadata().orElse(null),
                              clientConfig));
            });
    return Mono.empty();
  }
}
