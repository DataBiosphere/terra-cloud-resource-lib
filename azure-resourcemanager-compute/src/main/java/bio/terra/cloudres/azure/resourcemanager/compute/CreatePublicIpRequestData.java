package bio.terra.cloudres.azure.resourcemanager.compute;

import bio.terra.cloudres.azure.resourcemanager.resources.AbstractRequestData;
import com.azure.core.management.Region;
import com.google.gson.JsonObject;

/** Data for an Azure IP creation request. */
public class CreatePublicIpRequestData extends AbstractRequestData {
  private final String resourceGroupName;
  private final String name;
  private final Region region;

  public CreatePublicIpRequestData(String resourceGroupName, String name, Region region) {
    super(ComputeManagerOperation.AZURE_CREATE_PUBLIC_IP);
    this.resourceGroupName = resourceGroupName;
    this.name = name;
    this.region = region;
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
