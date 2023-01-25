package bio.terra.cloudres.azure.resourcemanager.batch.data;

import bio.terra.cloudres.azure.resourcemanager.common.ResourceManagerRequestData;
import com.google.gson.JsonObject;

public abstract class BaseBatchPoolRequestData implements ResourceManagerRequestData {
  /** The tenant of the resource. */
  public abstract String tenantId();

  /** The subscription of the resource. */
  public abstract String subscriptionId();

  /** The resource group of the resource. */
  public abstract String resourceGroupName();

  /** The batch account resource ID. */
  public abstract String batchAccountName();

  /**
   * Serializes this object to JSON. Not overriding {@link ResourceManagerRequestData#serialize()}
   * to ensure subclasses implement their own serialize method.
   */
  protected JsonObject serializeCommon() {
    JsonObject requestData = new JsonObject();
    requestData.addProperty("tenantId", tenantId());
    requestData.addProperty("subscriptionId", subscriptionId());
    requestData.addProperty("resourceGroupName", resourceGroupName());
    requestData.addProperty("batchAccountName", batchAccountName());
    return requestData;
  }
}
