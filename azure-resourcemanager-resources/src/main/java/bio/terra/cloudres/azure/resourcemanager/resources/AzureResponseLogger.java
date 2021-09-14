package bio.terra.cloudres.azure.resourcemanager.resources;

import static bio.terra.cloudres.azure.resourcemanager.resources.Defaults.CLOUD_RESOURCE_REQUEST_DATA_KEY;

import bio.terra.cloudres.common.ClientConfig;
import com.azure.core.http.HttpRequest;
import com.azure.core.http.HttpResponse;
import com.azure.core.http.policy.HttpResponseLogger;
import com.azure.core.http.policy.HttpResponseLoggingContext;
import com.azure.core.util.Context;
import com.azure.core.util.logging.ClientLogger;
import com.google.gson.JsonObject;
import java.time.Duration;
import reactor.core.publisher.Mono;

/**
 * Intercepts Azure HTTP responses and logs a debug message indicating the completion of a CRL event
 * or that an exception occurred.
 *
 * <p>A structured JsonObject is included in the logging arguments to plug into Terra Common
 * Logging. See:
 * https://github.com/DataBiosphere/terra-common-lib/tree/develop/src/main/java/bio/terra/common/logging
 */
public class AzureResponseLogger implements HttpResponseLogger {
  private final ClientConfig clientConfig;

  AzureResponseLogger(ClientConfig clientConfig) {
    this.clientConfig = clientConfig;
  }

  @Override
  public Mono<HttpResponse> logResponse(
      ClientLogger logger, HttpResponseLoggingContext loggingOptions) {
    JsonObject logData = new JsonObject();
    logData.addProperty("clientName", clientConfig.getClientName());
    final Duration responseDuration = loggingOptions.getResponseDuration();
    if (responseDuration != null) {
      logData.addProperty("durationMs", responseDuration.toMillis());
    }
    final Integer tryCount = loggingOptions.getTryCount();
    if (tryCount != null) {
      logData.addProperty("tryCount", tryCount);
    }
    final HttpResponse response = loggingOptions.getHttpResponse();
    logData.addProperty("responseStatusCode", response.getStatusCode());

    final HttpRequest request = response.getRequest();
    logData.addProperty("requestMethod", request.getHttpMethod().toString());
    logData.addProperty("requestUrl", request.getUrl().toString());

    final Context context = loggingOptions.getContext();
    if (context != null) {
      context
          .getData(CLOUD_RESOURCE_REQUEST_DATA_KEY)
          .ifPresent(
              data -> {
                AbstractRequestData requestData = (AbstractRequestData) data;
                logData.addProperty("operation", requestData.cloudOperation().name());
                logData.add("requestData", requestData.serialize());
              });
    }

    logger.info(
        "CRL completed Azure request",
        // Include logData for terra-common-lib logging to pick up and include in JSON output.
        logData);

    return Mono.justOrEmpty(response);
  }
}
