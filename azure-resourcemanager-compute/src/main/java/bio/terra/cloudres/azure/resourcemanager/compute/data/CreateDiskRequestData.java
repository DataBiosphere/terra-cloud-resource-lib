package bio.terra.cloudres.azure.resourcemanager.compute.data;

import bio.terra.cloudres.azure.resourcemanager.compute.ComputeManagerOperation;
import bio.terra.janitor.model.CloudResourceUid;
import com.azure.core.management.Region;
import com.google.gson.JsonObject;
import java.util.Optional;

/** Disk creation request data. */
public class CreateDiskRequestData extends BaseRequestData {
  private final int size;

  public CreateDiskRequestData(String resourceGroupName, String name, Region region, int size) {
    super(ComputeManagerOperation.AZURE_CREATE_DISK, resourceGroupName, name, region);
    this.size = size;
  }

  @Override
  public Optional<CloudResourceUid> resourceUidCreation() {
    // TODO: populate this when the CloudResourceUid Janitor model is regenerated
    return Optional.empty();
  }

  @Override
  public JsonObject serialize() {
    JsonObject requestData = super.serialize();
    requestData.addProperty("size", size);
    return requestData;
  }
}
