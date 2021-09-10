package bio.terra.cloudres.azure.resourcemanager.resources;

import bio.terra.cloudres.common.ClientConfig;
import com.azure.core.http.policy.HttpLogDetailLevel;
import com.azure.core.http.policy.HttpLogOptions;

public class Defaults {
  private Defaults() {}

  public static HttpLogOptions logOptions(ClientConfig clientConfig) {
    return new HttpLogOptions()
        .setRequestLogger(new AzureRequestLogger())
        .setResponseLogger(new AzureResponseLogger(clientConfig))
        .setLogLevel(HttpLogDetailLevel.BODY_AND_HEADERS)
        .setPrettyPrintBody(true);
  }
}
