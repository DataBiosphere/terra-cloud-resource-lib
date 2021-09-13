package bio.terra.cloudres.azure.resourcemanager.compute;

import bio.terra.cloudres.azure.resourcemanager.resources.AzureRequestData;
import bio.terra.janitor.model.CloudResourceUid;
import com.azure.core.management.Region;
import com.google.gson.JsonObject;
import java.util.Optional;

public class PublicIpRequestData extends AzureRequestData {
  private final String resourceGroupName;
  private final String name;
  private final Region region;

  protected PublicIpRequestData(
      String resourceGroupName,
      String name,
      Region region,
      Optional<CloudResourceUid> cloudResourceUid) {
    super(cloudResourceUid);
    this.resourceGroupName = resourceGroupName;
    this.name = name;
    this.region = region;
  }

  @Override
  public JsonObject getRequestData() {
    JsonObject requestData = new JsonObject();
    requestData.addProperty("resourceGroupName", resourceGroupName);
    requestData.addProperty("name", name);
    requestData.addProperty("region", region.name());
    return requestData;
  }
}
