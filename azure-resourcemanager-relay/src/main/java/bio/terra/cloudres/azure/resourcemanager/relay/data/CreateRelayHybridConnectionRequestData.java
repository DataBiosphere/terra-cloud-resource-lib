package bio.terra.cloudres.azure.resourcemanager.relay.data;

import bio.terra.cloudres.azure.resourcemanager.common.ResourceManagerRequestData;
import bio.terra.cloudres.azure.resourcemanager.relay.RelayManagerOperation;
import bio.terra.cloudres.common.CloudOperation;
import com.azure.core.management.Region;
import com.google.auto.value.AutoValue;
import com.google.gson.JsonObject;

/**
 * Extends {@link ResourceManagerRequestData} to add common fields for working with the Compute
 * Manager API.
 */
@AutoValue
public abstract class CreateRelayHybridConnectionRequestData extends BaseRelayRequestData {

  @Override
  public final CloudOperation cloudOperation() {
    return RelayManagerOperation.AZURE_CREATE_RELAY_HYBRID_CONNNECTION;
  }

  public static CreateRelayHybridConnectionRequestData.Builder builder() {
    return new AutoValue_CreateRelayHybridConnectionRequestData.Builder();
  }

  @AutoValue.Builder
  public abstract static class Builder {
    public abstract CreateRelayHybridConnectionRequestData.Builder setName(String value);

    public abstract CreateRelayHybridConnectionRequestData.Builder setRegion(Region value);

    public abstract CreateRelayHybridConnectionRequestData.Builder setTenantId(String value);

    public abstract CreateRelayHybridConnectionRequestData.Builder setSubscriptionId(String value);

    public abstract CreateRelayHybridConnectionRequestData.Builder setResourceGroupName(String value);

    public abstract CreateRelayHybridConnectionRequestData build();
  }

  @Override
  public JsonObject serialize() {
    JsonObject requestData = super.serializeCommon();
    return requestData;
  }
}
