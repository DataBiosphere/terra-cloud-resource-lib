package bio.terra.cloudres.azure.resourcemanager.compute.data;

import bio.terra.cloudres.azure.resourcemanager.common.AbstractRequestData;
import bio.terra.cloudres.common.CloudOperation;
import com.azure.core.management.Region;
import com.google.gson.JsonObject;

/**
 * Extends {@link AbstractRequestData} to add common fields for working with the Compute Manager
 * API.
 */
public abstract class BaseRequestData extends AbstractRequestData {
  protected final String name;
  protected final Region region;
  protected final String resourceGroupName;

  protected BaseRequestData(
      CloudOperation cloudOperation, String resourceGroupName, String name, Region region) {
    super(cloudOperation);
    this.name = name;
    this.region = region;
    this.resourceGroupName = resourceGroupName;
  }

  @Override
  public JsonObject serialize() {
    JsonObject requestData = new JsonObject();
    requestData.addProperty("resourceGroupName", resourceGroupName);
    requestData.addProperty("name", name);
    requestData.addProperty("region", region.name());
    return requestData;
  }
}
