package bio.terra.cloudres.azure.resourcemanager.batch.data;

import bio.terra.cloudres.azure.resourcemanager.batch.BatchManagerOperation;
import bio.terra.cloudres.common.CloudOperation;
import bio.terra.janitor.model.AzureBatchPool;
import bio.terra.janitor.model.AzureResourceGroup;
import bio.terra.janitor.model.CloudResourceUid;
import com.google.auto.value.AutoValue;
import com.google.gson.JsonObject;
import java.util.Optional;

@AutoValue
public abstract class CreateBatchPoolRequestData extends BaseBatchPoolRequestData {
  /** The id of the resource. */
  public abstract String id();

  /** The vmSize of the resource. */
  public abstract String vmSize();

  @Override
  public final CloudOperation cloudOperation() {
    return BatchManagerOperation.AZURE_CREATE_BATCH_POOL;
  }

  @Override
  public final Optional<CloudResourceUid> resourceUidCreation() {
    return Optional.of(
        new CloudResourceUid()
            .azureBatchPool(
                new AzureBatchPool()
                    .resourceGroup(
                        new AzureResourceGroup()
                            .tenantId(tenantId())
                            .subscriptionId(subscriptionId())
                            .resourceGroupName(resourceGroupName()))
                    .id(id())));
  }

  public static CreateBatchPoolRequestData.Builder builder() {
    return new AutoValue_CreateBatchPoolRequestData.Builder();
  }

  @AutoValue.Builder
  public abstract static class Builder {
    public abstract CreateBatchPoolRequestData.Builder setId(String value);

    public abstract CreateBatchPoolRequestData.Builder setVmSize(String value);

    public abstract CreateBatchPoolRequestData.Builder setTenantId(String value);

    public abstract CreateBatchPoolRequestData.Builder setSubscriptionId(String value);

    public abstract CreateBatchPoolRequestData.Builder setResourceGroupName(String value);

    public abstract CreateBatchPoolRequestData.Builder setBatchAccountName(String value);

    public abstract CreateBatchPoolRequestData build();
  }

  @Override
  public JsonObject serialize() {
    JsonObject requestData = super.serializeCommon();
    requestData.addProperty("id", id());
    requestData.addProperty("vmSize", vmSize());
    return requestData;
  }
}
