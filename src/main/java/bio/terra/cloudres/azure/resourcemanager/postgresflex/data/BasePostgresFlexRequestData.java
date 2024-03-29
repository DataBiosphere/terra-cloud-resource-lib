package bio.terra.cloudres.azure.resourcemanager.postgresflex.data;

import bio.terra.cloudres.azure.resourcemanager.common.ResourceManagerRequestData;
import com.google.gson.JsonObject;

/**
 * Extends {@link ResourceManagerRequestData} to add common fields for working with the Postgres
 * Flex API.
 */
public abstract class BasePostgresFlexRequestData implements ResourceManagerRequestData {
  /** The tenant of the resource. */
  public abstract String tenantId();

  /** The subscription of the resource. */
  public abstract String subscriptionId();

  /** The resource group of the resource. */
  public abstract String resourceGroupName();

  /** The postgres server name. */
  public abstract String serverName();

  /**
   * Serializes this object to JSON. Not overriding {@link ResourceManagerRequestData#serialize()}
   * to ensure subclasses implement their own serialize method.
   */
  protected JsonObject serializeCommon() {
    JsonObject requestData = new JsonObject();
    requestData.addProperty("tenantId", tenantId());
    requestData.addProperty("subscriptionId", subscriptionId());
    requestData.addProperty("resourceGroupName", resourceGroupName());
    requestData.addProperty("serverName", serverName());
    return requestData;
  }
}
