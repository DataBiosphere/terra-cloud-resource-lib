package bio.terra.cloudres.azure.resourcemanager.compute.data;

import bio.terra.cloudres.azure.resourcemanager.compute.ComputeManagerOperation;
import bio.terra.janitor.model.CloudResourceUid;
import com.azure.core.management.Region;
import java.util.Optional;

/** Public IP creation request data. */
public class CreatePublicIpRequestData extends BaseRequestData {
  public CreatePublicIpRequestData(String resourceGroupName, String name, Region region) {
    super(ComputeManagerOperation.AZURE_CREATE_PUBLIC_IP, resourceGroupName, name, region);
  }

  @Override
  public Optional<CloudResourceUid> resourceUidCreation() {
    // TODO: populate this when the CloudResourceUid Janitor model is regenerated
    return Optional.empty();
  }
}
