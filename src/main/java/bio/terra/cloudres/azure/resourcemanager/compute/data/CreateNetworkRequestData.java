package bio.terra.cloudres.azure.resourcemanager.compute.data;

import bio.terra.cloudres.azure.resourcemanager.compute.ComputeManagerOperation;
import bio.terra.cloudres.common.CloudOperation;
import bio.terra.janitor.model.AzureNetwork;
import bio.terra.janitor.model.AzureResourceGroup;
import bio.terra.janitor.model.CloudResourceUid;
import com.azure.core.management.Region;
import com.azure.resourcemanager.network.models.NetworkSecurityGroup;
import com.google.auto.value.AutoValue;
import com.google.gson.JsonObject;
import java.util.Optional;

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

  @Override
  public final Optional<CloudResourceUid> resourceUidCreation() {
    return Optional.of(
        new CloudResourceUid()
            .azureNetwork(
                new AzureNetwork()
                    .resourceGroup(
                        new AzureResourceGroup()
                            .tenantId(tenantId())
                            .subscriptionId(subscriptionId())
                            .resourceGroupName(resourceGroupName()))
                    .networkName(name())));
  }

  public static CreateNetworkRequestData.Builder builder() {
    return new AutoValue_CreateNetworkRequestData.Builder();
  }

  @AutoValue.Builder
  public abstract static class Builder {
    public abstract Builder setName(String value);

    public abstract Builder setRegion(Region value);

    public abstract Builder setTenantId(String value);

    public abstract Builder setSubscriptionId(String value);

    public abstract Builder setResourceGroupName(String value);

    public abstract Builder setAddressSpaceCidr(String value);

    public abstract Builder setSubnetName(String value);

    public abstract Builder setAddressPrefix(String value);

    public abstract Builder setNetworkSecurityGroup(NetworkSecurityGroup value);

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
