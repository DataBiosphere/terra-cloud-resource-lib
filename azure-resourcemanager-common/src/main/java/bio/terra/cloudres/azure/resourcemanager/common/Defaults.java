package bio.terra.cloudres.azure.resourcemanager.common;

import bio.terra.cloudres.common.ClientConfig;
import com.azure.core.http.policy.HttpLogDetailLevel;
import com.azure.core.http.policy.HttpLogOptions;
import com.azure.core.util.Context;

/** Provides defaults for working with the Azure Resource Manager API. */
public class Defaults {
  private Defaults() {}

  static final String CLOUD_RESOURCE_REQUEST_DATA_KEY = "requestData";

  /** Builds a standard {@link Context} object for calling Azure Resource Manager APIs. */
  public static Context buildContext(AbstractRequestData requestData) {
    return new Context(CLOUD_RESOURCE_REQUEST_DATA_KEY, requestData);
  }

  /** Returns a default {@link HttpLogOptions} for initializing an Azure Resource Manager. */
  public static HttpLogOptions logOptions(ClientConfig clientConfig) {
    return new HttpLogOptions()
        .setRequestLogger(new AzureRequestLogger(clientConfig))
        .setResponseLogger(new AzureResponseLogger(clientConfig))
        .setLogLevel(HttpLogDetailLevel.BODY_AND_HEADERS);
  }
}
