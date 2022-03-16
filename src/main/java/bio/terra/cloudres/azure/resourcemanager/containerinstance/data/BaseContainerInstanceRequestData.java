package bio.terra.cloudres.azure.resourcemanager.containerinstance.data;

import bio.terra.cloudres.azure.resourcemanager.common.ResourceManagerRequestData;
import com.azure.core.management.Region;
import com.google.gson.JsonObject;

/**
 * Extends {@link ResourceManagerRequestData} to add common fields for working with the Container
 * Instance Manager API.
 */
public abstract class BaseContainerInstanceRequestData implements ResourceManagerRequestData {
  /**
   * The name of the resource - name of the container group. Azure REST API and CLI have different
   * name for this: Azure REST API uses 'containerGroupName' Azure CLI uses - 'name'
   */
  public abstract String containerGroupName();

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
    requestData.addProperty("tenantId", tenantId());
    requestData.addProperty("subscriptionId", subscriptionId());
    requestData.addProperty("resourceGroupName", resourceGroupName());
    requestData.addProperty("containerGroupName", containerGroupName());
    requestData.addProperty("region", region().name());
    return requestData;
  }
}
