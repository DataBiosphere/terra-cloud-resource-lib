package bio.terra.cloudres.azure.resourcemanager.relay.data;

import bio.terra.cloudres.azure.resourcemanager.common.ResourceManagerRequestData;
import bio.terra.cloudres.azure.resourcemanager.relay.RelayManagerOperation;
import bio.terra.cloudres.common.CloudOperation;
import bio.terra.janitor.model.AzureRelay;
import bio.terra.janitor.model.AzureResourceGroup;
import bio.terra.janitor.model.CloudResourceUid;
import com.azure.core.management.Region;
import com.google.auto.value.AutoValue;
import com.google.gson.JsonObject;
import java.util.Optional;

/**
 * Extends {@link ResourceManagerRequestData} to add common fields for working with the Compute
 * Manager API.
 */
@AutoValue
public abstract class CreateRelayRequestData extends BaseRelayRequestData {

  /** The region of the resource. */
  public abstract Region region();

  @Override
  public final CloudOperation cloudOperation() {
    return RelayManagerOperation.AZURE_CREATE_RELAY;
  }

  @Override
  public final Optional<CloudResourceUid> resourceUidCreation() {
    return Optional.of(
        new CloudResourceUid()
            .azureRelay(
                new AzureRelay()
                    .resourceGroup(
                        new AzureResourceGroup()
                            .tenantId(tenantId())
                            .subscriptionId(subscriptionId())
                            .resourceGroupName(resourceGroupName()))
                    .relayName(name())));
  }

  public static Builder builder() {
    return new AutoValue_CreateRelayRequestData.Builder();
  }

  @AutoValue.Builder
  public abstract static class Builder {
    public abstract Builder setName(String value);

    public abstract Builder setRegion(Region value);

    public abstract Builder setTenantId(String value);

    public abstract Builder setSubscriptionId(String value);

    public abstract Builder setResourceGroupName(String value);

    public abstract CreateRelayRequestData build();
  }

  @Override
  public JsonObject serialize() {
    JsonObject requestData = super.serializeCommon();
    requestData.addProperty("region", region().name());
    return requestData;
  }
}