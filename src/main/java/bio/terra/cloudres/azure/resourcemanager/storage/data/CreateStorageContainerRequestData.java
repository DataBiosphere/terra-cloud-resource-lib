package bio.terra.cloudres.azure.resourcemanager.storage.data;

import bio.terra.cloudres.azure.resourcemanager.storage.StorageManagerOperation;
import bio.terra.cloudres.common.CloudOperation;
import bio.terra.janitor.model.AzureResourceGroup;
import bio.terra.janitor.model.AzureStorageContainer;
import bio.terra.janitor.model.CloudResourceUid;
import com.google.auto.value.AutoValue;
import com.google.gson.JsonObject;
import java.util.Optional;

@AutoValue
public abstract class CreateStorageContainerRequestData extends BaseStorageRequestData {
  /** The name of the storage container. */
  public abstract String storageContainerName();

  @Override
  public final CloudOperation cloudOperation() {
    return StorageManagerOperation.AZURE_CREATE_STORAGE_CONTAINER;
  }

  @Override
  public final Optional<CloudResourceUid> resourceUidCreation() {
    return Optional.of(
        new CloudResourceUid()
            .azureStorageContainer(
                new AzureStorageContainer()
                    .resourceGroup(
                        new AzureResourceGroup()
                            .tenantId(tenantId())
                            .subscriptionId(subscriptionId())
                            .resourceGroupName(resourceGroupName()))
                    .storageAccountName(storageAccountName())
                    .storageContainerName(storageContainerName())));
  }

  public static CreateStorageContainerRequestData.Builder builder() {
    return new AutoValue_CreateStorageContainerRequestData.Builder();
  }

  @AutoValue.Builder
  public abstract static class Builder {
    public abstract CreateStorageContainerRequestData.Builder setStorageAccountName(String value);

    public abstract CreateStorageContainerRequestData.Builder setStorageContainerName(String value);

    public abstract CreateStorageContainerRequestData.Builder setTenantId(String value);

    public abstract CreateStorageContainerRequestData.Builder setSubscriptionId(String value);

    public abstract CreateStorageContainerRequestData.Builder setResourceGroupName(String value);

    public abstract CreateStorageContainerRequestData build();
  }

  @Override
  public JsonObject serialize() {
    JsonObject requestData = super.serializeCommon();
    requestData.addProperty("storageContainerName", storageContainerName());
    return requestData;
  }
}
