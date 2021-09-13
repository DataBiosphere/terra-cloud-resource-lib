package bio.terra.cloudres.azure.resourcemanager.resources;

import static bio.terra.cloudres.azure.resourcemanager.resources.Defaults.CLOUD_OPERATION_CONTEXT_KEY;
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
import java.util.Map;
import reactor.core.publisher.Mono;

/**
 * Logs a debug message indicating the completion of a CRL event or that an exception occurred.
 *
 * <p>A structured JsonObject is included in the logging arguments.
 *
 * <p>Plugs into Azure Resource Manager via HttpLogOptions. The Azure SDK will invoke this logger
 * when an HTTP request is completed.
 */
public class AzureResponseLogger implements HttpResponseLogger {
  private final ClientConfig clientConfig;

  public AzureResponseLogger(ClientConfig clientConfig) {
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
      final Map<Object, Object> contextMap = context.getValues();
      if (contextMap.containsKey(CLOUD_OPERATION_CONTEXT_KEY)) {
        logData.addProperty("operation", (String) contextMap.get(CLOUD_OPERATION_CONTEXT_KEY));
      }
      if (contextMap.containsKey(CLOUD_RESOURCE_REQUEST_DATA_KEY)) {
        logData.add("requestData", (JsonObject) contextMap.get(CLOUD_RESOURCE_REQUEST_DATA_KEY));
      }
    }

    logger.info(
        "CRL completed Azure request",
        // Include logData for terra-common-lib logging to pick up and include in JSON output.
        logData);

    return Mono.justOrEmpty(response);
  }
}
