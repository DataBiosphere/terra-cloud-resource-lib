package bio.terra.cloudres.azure.resourcemanager.compute.data;

import bio.terra.cloudres.azure.resourcemanager.compute.ComputeManagerOperation;
import bio.terra.cloudres.common.CloudOperation;
import com.azure.core.management.Region;
import com.google.auto.value.AutoValue;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import java.util.List;

/** Network security group creation request data. */
@AutoValue
public abstract class CreateNetworkSecurityGroupRequestData extends BaseComputeRequestData {
  /** Names of the security rules associated with this network security group. */
  public abstract List<String> rules();

  @Override
  public final CloudOperation cloudOperation() {
    return ComputeManagerOperation.AZURE_CREATE_NETWORK_SECURITY_GROUP;
  }

  public static CreateNetworkSecurityGroupRequestData.Builder builder() {
    return new AutoValue_CreateNetworkSecurityGroupRequestData.Builder();
  }

  @AutoValue.Builder
  public abstract static class Builder {
    public abstract CreateNetworkSecurityGroupRequestData.Builder setName(String value);

    public abstract CreateNetworkSecurityGroupRequestData.Builder setRegion(Region value);

    public abstract CreateNetworkSecurityGroupRequestData.Builder setResourceGroupName(
        String value);

    public abstract CreateNetworkSecurityGroupRequestData.Builder setRules(List<String> rules);

    public abstract CreateNetworkSecurityGroupRequestData build();
  }

  @Override
  public JsonObject serialize() {
    JsonObject requestData = super.serialize();
    JsonArray rules = new JsonArray();
    rules().forEach(rules::add);
    requestData.add("rules", rules);
    return requestData;
  }
}