package bio.terra.cloudres.azure.resourcemanager.compute.data;

import bio.terra.cloudres.azure.resourcemanager.compute.ComputeManagerOperation;
import bio.terra.cloudres.common.CloudOperation;
import com.azure.core.management.Region;
import com.azure.resourcemanager.network.models.IpAllocationMethod;
import com.google.auto.value.AutoValue;
import com.google.gson.JsonObject;

/** Public IP creation request data. */
@AutoValue
public abstract class CreatePublicIpRequestData extends BaseComputeRequestData {

  /** The IP address allocation method (Static/Dynamic). */
  public abstract IpAllocationMethod ipAllocationMethod();

  @Override
  public final CloudOperation cloudOperation() {
    return ComputeManagerOperation.AZURE_CREATE_PUBLIC_IP;
  }

  public static CreatePublicIpRequestData.Builder builder() {
    return new AutoValue_CreatePublicIpRequestData.Builder();
  }

  @AutoValue.Builder
  public abstract static class Builder {
    public abstract CreatePublicIpRequestData.Builder setName(String value);

    public abstract CreatePublicIpRequestData.Builder setRegion(Region value);

    public abstract CreatePublicIpRequestData.Builder setResourceGroupName(String value);

    public abstract CreatePublicIpRequestData.Builder setIpAllocationMethod(
        IpAllocationMethod IpAllocationMethod);

    public abstract CreatePublicIpRequestData build();
  }

  @Override
  public JsonObject serialize() {
    JsonObject requestData = super.serialize();
    requestData.addProperty("ipAllocationMethod", ipAllocationMethod().toString());
    return requestData;
  }
}
