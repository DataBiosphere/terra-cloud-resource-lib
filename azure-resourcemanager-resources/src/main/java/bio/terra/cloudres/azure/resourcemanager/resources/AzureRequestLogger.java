package bio.terra.cloudres.azure.resourcemanager.resources;

import com.azure.core.http.policy.HttpRequestLogger;
import com.azure.core.http.policy.HttpRequestLoggingContext;
import com.azure.core.util.logging.ClientLogger;
import java.util.Arrays;
import reactor.core.publisher.Mono;

public class AzureRequestLogger implements HttpRequestLogger {
  @Override
  public Mono<Void> logRequest(ClientLogger logger, HttpRequestLoggingContext loggingOptions) {
    // TODO send pubsub request to Janitor

    logger.info("IN logRequest");
    logger.info("The http request: " + loggingOptions.getHttpRequest().toString());
    logger.info(
        loggingOptions.getHttpRequest().getHttpMethod().toString()
            + " "
            + loggingOptions.getHttpRequest().getUrl().toString());
    logger.info(loggingOptions.getHttpRequest().getHeaders().toMap().toString());
    //        logger.info(loggingOptions.getHttpRequest().getBody().);
    logger.info("The try count: " + loggingOptions.getTryCount());
    logger.info("The context: " + loggingOptions.getContext().toString());
    logger.info(loggingOptions.getContext().getValues().toString());
    logger.info(
        Arrays.asList((String[]) loggingOptions.getContext().getData("ARMScopes").get())
            .toString());

    return Mono.empty();
  }
}
