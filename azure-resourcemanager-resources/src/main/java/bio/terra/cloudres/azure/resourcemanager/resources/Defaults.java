package bio.terra.cloudres.azure.resourcemanager.resources;

import bio.terra.cloudres.common.ClientConfig;
import bio.terra.cloudres.common.CloudOperation;
import bio.terra.janitor.model.CloudResourceUid;
import com.azure.core.http.policy.HttpLogDetailLevel;
import com.azure.core.http.policy.HttpLogOptions;
import com.azure.core.util.Context;
import java.util.Map;
import java.util.Optional;

public class Defaults {
  private Defaults() {}

  static final String CLOUD_OPERATION_CONTEXT_KEY = "cloudOperation";
  static final String CLOUD_RESOURCE_REQUEST_DATA_KEY = "requestData";
  static final String CLOUD_RESOURCE_UID_KEY = "cloudResourceUid";

  public static Context buildContext(CloudOperation cloudOperation, AzureRequestData requestData) {
    return buildContext(cloudOperation, requestData, Optional.empty());
  }

  public static Context buildContext(
      CloudOperation cloudOperation,
      AzureRequestData requestData,
      Optional<CloudResourceUid> cloudResourceUid) {
    Context ctx =
        Context.of(
            Map.of(
                CLOUD_OPERATION_CONTEXT_KEY,
                cloudOperation,
                CLOUD_RESOURCE_REQUEST_DATA_KEY,
                requestData));
    if (cloudResourceUid.isPresent()) {
      return ctx.addData(CLOUD_RESOURCE_UID_KEY, cloudResourceUid.get());
    }

    return ctx;
  }

  public static HttpLogOptions logOptions(ClientConfig clientConfig) {
    return new HttpLogOptions()
        .setRequestLogger(new AzureRequestLogger(clientConfig))
        .setResponseLogger(new AzureResponseLogger(clientConfig))
        .setLogLevel(HttpLogDetailLevel.BODY_AND_HEADERS);
  }
}
