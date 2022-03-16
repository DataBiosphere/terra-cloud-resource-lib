package bio.terra.cloudres.azure.resourcemanager.compute.data;

import bio.terra.cloudres.azure.resourcemanager.compute.ComputeManagerOperation;
import bio.terra.cloudres.common.CloudOperation;
import bio.terra.janitor.model.AzureNetworkSecurityGroup;
import bio.terra.janitor.model.AzureResourceGroup;
import bio.terra.janitor.model.CloudResourceUid;
import com.azure.core.management.Region;
import com.google.auto.value.AutoValue;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import java.util.List;
import java.util.Optional;

/** Network security group creation request data. */
@AutoValue
public abstract class CreateNetworkSecurityGroupRequestData extends BaseComputeRequestData {
  /** Names of the security rules associated with this network security group. */
  public abstract List<String> rules();

  @Override
  public final CloudOperation cloudOperation() {
    return ComputeManagerOperation.AZURE_CREATE_NETWORK_SECURITY_GROUP;
  }

  @Override
  public final Optional<CloudResourceUid> resourceUidCreation() {
    return Optional.of(
        new CloudResourceUid()
            .azureNetworkSecurityGroup(
                new AzureNetworkSecurityGroup()
                    .resourceGroup(
                        new AzureResourceGroup()
                            .tenantId(tenantId())
                            .subscriptionId(subscriptionId())
                            .resourceGroupName(resourceGroupName()))
                    .networkSecurityGroupName(name())));
  }

  public static CreateNetworkSecurityGroupRequestData.Builder builder() {
    return new AutoValue_CreateNetworkSecurityGroupRequestData.Builder();
  }

  @AutoValue.Builder
  public abstract static class Builder {
    public abstract Builder setName(String value);

    public abstract Builder setRegion(Region value);

    public abstract Builder setTenantId(String value);

    public abstract Builder setSubscriptionId(String value);

    public abstract Builder setResourceGroupName(String value);

    public abstract Builder setRules(List<String> rules);

    public abstract CreateNetworkSecurityGroupRequestData build();
  }

  @Override
  public JsonObject serialize() {
    JsonObject requestData = super.serializeCommon();
    JsonArray rules = new JsonArray();
    rules().forEach(rules::add);
    requestData.add("rules", rules);
    return requestData;
  }
}
