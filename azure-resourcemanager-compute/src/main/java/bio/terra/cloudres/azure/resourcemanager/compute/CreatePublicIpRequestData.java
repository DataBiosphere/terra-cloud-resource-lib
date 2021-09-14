package bio.terra.cloudres.azure.resourcemanager.compute;

import bio.terra.cloudres.azure.resourcemanager.resources.AzureRequestData;
import com.azure.core.management.Region;
import com.google.gson.JsonObject;

/** Data for an Azure IP creation request. */
public class CreatePublicIpRequestData extends BaseRequestData {

  protected PublicIpRequestData(String resourceGroupName, String name, Region region) {
    super(ComputeManagerOperation.AZURE_CREATE_PUBLIC_IP, resourceGroupName, name, region);
  }

  @Override
  public JsonObject getRequestData() {
    return super.getRequestData();
  }
}
