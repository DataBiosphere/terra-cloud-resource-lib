package bio.terra.cloudres.azure.resourcemanager.compute.data;

import bio.terra.cloudres.azure.resourcemanager.compute.ComputeManagerOperation;
import bio.terra.janitor.model.CloudResourceUid;
import com.azure.core.management.Region;
import com.google.gson.JsonObject;
import java.util.Optional;

/** Network creation request data. */
public class CreateNetworkRequestData extends BaseRequestData {
  private final String subnetName;

  public CreateNetworkRequestData(
      String resourceGroupName, String name, Region region, String subnetName) {
    super(ComputeManagerOperation.AZURE_CREATE_NETWORK, resourceGroupName, name, region);
    this.subnetName = subnetName;
  }

  @Override
  public Optional<CloudResourceUid> resourceUidCreation() {
    // TODO: populate this when the CloudResourceUid Janitor model is regenerated
    return Optional.empty();
  }

  @Override
  public JsonObject serialize() {
    JsonObject requestData = super.serialize();
    requestData.addProperty("subnetName", subnetName);
    return requestData;
  }
}
