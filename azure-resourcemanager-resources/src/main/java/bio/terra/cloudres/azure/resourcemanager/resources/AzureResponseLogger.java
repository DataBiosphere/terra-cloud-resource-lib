package bio.terra.cloudres.azure.resourcemanager.resources;

import bio.terra.cloudres.common.ClientConfig;
import com.azure.core.http.HttpHeaders;
import com.azure.core.http.HttpResponse;
import com.azure.core.http.policy.HttpResponseLogger;
import com.azure.core.http.policy.HttpResponseLoggingContext;
import com.azure.core.util.logging.ClientLogger;
import com.google.api.client.http.HttpStatusCodes;
import com.google.gson.JsonObject;
import java.io.ByteArrayOutputStream;
import java.time.Duration;
import java.util.Optional;
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

  private static Optional<Long> getContentLength(HttpHeaders headers) {
    final String contentLengthStr = headers.getValue("Content-Length");
    if (contentLengthStr != null && !contentLengthStr.trim().isBlank()) {
      try {
        return Optional.of(Long.parseLong(contentLengthStr));
      } catch (NumberFormatException e) {
        return Optional.empty();
      }
    }
    return Optional.empty();
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
    final HttpResponse response = loggingOptions.getHttpResponse();
    logData.addProperty("responseStatusCode", response.getStatusCode());
    // If the request was not successful, include the response body as a string

    if (!HttpStatusCodes.isSuccess(response.getStatusCode())) {
      getContentLength(response.getHeaders())
          .ifPresent(
              contentLength -> {
                HttpResponse bufferedResponse = response.buffer();
                ByteArrayOutputStream outputStream =
                    new ByteArrayOutputStream(contentLength.intValue());
              });

      //            HttpResponse bufferedResponse = response.buffer();
      //            ByteArrayOutputStream outputStream = new ByteArrayOutputStream((int)
      // contentLength);
      //            WritableByteChannel bodyContentChannel = Channels.newChannel(outputStream);
      //            return bufferedResponse.getBody()
      //                    .flatMap(byteBuffer -> writeBufferToBodyStream(bodyContentChannel,
      // byteBuffer))
      //                    .doFinally(ignored -> {
      //                        responseLogMessage.append("Response body:")
      //                                .append(System.lineSeparator())
      //                                .append(prettyPrintIfNeeded(logger, prettyPrintBody,
      // contentTypeHeader,
      //                                        convertStreamToString(outputStream, logger)))
      //                                .append(System.lineSeparator())
      //                                .append("<-- END HTTP");
      //
      //                        logAndReturn(logger, logLevel, responseLogMessage, response);
      //                    }).then(Mono.just(bufferedResponse));
    }

    logger.info("CRL completed ");

    logger.info("IN logResponse");
    logger.info("The http response: " + loggingOptions.getHttpResponse().toString());
    logger.info(String.valueOf(loggingOptions.getHttpResponse().getStatusCode()));
    //        loggingOptions.getHttpResponse().
    logger.info("The try count: " + loggingOptions.getTryCount());
    //        logger.info("The context: " + loggingOptions.getContext().toString());
    //        logger.info(loggingOptions.getContext().getValues().toString());

    return Mono.justOrEmpty(response);
  }
}
