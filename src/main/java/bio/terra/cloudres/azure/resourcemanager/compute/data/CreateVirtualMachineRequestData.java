package bio.terra.cloudres.azure.resourcemanager.compute.data;

import bio.terra.cloudres.azure.resourcemanager.compute.ComputeManagerOperation;
import bio.terra.cloudres.common.CloudOperation;
import bio.terra.janitor.model.AzureResourceGroup;
import bio.terra.janitor.model.AzureVirtualMachine;
import bio.terra.janitor.model.CloudResourceUid;
import com.azure.core.management.Region;
import com.azure.resourcemanager.compute.models.Disk;
import com.azure.resourcemanager.network.models.Network;
import com.azure.resourcemanager.network.models.PublicIpAddress;
import com.google.auto.value.AutoValue;
import com.google.gson.JsonObject;
import java.util.Optional;
import javax.annotation.Nullable;

/** Virtual machine creation request data. */
@AutoValue
public abstract class CreateVirtualMachineRequestData extends BaseComputeRequestData {
  /** Network associated with the virtual machine's primary network interface. */
  public abstract Network network();

  /** Subnet associated with the virtual machine's primary network interface. */
  public abstract String subnetName();

  /** Public IP address associated with the VM. */
  @Nullable
  public abstract PublicIpAddress publicIpAddress();

  /** Disk associated with the VM. */
  @Nullable
  public abstract Disk disk();

  /** Virtual machine image name. */
  public abstract String image();

  @Override
  public final CloudOperation cloudOperation() {
    return ComputeManagerOperation.AZURE_CREATE_VM;
  }

  @Override
  public final Optional<CloudResourceUid> resourceUidCreation() {
    return Optional.of(
        new CloudResourceUid()
            .azureVirtualMachine(
                new AzureVirtualMachine()
                    .resourceGroup(
                        new AzureResourceGroup()
                            .tenantId(tenantId())
                            .subscriptionId(subscriptionId())
                            .resourceGroupName(resourceGroupName()))
                    .vmName(name())));
  }

  public static CreateVirtualMachineRequestData.Builder builder() {
    return new AutoValue_CreateVirtualMachineRequestData.Builder();
  }

  @AutoValue.Builder
  public abstract static class Builder {
    public abstract Builder setName(String value);

    public abstract Builder setRegion(Region value);

    public abstract Builder setTenantId(String value);

    public abstract Builder setSubscriptionId(String value);

    public abstract Builder setResourceGroupName(String value);

    public abstract Builder setNetwork(Network value);

    public abstract Builder setSubnetName(String value);

    public abstract Builder setPublicIpAddress(PublicIpAddress value);

    public abstract Builder setDisk(Disk value);

    public abstract Builder setImage(String value);

    public abstract CreateVirtualMachineRequestData build();
  }

  @Override
  public JsonObject serialize() {
    JsonObject requestData = super.serializeCommon();
    requestData.addProperty("network", network().name());
    requestData.addProperty("subnetName", subnetName());
    Optional.ofNullable(publicIpAddress())
        .ifPresent(ip -> requestData.addProperty("ip", ip.ipAddress()));
    Optional.ofNullable(disk()).ifPresent(d -> requestData.addProperty("disk", d.name()));
    requestData.addProperty("image", image());
    return requestData;
  }
}
