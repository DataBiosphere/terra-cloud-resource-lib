package bio.terra.cloudres.azure.resourcemanager.compute.data;

import bio.terra.cloudres.azure.resourcemanager.common.ResourceManagerRequestData;
import com.azure.core.management.Region;
import com.google.gson.JsonObject;

/**
 * Extends {@link ResourceManagerRequestData} to add common fields for working with the Compute
 * Manager API.
 */
public abstract class BaseComputeRequestData implements ResourceManagerRequestData {
  /** The name of the resource. */
  public abstract String name();

  /** The region of the resource. */
  public abstract Region region();

  /** The resource group of the resource. */
  public abstract String resourceGroupName();

  /**
   * Serializes this object to JSON. Not overriding {@link ResourceManagerRequestData#serialize()}
   * to ensure subclasses implement their own serialize method.
   */
  protected JsonObject serializeCommon() {
    JsonObject requestData = new JsonObject();
    requestData.addProperty("resourceGroupName", resourceGroupName());
    requestData.addProperty("name", name());
    requestData.addProperty("region", region().name());
    return requestData;
  }
}
