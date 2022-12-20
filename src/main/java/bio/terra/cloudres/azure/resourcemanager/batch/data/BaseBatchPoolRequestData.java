package bio.terra.cloudres.azure.resourcemanager.batch.data;

import bio.terra.cloudres.azure.resourcemanager.common.ResourceManagerRequestData;
import com.azure.core.management.Region;
import com.google.gson.JsonObject;

public abstract class BaseBatchPoolRequestData implements ResourceManagerRequestData {
  /** The region of the resource. */
  public abstract Region region();

  /** The tenant of the resource. */
  public abstract String tenantId();

  /** The subscription of the resource. */
  public abstract String subscriptionId();

  /** The resource group of the resource. */
  public abstract String resourceGroupName();

  /**
   * Serializes this object to JSON. Not overriding {@link ResourceManagerRequestData#serialize()}
   * to ensure subclasses implement their own serialize method.
   */
  protected JsonObject serializeCommon() {
    JsonObject requestData = new JsonObject();
    requestData.addProperty("region", region().name());
    requestData.addProperty("tenantId", tenantId());
    requestData.addProperty("subscriptionId", subscriptionId());
    requestData.addProperty("resourceGroupName", resourceGroupName());
    return requestData;
  }
}
