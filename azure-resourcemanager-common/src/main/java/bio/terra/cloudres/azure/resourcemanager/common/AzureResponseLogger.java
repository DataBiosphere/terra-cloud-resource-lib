package bio.terra.cloudres.azure.resourcemanager.common;

import static bio.terra.cloudres.azure.resourcemanager.common.Defaults.CLOUD_RESOURCE_REQUEST_DATA_KEY;

import bio.terra.cloudres.common.ClientConfig;
import bio.terra.cloudres.common.OperationAnnotator;
import bio.terra.cloudres.common.OperationData;
import com.azure.core.http.ContentType;
import com.azure.core.http.HttpHeaders;
import com.azure.core.http.HttpRequest;
import com.azure.core.http.HttpResponse;
import com.azure.core.http.policy.HttpResponseLogger;
import com.azure.core.http.policy.HttpResponseLoggingContext;
import com.azure.core.util.CoreUtils;
import com.azure.core.util.logging.ClientLogger;
import com.google.common.base.Charsets;
import com.google.gson.JsonObject;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.Channels;
import java.nio.channels.WritableByteChannel;
import java.time.Duration;
import java.util.Optional;
import java.util.OptionalInt;
import java.util.function.Consumer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

/**
 * Intercepts Azure HTTP responses and invokes {@link OperationAnnotator} to annotate cloud resource
 * operations with logs, traces, and metrics.
 */
public class AzureResponseLogger implements HttpResponseLogger {
  private static final int MAX_BODY_LOG_SIZE = 1024 * 16;
  private static final Logger logger = LoggerFactory.getLogger(AzureResponseLogger.class);

  private final OperationAnnotator operationAnnotator;

  AzureResponseLogger(ClientConfig clientConfig) {
    // Note we use our own Logger instead of the ClientLogger wrapper that Azure provides.
    this.operationAnnotator = new OperationAnnotator(clientConfig, logger);
  }

  @Override
  public Mono<HttpResponse> logResponse(
      ClientLogger clientLogger, HttpResponseLoggingContext loggingOptions) {
    final HttpResponse response = loggingOptions.getHttpResponse();
    final HttpRequest request = response.getRequest();

    // Always add request method and request URL
    JsonObject requestDataJson = new JsonObject();
    requestDataJson.addProperty("requestMethod", request.getHttpMethod().toString());
    requestDataJson.addProperty("requestUrl", request.getUrl().toString());

    // Optionally add rich request data if provided in the logging context
    Optional<ResourceManagerRequestData> requestData =
        Optional.ofNullable(loggingOptions.getContext())
            .flatMap(c -> c.getData(CLOUD_RESOURCE_REQUEST_DATA_KEY))
            .map(o -> (ResourceManagerRequestData) o);
    requestData.ifPresent(d -> requestDataJson.add("requestBody", d.serialize()));

    // Add the raw request/response body only if debug logging is enabled, as it may be very
    // verbose.
    if (logger.isDebugEnabled()) {
      logBody(
          request.getHeaders(),
          request.getBody(),
          s -> requestDataJson.addProperty("rawRequestBody", s));
      logBody(
          response.getHeaders(),
          response.buffer().getBody(),
          s -> requestDataJson.addProperty("rawResponseBody", s));
    }

    // Build OperationData object.
    OperationData operationData =
        OperationData.builder()
            .setDuration(
                Optional.ofNullable(loggingOptions.getResponseDuration()).orElse(Duration.ZERO))
            .setTryCount(OptionalInt.of(loggingOptions.getTryCount()))
            .setExecutionException(Optional.empty())
            .setHttpStatusCode(OptionalInt.of(response.getStatusCode()))
            .setCloudOperation(
                requestData
                    .map(ResourceManagerRequestData::cloudOperation)
                    .orElse(ResourceManagerOperation.AZURE_RESOURCE_MANAGER_OPERATION))
            .setRequestData(requestDataJson)
            .build();

    // Invoke OperationAnnotator to record the operation.
    operationAnnotator.recordOperation(operationData);

    return Mono.justOrEmpty(response);
  }

  private static void logBody(
      HttpHeaders headers, Flux<ByteBuffer> body, Consumer<String> consumer) {
    // Ensure we have a valid content length
    String contentLengthString = headers.getValue("Content-Length");
    if (CoreUtils.isNullOrEmpty(contentLengthString)) {
      return;
    }
    final long contentLength;
    try {
      contentLength = Long.parseLong(contentLengthString);
    } catch (NumberFormatException | NullPointerException e) {
      return;
    }

    // The body is logged if the Content-Type is not "application/octet-stream" and the body isn't
    // empty
    // and is less than 16KB in size.
    String contentTypeHeader = headers.getValue("Content-Type");
    if (!ContentType.APPLICATION_OCTET_STREAM.equalsIgnoreCase(contentTypeHeader)
        && contentLength != 0
        && contentLength < MAX_BODY_LOG_SIZE) {
      ByteArrayOutputStream outputStream = new ByteArrayOutputStream((int) contentLength);
      WritableByteChannel bodyContentChannel = Channels.newChannel(outputStream);
      body.flatMap(
              byteBuffer -> {
                try {
                  bodyContentChannel.write(byteBuffer.duplicate());
                  return Mono.just(byteBuffer);
                } catch (IOException e) {
                  return Mono.error(e);
                }
              })
          .doFinally(ignored -> consumer.accept(outputStream.toString(Charsets.UTF_8)));
    }
  }
}
