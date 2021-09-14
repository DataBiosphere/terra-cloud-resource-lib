package bio.terra.cloudres.azure.resourcemanager.compute.data;

import bio.terra.cloudres.azure.resourcemanager.compute.ComputeManagerOperation;
import bio.terra.janitor.model.CloudResourceUid;
import com.azure.core.management.Region;
import com.azure.resourcemanager.compute.models.Disk;
import com.azure.resourcemanager.network.models.Network;
import com.azure.resourcemanager.network.models.PublicIpAddress;
import com.google.gson.JsonObject;
import java.util.Optional;

/** Virtual machine creation request data. */
public class CreateVirtualMachineRequestData extends BaseRequestData {
  private final Network network;
  private final String subnetName;
  private final PublicIpAddress ip;
  private final Disk disk;

  public CreateVirtualMachineRequestData(
      String resourceGroupName,
      String name,
      Region region,
      Network network,
      String subnetName,
      PublicIpAddress ip,
      Disk disk) {
    super(ComputeManagerOperation.AZURE_CREATE_VM, resourceGroupName, name, region);
    this.network = network;
    this.subnetName = subnetName;
    this.ip = ip;
    this.disk = disk;
  }

  @Override
  public Optional<CloudResourceUid> resourceUidCreation() {
    // TODO: populate this when the CloudResourceUid Janitor model is regenerated
    return Optional.empty();
  }

  @Override
  public JsonObject serialize() {
    JsonObject requestData = super.serialize();
    requestData.addProperty("network", network.name());
    requestData.addProperty("subnetName", subnetName);
    requestData.addProperty("ip", ip.ipAddress());
    requestData.addProperty("disk", disk.name());
    return requestData;
  }
}
