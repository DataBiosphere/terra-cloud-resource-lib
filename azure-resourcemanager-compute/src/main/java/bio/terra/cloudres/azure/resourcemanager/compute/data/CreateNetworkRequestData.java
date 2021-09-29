package bio.terra.cloudres.azure.resourcemanager.compute.data;

import bio.terra.cloudres.azure.resourcemanager.compute.ComputeManagerOperation;
import bio.terra.cloudres.common.CloudOperation;
import com.azure.core.management.Region;
import com.azure.resourcemanager.network.models.NetworkSecurityGroup;
import com.google.auto.value.AutoValue;
import com.google.gson.JsonObject;

/** Network creation request data. */
@AutoValue
public abstract class CreateNetworkRequestData extends BaseComputeRequestData {
  /** CIDR representation of the address space of the network. */
  public abstract String addressSpaceCidr();

  /** Name of the subnet. */
  public abstract String subnetName();

  /** The address space prefix, in CIDR notation, assigned to the subnet. */
  public abstract String addressPrefix();

  /** The network security group to associate with this network. */
  public abstract NetworkSecurityGroup networkSecurityGroup();

  @Override
  public final CloudOperation cloudOperation() {
    return ComputeManagerOperation.AZURE_CREATE_NETWORK;
  }

  public static CreateNetworkRequestData.Builder builder() {
    return new AutoValue_CreateNetworkRequestData.Builder();
  }

  @AutoValue.Builder
  public abstract static class Builder {
    public abstract CreateNetworkRequestData.Builder setName(String value);

    public abstract CreateNetworkRequestData.Builder setRegion(Region value);

    public abstract CreateNetworkRequestData.Builder setResourceGroupName(String value);

    public abstract CreateNetworkRequestData.Builder setAddressSpaceCidr(String value);

    public abstract CreateNetworkRequestData.Builder setSubnetName(String value);

    public abstract CreateNetworkRequestData.Builder setAddressPrefix(String value);

    public abstract CreateNetworkRequestData.Builder setNetworkSecurityGroup(
        NetworkSecurityGroup value);

    public abstract CreateNetworkRequestData build();
  }

  @Override
  public JsonObject serialize() {
    JsonObject requestData = super.serializeCommon();
    requestData.addProperty("addressSpaceCidr", addressSpaceCidr());
    requestData.addProperty("subnetName", subnetName());
    requestData.addProperty("addressPrefix", addressPrefix());
    requestData.addProperty("networkSecurityGroupName", networkSecurityGroup().name());
    return requestData;
  }
}
