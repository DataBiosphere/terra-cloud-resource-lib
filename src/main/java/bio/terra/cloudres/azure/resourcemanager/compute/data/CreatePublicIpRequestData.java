package bio.terra.cloudres.azure.resourcemanager.compute.data;

import bio.terra.cloudres.azure.resourcemanager.compute.ComputeManagerOperation;
import bio.terra.cloudres.common.CloudOperation;
import bio.terra.janitor.model.AzurePublicIp;
import bio.terra.janitor.model.AzureResourceGroup;
import bio.terra.janitor.model.CloudResourceUid;
import com.azure.core.management.Region;
import com.azure.resourcemanager.network.models.IpAllocationMethod;
import com.google.auto.value.AutoValue;
import com.google.gson.JsonObject;
import java.util.Optional;

/** Public IP creation request data. */
@AutoValue
public abstract class CreatePublicIpRequestData extends BaseComputeRequestData {

  /** The IP address allocation method (Static/Dynamic). */
  public abstract IpAllocationMethod ipAllocationMethod();

  @Override
  public final CloudOperation cloudOperation() {
    return ComputeManagerOperation.AZURE_CREATE_PUBLIC_IP;
  }

  @Override
  public final Optional<CloudResourceUid> resourceUidCreation() {
    return Optional.of(
        new CloudResourceUid()
            .azurePublicIp(
                new AzurePublicIp()
                    .resourceGroup(
                        new AzureResourceGroup()
                            .tenantId(tenantId())
                            .subscriptionId(subscriptionId())
                            .resourceGroupName(resourceGroupName()))
                    .ipName(name())));
  }

  public static CreatePublicIpRequestData.Builder builder() {
    return new AutoValue_CreatePublicIpRequestData.Builder();
  }

  @AutoValue.Builder
  public abstract static class Builder {
    public abstract Builder setName(String value);

    public abstract Builder setRegion(Region value);

    public abstract Builder setTenantId(String value);

    public abstract Builder setSubscriptionId(String value);

    public abstract Builder setResourceGroupName(String value);

    public abstract Builder setIpAllocationMethod(IpAllocationMethod IpAllocationMethod);

    public abstract CreatePublicIpRequestData build();
  }

  @Override
  public JsonObject serialize() {
    JsonObject requestData = super.serializeCommon();
    requestData.addProperty("ipAllocationMethod", ipAllocationMethod().toString());
    return requestData;
  }
}
