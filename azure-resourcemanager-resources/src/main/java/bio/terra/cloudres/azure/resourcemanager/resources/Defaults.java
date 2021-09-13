package bio.terra.cloudres.azure.resourcemanager.resources;

import bio.terra.cloudres.common.ClientConfig;
import bio.terra.cloudres.common.CloudOperation;
import com.azure.core.http.policy.HttpLogDetailLevel;
import com.azure.core.http.policy.HttpLogOptions;
import com.azure.core.util.Context;
import java.util.Map;

public class Defaults {
  private Defaults() {}

  static final String CLOUD_OPERATION_CONTEXT_KEY = "cloudOperation";
  static final String CLOUD_RESOURCE_REQUEST_DATA_KEY = "requestData";

  public static Context buildContext(CloudOperation cloudOperation, AzureRequestData requestData) {
    Context ctx =
        Context.of(
            Map.of(
                CLOUD_OPERATION_CONTEXT_KEY,
                cloudOperation,
                CLOUD_RESOURCE_REQUEST_DATA_KEY,
                requestData));
    return ctx;
  }

  public static HttpLogOptions logOptions(ClientConfig clientConfig) {
    return new HttpLogOptions()
        .setRequestLogger(new AzureRequestLogger(clientConfig))
        .setResponseLogger(new AzureResponseLogger(clientConfig))
        .setLogLevel(HttpLogDetailLevel.BODY_AND_HEADERS);
  }
}
