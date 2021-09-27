package bio.terra.cloudres.azure.resourcemanager.compute.data;

import bio.terra.cloudres.azure.resourcemanager.compute.ComputeManagerOperation;
import bio.terra.cloudres.common.CloudOperation;
import com.azure.core.management.Region;
import com.azure.resourcemanager.compute.models.Disk;
import com.azure.resourcemanager.network.models.Network;
import com.azure.resourcemanager.network.models.PublicIpAddress;
import com.google.auto.value.AutoValue;
import com.google.gson.JsonObject;

/** Virtual machine creation request data. */
@AutoValue
public abstract class CreateVirtualMachineRequestData extends BaseComputeRequestData {
  /** Network associated with the virtual machine's primary network interface. */
  public abstract Network network();

  /** Subnet associated with the virtual machine's primary network interface. */
  public abstract String subnetName();

  /** Public IP address associated with the VM. */
  public abstract PublicIpAddress publicIpAddress();

  /** Disk associated with the VM. */
  public abstract Disk disk();

  /** Virtual machine image name. */
  public abstract String image();

  @Override
  public final CloudOperation cloudOperation() {
    return ComputeManagerOperation.AZURE_CREATE_VM;
  }

  public static CreateVirtualMachineRequestData.Builder builder() {
    return new AutoValue_CreateVirtualMachineRequestData.Builder();
  }

  @AutoValue.Builder
  public abstract static class Builder {
    public abstract CreateVirtualMachineRequestData.Builder setName(String value);

    public abstract CreateVirtualMachineRequestData.Builder setRegion(Region value);

    public abstract CreateVirtualMachineRequestData.Builder setResourceGroupName(String value);

    public abstract CreateVirtualMachineRequestData.Builder setNetwork(Network value);

    public abstract CreateVirtualMachineRequestData.Builder setSubnetName(String value);

    public abstract CreateVirtualMachineRequestData.Builder setPublicIpAddress(
        PublicIpAddress value);

    public abstract CreateVirtualMachineRequestData.Builder setDisk(Disk value);

    public abstract CreateVirtualMachineRequestData.Builder setImage(String value);

    public abstract CreateVirtualMachineRequestData build();
  }

  @Override
  public JsonObject serialize() {
    JsonObject requestData = super.serialize();
    requestData.addProperty("network", network().name());
    requestData.addProperty("subnetName", subnetName());
    requestData.addProperty("ip", publicIpAddress().ipAddress());
    requestData.addProperty("disk", disk().name());
    requestData.addProperty("image", image());
    return requestData;
  }
}
