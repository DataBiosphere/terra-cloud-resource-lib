package bio.terra.cloudres.azure.resourcemanager.compute.data;

import bio.terra.cloudres.azure.resourcemanager.compute.ComputeManagerOperation;
import bio.terra.janitor.model.CloudResourceUid;
import com.azure.core.management.Region;
import java.util.Optional;

/** Network security group creation request data. */
public class CreateNetworkSecurityGroupRequestData extends BaseRequestData {
  public CreateNetworkSecurityGroupRequestData(
      String resourceGroupName, String name, Region region) {
    super(
        ComputeManagerOperation.AZURE_CREATE_NETWORK_SECURITY_GROUP,
        resourceGroupName,
        name,
        region);
  }

  @Override
  public Optional<CloudResourceUid> resourceUidCreation() {
    // TODO: populate this when the CloudResourceUid Janitor model is regenerated
    return Optional.empty();
  }
}
