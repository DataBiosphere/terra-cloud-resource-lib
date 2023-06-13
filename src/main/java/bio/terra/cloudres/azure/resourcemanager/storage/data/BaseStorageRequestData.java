package bio.terra.cloudres.azure.resourcemanager.storage.data;

import bio.terra.cloudres.azure.resourcemanager.common.ResourceManagerRequestData;
import com.google.gson.JsonObject;

/**
 * Extends {@link ResourceManagerRequestData} to add common fields for working with the Storage
 * Manager API.
 */
public abstract class BaseStorageRequestData implements ResourceManagerRequestData {
  /** The tenant of the resource. */
  public abstract String tenantId();

  /** The subscription of the resource. */
  public abstract String subscriptionId();

  /** The resource group of the resource. */
  public abstract String resourceGroupName();

  /** The name of the storage account. */
  public abstract String storageAccountName();

  /**
   * Serializes this object to JSON. Not overriding {@link ResourceManagerRequestData#serialize()}
   * to ensure subclasses implement their own serialize method.
   */
  protected JsonObject serializeCommon() {
    JsonObject requestData = new JsonObject();
    requestData.addProperty("tenantId", tenantId());
    requestData.addProperty("subscriptionId", subscriptionId());
    requestData.addProperty("resourceGroupName", resourceGroupName());
    requestData.addProperty("storageAccountName", storageAccountName());
    return requestData;
  }
}
